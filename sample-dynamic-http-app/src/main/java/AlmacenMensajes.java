
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

class Mensajes {
	public static String leeMensajes() {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream("/var/web/app-data/mensajes.txt"), "UTF-8"));
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

	public static String leeMensajesJson() {
		ArrayList<Mensaje> mensajes = new ArrayList();
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream("/var/web/app-data/mensajes.txt"), "UTF-8"));
			String cad;
			int i = 0;
			while ((cad = br.readLine()) != null) {
				i += 1;
				mensajes.add(new Mensaje(i,cad));
			}
			br.close();
		} catch (Exception ex) {
		}
		Gson gson = new Gson();
		String json = gson.toJson(mensajes);
		return json;
	}

	public static void almacenaMensaje(String msg) throws Exception {
		PrintWriter pw = new PrintWriter(new FileWriter("/var/web/app-data/mensajes.txt", true));
		pw.println(msg);
		pw.flush();
		pw.close();
		if (pw.checkError()) {
			throw new Exception();
		}

	}
}

class Mensaje{
	private int id;
	private String text;
	
	Mensaje(int id,String text){
		this.id = id;
		this.text = text;
	}

	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
	
	public void setText(String text){
		this.text = text;
	}
	
	public String getText(){
		return text;
	}

	
}

public class AlmacenMensajes extends ResponseClass {

	public void ifGet(ThingsAboutRequest req, ThingsAboutResponse resp) throws Exception {
		OutputStream out = resp.getOutputStream();
		PrintWriter pw = new PrintWriter(out);

		String param = req.getHeader("Accept").trim();
		
		if (param.equals("application/json")) {
			resp.setResponseHeader("Content-Type", "application/json; charset=utf-8");
			resp.flushResponseHeaders();
			pw.println(Mensajes.leeMensajesJson());
		} else {
			resp.setResponseHeader("Content-Type", "text/html; charset=utf-8");
			resp.flushResponseHeaders();
			pw.println("<h1> Mensajes en el servidor</h1>");
			pw.println(Mensajes.leeMensajes());
		}
		pw.flush();
		pw.close();

	}

	public void ifPost(ThingsAboutRequest req, ThingsAboutResponse resp) throws Exception {
		String msg = req.getParam("mensaje");
		try {
			Mensajes.almacenaMensaje(msg);
			resp.setStatus(200);
			resp.flushResponseHeaders();
		} catch (Exception ex) {
			resp.setStatus(500);
			resp.flushResponseHeaders();
		}

	}

}
