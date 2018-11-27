import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opencv.highgui.Highgui;

class OpenCVUtils {
    public static Mat readFile(String fileName) {
        Mat img = Highgui.imread(fileName);
        return img;
    }

    public static void writeImage(Mat mat, String dest) {
        Highgui.imwrite(dest, mat);
    }

    public static Mat blur(Mat input) {
        Mat destImage = input.clone();
        Imgproc.blur(input, destImage, new Size(3.0, 3.0));
        return destImage;
    }

    public static Mat gray(Mat input) {
        Mat gray = new Mat(input.rows(), input.cols(), CvType.CV_8UC1);
        Imgproc.cvtColor(input, gray, Imgproc.COLOR_RGB2GRAY);
        return gray;
    }

    public static Mat canny(Mat input) {
        Mat gray = gray(input);
        Imgproc.blur(gray, input, new Size(3, 3));
        int threshold = 2;
        Imgproc.Canny(input, input, threshold, threshold * 3, 3, false);
        Mat dest = input.clone();
        Core.add(dest, Scalar.all(0), dest);
        dest.copyTo(dest, input);
        return dest;
    }
}

class AppOpenCV {
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(AppOpenCV.class);
        nu.pattern.OpenCV.loadShared();
        // System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

        String worker = args[0] == null ? "Worker " + new String().hashCode() : args[0];

        System.out.println("Running Worker");
        JobCompletion jCompletion = new JobCompletion();
        jCompletion.setWorker(worker);

        try {
            Channel channel = ConexionRabbitMQ.getChannel();
            channel.exchangeDeclare(RabbitMQStuff.EXCHANGE, "direct", true);
            channel.queueDeclare(RabbitMQStuff.COLA_TRABAJOS, false, false, false, null);
            channel.queueBind(RabbitMQStuff.COLA_TRABAJOS, RabbitMQStuff.EXCHANGE, "");
            channel.basicQos(1);

            System.out.println(" [*] Waiting for messages. To exit ");
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                        byte[] body) throws IOException {

                    String msg = new String(body, "UTF-8");
                    ImageJob imageJob = new ImageJob();
                    imageJob = new Gson().fromJson(msg, ImageJob.class);
                    long receptionW = System.currentTimeMillis();
                    Mat effect = null;
                    System.out.println(
                            "[x] Received: '" + imageJob.getAction() + " --- " + imageJob.getImageDst() + "'");
                    try {
                        processCopy(imageJob.getPathSrc() + "/" + imageJob.getImageSrc());
                        // Mat img = OpenCVUtils.readFile(imageJob.getPathSrc() + "/" + imageJob.getImageSrc());
                        Mat img = OpenCVUtils.readFile("./images/" + imageJob.getImageSrc());
                        String action = imageJob.getAction();
                        switch (action) {
                        case "blur":
                            effect = OpenCVUtils.blur(img);
                            break;
                        case "canny":
                            effect = OpenCVUtils.canny(img);
                            break;
                        case "gray":
                            effect = OpenCVUtils.gray(img);
                            break;
                        default:
                            break;
                        }
                        // OpenCVUtils.writeImage(effect, imageJob.getPathDst() + "/" + imageJob.getImageDst());
                        OpenCVUtils.writeImage(effect, "./images/" + imageJob.getImageDst());
                        processCopyDest(imageJob.getPathDst(), imageJob.getImageDst());
                        long finalizationW = System.currentTimeMillis();
                        jCompletion.setImage(imageJob.getImageDst());
                        jCompletion.setTsCreationMessage(receptionW - imageJob.getTsCreationMessage());
                        jCompletion.setTsReceptionWorker(receptionW);
                        jCompletion.setTsFinalizationWorker(finalizationW);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        System.out.println(" [x] Work Done!");
                        channel.basicAck(envelope.getDeliveryTag(), false);
                        jobCompletionTask(jCompletion, effect);
                    }
                }
            };
            boolean autoAck = false;
            channel.basicConsume(RabbitMQStuff.COLA_TRABAJOS, autoAck, consumer);

        } catch (IOException e) {
            logger.error("Error: ", e);
        } catch (TimeoutException e) {
            logger.error("Error: ", e);
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
    }

    public static void jobCompletionTask(JobCompletion jobCompletion, Mat effect) {

        if (effect != null) {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            byte[] body = gson.toJson(jobCompletion).getBytes();
            try {
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

    public static void processCopy(String pathSrc) throws IOException, InterruptedException {
        String[] commands = new String[] { "bash", "-c", "scp twcam@10.50.0.10:" + pathSrc + " ./images/" };
        ProcessBuilder pb = new ProcessBuilder(commands);
        System.out.println("Run processCopy server to worker");
        Process process = pb.start();
        int errCode = process.waitFor();
        System.out.println("scp command executed, any errors? " + (errCode == 0 ? "No" : "Yes"));
    }

    public static void processCopyDest(String pathDst, String imgNameDst) throws IOException, InterruptedException {
       System.out.println("imagen a buscar: ./images/" + imgNameDst);
        String[] commands = new String[] { "bash", "-c", "scp ./images/" + imgNameDst + " twcam@10.50.0.10:" + pathDst };
        ProcessBuilder pb = new ProcessBuilder(commands);
        System.out.println("Run processCopy worker to server");
        Process process = pb.start();
        int errCode = process.waitFor();
        System.out.println("scp command executed, any errors? " + (errCode == 0 ? "No" : "Yes"));
    }

}
