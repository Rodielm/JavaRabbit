import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;

import cs.edu.uv.http.dynamicresponse.MultipartUtils;
import cs.edu.uv.http.dynamicresponse.ResponseClass;
import cs.edu.uv.http.dynamicresponse.ThingsAboutRequest;
import cs.edu.uv.http.dynamicresponse.ThingsAboutResponse;

public class ImProcServer extends ResponseClass {

  // Directorio donde guardaremos las imágenes que nos envían
  private static final String PATH_SRC = "/tmp";
  // Directorio donde se deben guardar las imágenes procesadas
  private static final String PATH_DST = "/var/web/resources/images";

  // curl -H 'Expect:' -F "ACTION=blur" -F "IMAGE=@/home/twcam/images/images-1.png" http://localhost:8080/rabbit

  public void ifPost(ThingsAboutRequest req, ThingsAboutResponse resp) throws Exception {

    try {
      // Comprueba que se trata de una petición multipart/form-data
      if (MultipartUtils.isMultipartFormData(req.getHeaders())) {
        HashMap<String, String> params = new HashMap<String, String>();
        HashMap<String, String> files = new HashMap<String, String>();
        MultipartUtils multipartUtils = new MultipartUtils(req);

        //Genera un nombre único para la imagen.
        multipartUtils.parseMultipart(params, files, PATH_SRC);

        //Obtiene la acción a realizar sobre la imagen y guardar la imagen
        String action = params.get("ACTION");
        String imageName = files.get("IMAGE");

        long startMessage = System.currentTimeMillis();

        //Crea una instancia del tipo ImageJob
        ImageJob imageJob = new ImageJob();
        imageJob.setAction(params.get("ACTION"));
        imageJob.setPathSrc(PATH_SRC);
        imageJob.setPathDst(PATH_DST);
        imageJob.setImageSrc(imageName);
        imageJob.setImageDst("processed-" + imageName);
        imageJob.setTsCreationMessage(startMessage);

        // Serialización de la instancia del tipo ImageJob a JSON (String)
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        // Obtención del cuerpo del mensaje
        byte[] messageBody = gson.toJson(imageJob).getBytes();
        // Publicar el mensaje con el trabajo a realizar
        // Usamos el canal para definir: el exchange, la cola y la asociación
        // exchange-cola
        Channel c = ConexionRabbitMQ.getChannel();
        c.exchangeDeclare(RabbitMQStuff.EXCHANGE, "direct", true);
        c.queueDeclare(RabbitMQStuff.COLA_TRABAJOS, false, false, false, null);
        c.queueBind(RabbitMQStuff.COLA_TRABAJOS, RabbitMQStuff.EXCHANGE, RabbitMQStuff.RK_TRABAJOS);
        c.basicPublish(RabbitMQStuff.EXCHANGE, RabbitMQStuff.RK_TRABAJOS, null, messageBody);

        resp.flushResponseHeaders();
        PrintWriter pw = resp.getWriter();
        pw.println(req.getHeaders());
        pw.println("{\"status\":\"ok\"}");
        pw.flush();
        pw.close();
      }
    } catch (Exception ex) {
      resp.setStatus(500);
      resp.flushResponseHeaders();
      PrintWriter pw = resp.getWriter();
      pw.println("<html><body>");
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
