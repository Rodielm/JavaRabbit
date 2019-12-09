
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

class StatsCollector {
    public static void main(String[] args) {

        // Logger logger = LoggerFactory.getLogger(StatsCollector.class);

        System.out.println("Ejecutando StatCollector");

        try {
            final Channel c = ConexionRabbitMQ.getChannel();
            c.exchangeDeclare(RabbitMQStuff.EXCHANGE, "direct", true);
            c.queueDeclare(RabbitMQStuff.COLA_TIEMPOS, false, false, false, null);
            c.queueBind(RabbitMQStuff.COLA_TIEMPOS, RabbitMQStuff.EXCHANGE, "");
            c.basicQos(1);
            System.out.println(" Esperando el mensaje... ");

            Consumer consumer = new DefaultConsumer(c) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                        byte[] body) throws IOException {

                    JobCompletion jc = new JobCompletion();

                    try {
                        String msg = new String(body, "UTF-8");

                        // Pasar datos de json a objeto java.
                        jc = new Gson().fromJson(msg, JobCompletion.class);
                    } finally {
                        System.out.println(" Tiempo recibido de la imagen: " + jc.getWorker() + ": "
                                + (jc.getTsFinalizationWorker() - jc.getTsReceptionWorker()) + " ms");
                        c.basicAck(envelope.getDeliveryTag(), false);
                        try {
                            registrarTiempo(jc);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            boolean autoAck = false;
            c.basicConsume(RabbitMQStuff.COLA_TIEMPOS, autoAck, consumer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registrarTiempo(JobCompletion jc) throws Exception {
        final String DELIMITER = ",";
        PrintWriter pw = new PrintWriter(new FileWriter("/data/tiempo.csv", true));
        StringBuffer line = new StringBuffer();
        line.append(jc.getWorker()).append(DELIMITER);
        line.append(jc.getTsFinalizationWorker() - jc.getTsReceptionWorker()).append(DELIMITER);
        line.append(jc.getTsCreationMessage()).append(DELIMITER);
        line.append(jc.getTsReceptionWorker()).append(DELIMITER);
        line.append(jc.getTsFinalizationWorker()).append(DELIMITER)
        .append(jc.getImage());
        pw.println(line);
        pw.flush();
        pw.close();
        if (pw.checkError()) {
            throw new Exception();
        }

    }
}