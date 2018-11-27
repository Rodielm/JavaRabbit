import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ReceivedJobCompleted {

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(ReceivedJobCompleted.class);

        System.out.println("Running JobCompletions");

        try {
            final Channel c = ConexionRabbitMQ.getChannel();
            c.exchangeDeclare(RabbitMQStuff.EXCHANGE, "direct", true);
            c.queueDeclare(RabbitMQStuff.COLA_TIEMPOS, false, false, false, null);
            c.queueBind(RabbitMQStuff.COLA_TIEMPOS, RabbitMQStuff.EXCHANGE, "");
            c.basicQos(1);
            System.out.println(" [*] Waiting for messages. To exit ");

            Consumer consumer = new DefaultConsumer(c) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                        byte[] body) throws IOException {

                    JobCompletion jCompletion = new JobCompletion();
                    try {
                        String msg = new String(body, "UTF-8");
                        jCompletion = new Gson().fromJson(msg, JobCompletion.class);
                    } finally {
                        System.out.println(" [x] Time Received: " + jCompletion.getWorker() + ": "
                                + (jCompletion.getTsFinalizationWorker() - jCompletion.getTsReceptionWorker()) + " ms");
                        c.basicAck(envelope.getDeliveryTag(), false);
                        try {
                            almacenJobCompletion(jCompletion);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            };
            boolean autoAck = false;
            c.basicConsume(RabbitMQStuff.COLA_TIEMPOS, autoAck, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void almacenJobCompletion(JobCompletion jCompletion) throws Exception {
        final String DELIMITER = ",";
        PrintWriter pw = new PrintWriter(new FileWriter("/var/web/app-data/jCompletionRecords.txt", true));
        StringBuffer line = new StringBuffer();
        line.append(jCompletion.getWorker());
        line.append(DELIMITER);
        line.append(jCompletion.getTsFinalizationWorker() - jCompletion.getTsReceptionWorker());
        line.append(DELIMITER);
        line.append(jCompletion.getTsCreationMessage());
        line.append(DELIMITER);
        line.append(jCompletion.getTsReceptionWorker());
        line.append(DELIMITER);
        line.append(jCompletion.getTsFinalizationWorker());
        line.append(DELIMITER);
        line.append(jCompletion.getImage());
        pw.println(line);
        pw.flush();
        pw.close();
        if (pw.checkError()) {
            throw new Exception();
        }

    }
}
