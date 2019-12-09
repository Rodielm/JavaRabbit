import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import cs.edu.uv.http.dynamicresponse.client.WebResponse;
import cs.edu.uv.http.dynamicresponse.client.ThingsAboutRequest;
import cs.edu.uv.http.dynamicresponse.client.ThingsAboutResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class ImProcServer extends WebResponse {

   // Directorio donde guardaremos las imágenes que nos envían
   private static final String LOCAL_PATH_SRC = "/var/web/resources/subidas";

   // Directorio de donde deben leer los workers las imágenes procesadas
   private static final String PATH_SRC = "/data/subidas";

   // Directorio donde deben guardar los workers las imágenes procesadas
   private static final String PATH_DST = "/data/procesadas";

   public ImProcServer() {
   }

   public void ifPost(ThingsAboutRequest req, ThingsAboutResponse resp) throws Exception {

      try {
         // Comprobacion que se trata de una petición multipart/form-data
         if (req.isMultipart()) {
            HashMap<String, String> parametros = new HashMap<String, String>();
            HashMap<String, String> archivo = new HashMap<String, String>();

            // Subida del archivo al resources/subida
            req.parseMultipart(parametros, archivo, LOCAL_PATH_SRC, false);

            // Obtener la acción a realizar y la imagen del cuerpo de la petición

            String action = parametros.get("ACTION");
            String img = archivo.get("IMAGE");

            // Creacion una instancia del tipo ImageJob con la información necesaria
            ImageJob ij = new ImageJob();
            ij.setAction(action);
            ij.setImageSrc(img);
            ij.setImageDst("procesado-" + img);
            ij.setPathDst(PATH_DST);
            ij.setPathSrc(PATH_SRC);

            // Serialización de la instancia del tipo ImageJob a JSON (String)
            Gson gson = new Gson();
            String json = gson.toJson(ij);

            // Obtención del cuerpo del mensaje
            byte[] messageBody = json.getBytes();

            // Usamos el canal para definir: el exchange, la cola y la asociación
            // exchange-cola
            Channel c = ConexionRabbitMQ.getChannel();
            c.exchangeDeclare(RabbitMQStuff.EXCHANGE, "direct", true);
            c.queueDeclare(RabbitMQStuff.COLA_TRABAJOS, false, false, false, null);
            c.queueBind(RabbitMQStuff.COLA_TRABAJOS, RabbitMQStuff.EXCHANGE, RabbitMQStuff.RK_TRABAJOS);

            // Publicar el mensaje con el trabajo a realizar
            c.basicPublish(RabbitMQStuff.EXCHANGE, RabbitMQStuff.RK_TRABAJOS, null, messageBody);

            PrintWriter pw = resp.getWriter();
            pw.println(req.getHeaders());
            pw.println("{\"status\":\"envío de la imagen \" " + img + "}");
            pw.println(action + " " + img);
            pw.flush();
            pw.close();
         }

      } catch (Exception ex) {
         resp.setStatus(500);
         PrintWriter pw = resp.getWriter();
         pw.println("<html><body>");
         pw.println("Error del Servidor");
         StringWriter sw = new StringWriter();
         PrintWriter pwt = new PrintWriter(sw);
         ex.printStackTrace(pwt);
         pw.println(sw.toString());
         pw.println("</body></html>");
         pw.flush();
         pw.close();
      }

   }
}
