
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;

import com.google.gson.Gson;

import cs.edu.uv.http.dynamicresponse.ResponseClass;
import cs.edu.uv.http.dynamicresponse.ThingsAboutRequest;
import cs.edu.uv.http.dynamicresponse.ThingsAboutResponse;

import java.io.*;

class Records {
	
	public static String readRecord() {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream("/var/web/app-data/jCompletionRecords.txt"), "UTF-8"));
			String cad;
			while ((cad = br.readLine()) != null) {
				sb.append(cad);
				sb.append("</br>");
			}
			br.close();
		} catch (Exception ex) {
		}
		return sb.toString();
	}
}

public class PublishRecords extends ResponseClass {

	public void ifGet(ThingsAboutRequest req, ThingsAboutResponse resp) throws Exception {
		OutputStream out = resp.getOutputStream();
		PrintWriter pw = new PrintWriter(out);

		resp.setResponseHeader("Content-Type", "text/html; charset=utf-8");
		resp.flushResponseHeaders();
		pw.println("<h1> Workers RabbitMQ</h1>");
		pw.println(Records.readRecord());
		pw.flush();
		pw.close();

	}
}
