import org.opencv.core.Core;
import org.opencv.core.Mat;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

public class Worker {
   // Concatena la ruta con el nombre de fichero
   // Si es necesario pone el separador '/'
   private static String imgPath(String dir, String file) {
      String path = dir + file;
      if (!dir.endsWith("/"))
         path = dir + "/" + file;
      return path;
   }

   public static void main(String[] args) {

      String worker = args[0];
      // Cargar la librería de OpenCV
      System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

      System.out.println("Worker en ejecución");

      // Modelo de datos para el trabajo completado
      JobCompletion jc = new JobCompletion();
      jc.setWorker(worker);

      try {

         // Obtener un canal para comunicarnos con RabbitMQ
         Channel channel = ConexionRabbitMQ.getChannel();

         // Declararación del exchange
         channel.exchangeDeclare(RabbitMQStuff.EXCHANGE, "direct", true);

         // Declaración de la cola
         channel.queueDeclare(RabbitMQStuff.COLA_TRABAJOS, false, false, false, null);

         // Enlazar la cola
         channel.queueBind(RabbitMQStuff.COLA_TRABAJOS, RabbitMQStuff.EXCHANGE, "");

         channel.basicQos(1);

         // Notificar consumo del mensaje
         System.out.println(" Esperando el mensaje. ");
         Consumer consumer = new DefaultConsumer(channel) {

            // Registrar un handler para procesar cada mensaje:
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                  byte[] body) throws IOException {

               // Recoger la imagen
               String imagen = new String(body, "UTF-8");

               ImageJob imageJob = new ImageJob();

               // Obtener cuerpo y deserializar el JSON a ImageJob
               // deserializar -> convertir json en un objeto java
               imageJob = new Gson().fromJson(imagen, ImageJob.class);

               // Tiempo de recepción de la imagen
               long recepcionImagen = System.currentTimeMillis();

               Mat effect = null;
               System.out
                     .println(" Imagen recibida: '" + imageJob.getAction() + " --- " + imageJob.getImageDst() + "'");
               try {

                  // - Simular un tiempo de procesado de 2 segundos
                  TimeUnit.SECONDS.sleep(2);
                  
                  // Lee la imagen
                  Mat img = OpenCVUtils.readFile(imgPath(imageJob.getPathSrc(), imageJob.getImageSrc()));

                  // Recoge la acción a realizar a la imagen
                  String action = imageJob.getAction();

                  switch (action) {
                  case "blur":
                     effect = OpenCVUtils.blur(img);
                     break;
                  case "edge":
                     effect = OpenCVUtils.canny(img);
                     break;
                  case "gray":
                     effect = OpenCVUtils.gray(img);
                     break;
                  default:
                     break;
                  }
                  OpenCVUtils.writeImage(effect, imgPath(imageJob.getPathDst(), imageJob.getImageDst()));

                  // Tiempo de finalizacion de la imagen
                  long finalizacionImagen = System.currentTimeMillis();

                  jc.setImage(imageJob.getImageDst());
                  jc.setTsCreationMessage(recepcionImagen - imageJob.getTsCreationMessage());
                  jc.setTsReceptionWorker(recepcionImagen);
                  jc.setTsFinalizationWorker(finalizacionImagen);
               } catch (Exception e) {
                  e.printStackTrace();
               } finally {
                  System.out.println(" Trabajo Hecho! ");
                  channel.basicAck(envelope.getDeliveryTag(), false);
                  envioAlStats(jc, effect);
               }
            }
         };
         boolean autoAck = false;
         channel.basicConsume(RabbitMQStuff.COLA_TRABAJOS, autoAck, consumer);

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static void envioAlStats(JobCompletion jc, Mat effect) {

      if (effect != null) {
         GsonBuilder builder = new GsonBuilder();
         Gson gson = builder.create();
         byte[] body = gson.toJson(jc).getBytes();
         try {
            // - Envío del mensaje con los tiempos
            Channel channel = ConexionRabbitMQ.getChannel();
            channel.exchangeDeclare(RabbitMQStuff.EXCHANGE, "direct", true);
            channel.queueDeclare(RabbitMQStuff.COLA_TIEMPOS, false, false, false, null);
            channel.queueBind(RabbitMQStuff.COLA_TIEMPOS, RabbitMQStuff.EXCHANGE, RabbitMQStuff.RK_TIEMPOS);
            channel.basicPublish(RabbitMQStuff.EXCHANGE, RabbitMQStuff.RK_TIEMPOS, null, body);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

   }
}
