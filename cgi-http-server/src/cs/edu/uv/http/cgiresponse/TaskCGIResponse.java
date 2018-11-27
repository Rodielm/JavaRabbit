package cs.edu.uv.http.cgiresponse;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;

import cs.edu.uv.http.staticresponse.UtilsHTTP;

/**
 * This class deals with the response when the URL corresponds to a CGI call
 * 
 * @author juan
 * 
 */
public class TaskCGIResponse implements Runnable {
	private OutputStream out;
	private Socket canal;
	private String request;
	private ProcessAndArgsOrder pao;

	public TaskCGIResponse(Socket s, ProcessAndArgsOrder p, String req) {
		canal = s;
		request = req;
		try {
			out = s.getOutputStream();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		pao = p;
	}

	public void run() {
		try {

			String requestMethod = UtilsHTTP.getMethod(request);
			String resource = UtilsHTTP.getResource(request);

			ArrayList<String> cmdLine = new ArrayList<String>();
			cmdLine.add(pao.getProcess());

			if (requestMethod.equals("GET")) {
				HashMap<String, String> pGet = UtilsHTTP.getParamsGet(resource);
				// Add the args in the correct order
				for (String arg : pao.getArgumentOrder()) {
					cmdLine.add(pGet.get(arg));
				}
				UtilsHTTP.showParams(pGet);
				if (UtilsCGI.callProcess(out, cmdLine) != 0)
   			   UtilsHTTP.writeResponseServerError(new PrintWriter(out));

			} else if (requestMethod.equals("POST")) {
				InputStream in = canal.getInputStream();
				HashMap<String, String> pPost = UtilsHTTP.parseBody(UtilsHTTP
						.getBody(UtilsHTTP.getHeaders(in), in));
				// Add the args in the correct order
				for (String arg : pao.getArgumentOrder()){
					cmdLine.add(pPost.get(arg));
				}

				UtilsHTTP.showParams(pPost);

            if (UtilsCGI.callProcess(out, cmdLine) != 0)
      			 UtilsHTTP.writeResponseServerError(new PrintWriter(out));
				in.close();
			} else
				UtilsHTTP.writeResponseNotImplemented(new PrintWriter(out),
						requestMethod);

		} catch (Exception ex) {
			UtilsHTTP.writeResponseServerError(new PrintWriter(out));
			ex.printStackTrace();
		} finally {
			try {
				out.close();
				canal.close();
			} catch (Exception ex) {
			}
		}
	}
}