package cs.edu.uv.http.staticresponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.bind.DatatypeConverter;

/**
 * This class offers some helper static methods that can be used to read the
 * request and to write the response in an HTTP server
 * 
 * @author Juan Gutierrez Aguado (Dep. Informatica, Univ. Valencia, Spain)
 */
public class UtilsHTTP {
	private static final String OK = "HTTP/1.1 200 OK";
	private static final String REDIR = "HTTP/1.1 301 Moved Permanently";
	private static final String NOTMODIFIED = "HTTP/1.1 304 Not Modified";
	private static final String BADREQUEST = "HTTP/1.1 400 Bad Request";
	private static final String UNAUTHORIZED = "HTTP/1.1 401 Authorization Required";
	private static final String FORBIDDEN = "HTTP/1.1 403 Forbidden";
	private static final String NOTFOUND = "HTTP/1.1 404 Not Found";
	private static final String NOTALLOWED = "HTTP/1.1 405 Method Not Allowed";
	private static final String INTERNALERROR = "HTTP/1.1 500 Internal Server Error";
	private static final String NOTIMPLEMENTED = "HTTP/1.1 501 Not Implemented";
	private static final String START_HTML = "<!DOCTYPE html>\n<html>\n<body>\n";
	private static final String FINAL_HTML = "\n</body>\n</html>";

	/**
	 * This method reads a line. The line ends with \r\n
	 * 
	 * @param in
	 *            the stream
	 * @return the line
	 * @throws IOException
	 */
	public static String readLine(InputStream in) throws IOException {
		int b;
		int estado = 0;
		StringBuffer sb = new StringBuffer();
		while ((estado < 2) && ((b = in.read()) != -1)) {
			char c = (char) b;
			if ((c == '\r') && (estado == 0))
				estado = 1;
			if ((c == '\n') && (estado == 1))
				estado = 2;
			if (estado == 0)
				sb.append(c);
		}
		return sb.toString();

	}

	/**
	 * This method returns the request type
	 * 
	 * @param linea
	 *            the first line in the request
	 * @return the type of the request
	 */
	public static String getMethod(String linea) {
		String method = null;
		if (linea.startsWith("GET"))
			method = "GET";
		else if (linea.startsWith("POST"))
			method = "POST";
		else if (linea.startsWith("PUT"))
			method = "PUT";
		else if (linea.startsWith("HEAD"))
			method = "HEAD";
		else if (linea.startsWith("DELETE"))
			method = "DELETE";
		else if (linea.startsWith("CONNECT"))
			method = "CONNECT";
		return method;
	}

	/**
	 * This method extract the resource from the first line of the request
	 * 
	 * @param linea
	 *            the first line in the request
	 * @return the resource
	 */
	public static String getResource(String linea) {
		String recurso = null;
		try {
			StringTokenizer st = new StringTokenizer(linea, " ");
			st.nextToken();
			recurso = st.nextToken();
			recurso = recurso.substring(1, recurso.length());
		} catch (Exception ex) {
		}
		return recurso;
	}

	/**
	 * This method obtains all the headers of the request
	 * 
	 * @param in
	 *            the input stream
	 * @return all the headers as a HashMap<String,String>
	 * @throws Exception
	 */
	public static HashMap<String, String> getHeaders(InputStream in)
			throws Exception {
		String line;

		HashMap<String, String> headers = new HashMap<String, String>();

		while (!(line = readLine(in)).equals("")) {
			int pos = line.indexOf(':');
			headers.put(line.substring(0, pos),
					line.substring(pos + 1, line.length()));
		}

		return headers;
	}

	/**
	 * This method prints all the headers
	 * 
	 * @param headers
	 */
	public static void showHeaders(HashMap<String, String> headers) {
		System.out.println("Cabeceras de la peticion: ");
		Set<String> claves = headers.keySet();

		for (String c : claves)
			System.out.println(c + ": " + headers.get(c));
	}

	/**
	 * This method obtains the value of the header Content-Length and converts
	 * it to an int
	 * 
	 * @param cab
	 *            the headers
	 * @return the value of the header Content-Length
	 */
	private static int getContentLength(HashMap<String, String> cab) {
		String tam = cab.get("Content-Length");
		return Integer.parseInt(tam.trim());
	}

	/**
	 * This method extracts the parameters from the URL of a GET message
	 * 
	 * @param s
	 * @return
	 */
	public static HashMap<String, String> getParamsGet(String s) {
		HashMap<String, String> params = new HashMap<String, String>();

		try {
			String[] result = s.split("\\?");
			String p = result[1];
			String[] pairs = p.split("\\&");

			for (int x = 0; x < pairs.length; x++) {
				String[] par = pairs[x].split("=");
				params.put(par[0], URLDecoder.decode(par[1], "utf-8"));
			}
		} catch (Exception ex) {
			
		}
		return params;
	}

	/**
	 * 
	 * @param cab
	 * @param in
	 * @return
	 */
	public static byte[] getBody(HashMap<String, String> cab, InputStream in) {
		int tam = getContentLength(cab);
		byte[] body = null;

		if (tam != -1) {

			body = new byte[tam];
			try {
				in.read(body, 0, tam);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return body;
	}

	/**
	 * This method extracts the parameters from the request body of a POST
	 * message
	 * 
	 * @param body
	 *            the body of the request
	 * @return a HashMap with <param,value>
	 */
	public static HashMap<String, String> parseBody(byte[] body) {
		HashMap<String, String> params = null;
		try {
			String s = new String(body);
			params = new HashMap<String, String>();
			String[] result = s.split("&");
			for (int x = 0; x < result.length; x++) {
				String[] par = result[x].split("=");
				params.put(par[0], URLDecoder.decode(par[1], "utf-8"));
			}
		} catch (Exception ex) {
			
		}
		// System.out.println(params);
		return params;
	}

	/**
	 * This method prints all the parameters of the request. This is for debug
	 * (or verbose) scenarios.
	 * 
	 * @param params
	 *            params to be shown in System.out
	 */
	public static void showParams(HashMap<String, String> params) {
		Set<String> keys = params.keySet();
		for (String k : keys)
			System.out.println(k + "=" + params.get(k));
	}

	/**
	 * This method tests if the resource is in a path that requires
	 * authorization
	 * 
	 * @param linea
	 *            the first line of the request
	 * @param authPrefix
	 *            the prefix of the paths that require authorization
	 * @return true if requires authorization, false otherwise
	 */
	public static boolean requiresAuthentication(String linea, String authPrefix) {
		return linea.contains(authPrefix);
	}

	/**
	 * This method decodes Base64 and obtains the user
	 * 
	 * @param auth
	 *            the Base64 encoded Authorization
	 * @return the user
	 */
	public static String getUser(String auth) {
		String line = new String(DatatypeConverter.parseBase64Binary(auth));
		String[] data = line.split(":");
		return data[0];
	}

	/**
	 * This method decodes Base64 and obtains the password
	 * 
	 * @param auth
	 *            the Base64 encoded Authorization
	 * @return the password
	 */
	public static String getPass(String auth) {
		// Esto es para conversion a Base64
		// String line = DatatypeConverter.printBase64Binary(auth.getBytes());
		String line = new String(DatatypeConverter.parseBase64Binary(auth));
		String[] data = line.split(":");
		return data[1];
	}

	/**
	 * This method tests if the request is asking for the password file
	 * 
	 * @param linea
	 *            the first line in the request
	 * @param passFile
	 *            the name of the password file
	 * @return
	 */
	public static boolean isPassFile(String linea, String passFile) {
		return linea.contains(passFile);
	}

	/**
	 * This method returns the full path of the password file
	 * 
	 * @param recurso
	 *            the resource of the request
	 * @param path
	 *            the path where is located all the server pages
	 * @param passFileName
	 *            the name of the file that stores the user and password
	 * @return the full path to the user/password file
	 */
	public static String getPathToPassFile(String recurso, String path,
			String passFileName) {
		String pathPassFile = path;
		try {
			StringTokenizer st = new StringTokenizer(recurso, "/");
			String app = st.nextToken();
			pathPassFile = path + app + "/" + passFileName;

		} catch (Exception ex) {
		}
		return pathPassFile;
	}

	/**
	 * This method returns the date in a format suitable for the response header
	 * Date
	 * 
	 * @return
	 */
	private static String getDate() {
		String formato = "EEE, dd MMM yyyy HH:mm:ss zzz";
		return (new SimpleDateFormat(formato).format(new Date()));
	}

	/**
	 * This method writes the response headers in the output stream. After
	 * writing the headers outputs an empty line "\r\n"
	 * 
	 * @param pw
	 *            the output stream
	 * @param hm
	 *            the headers
	 */
	public static void writeHeaders(PrintWriter pw, HashMap<String, String> hm) {
		for (String key : hm.keySet()) {
			pw.print(key);
			pw.print(": ");
			pw.print(hm.get(key));
			pw.print('\r');
			pw.print('\n');
		}
		pw.print("Date: " + getDate());
		pw.print('\r');
		pw.print('\n');
		pw.print("Server: Lightweight server for teaching purposes");
		pw.print('\r');
		pw.print('\n');

		// This is the empty line between headers and the body
		pw.print('\r');
		pw.print('\n');

		pw.flush();
	}

	/**
	 * This method writes the first line of the response)
	 * 
	 * @param pw
	 *            the output stream
	 */

	public static void writeResponseLine(PrintWriter pw, String msg) {
		pw.print(msg);
		pw.print("\r\n");
		pw.flush();
	}

	private static String buildPredefinedResponse(String msg) {
		StringBuffer sb = new StringBuffer();
		sb.append(UtilsHTTP.START_HTML);
		sb.append(msg);
		sb.append(UtilsHTTP.FINAL_HTML);
		return sb.toString();
	}

	/**
	 * This method writes the response telling the browser that user/password is
	 * needed
	 * 
	 * @param pw
	 *            the output stream
	 */
	public static void writeResponseRequiresAuthorization(PrintWriter pw) {
		writeResponseLine(pw, UtilsHTTP.UNAUTHORIZED);
		String message = buildPredefinedResponse("<h1> Authentication is required </h1>");
		HashMap<String, String> responseCabs = new HashMap<String, String>();
		responseCabs
				.put("WWW-Authenticate", "Basic realm=\"Private resource\"");
		responseCabs.put("Content-Length", message.length() + "");
		responseCabs.put("Content-Type", "text/html; charset=utf-8");
		writeHeaders(pw, responseCabs);
		pw.print(message);
		pw.flush();
	}

	/**
	 * This method writes a response that informs the user that the resource is
	 * not public
	 * 
	 * @param pw
	 *            the output stream
	 */
	public static void writeForbidden(PrintWriter pw) {
		writeResponseLine(pw, UtilsHTTP.FORBIDDEN);
		String message = buildPredefinedResponse("The server understands your request but <br /> "
				+ "that action is <strong>forbidden</strong>");
		HashMap<String, String> responseCabs = new HashMap<String, String>();
		responseCabs.put("Content-Length", message.length() + "");
		responseCabs.put("Status", "403 Forbidden");
		responseCabs.put("Content-Type", "text/html; charset=utf-8");
		writeHeaders(pw, responseCabs);
		pw.print(message);
		pw.flush();
	}

	/**
	 * This method writes a response that informs the user that the resource is
	 * not found in the server
	 * 
	 * @param pw
	 *            the output stream
	 * @param recurso
	 *            the resource requested
	 */
	public static void writeResponseNotFound(PrintWriter pw, String recurso) {
		writeResponseLine(pw, UtilsHTTP.NOTFOUND);
		StringBuffer sb = new StringBuffer();
		sb.append("<h1> That resource <b>");
		sb.append("cannot be found");
		sb.append(" </b> in this server </h1>");
		String message = buildPredefinedResponse(sb.toString());
		int tam = message.getBytes().length;
		HashMap<String, String> responseCabs = new HashMap<String, String>();
		responseCabs.put("Content-Length", tam + "");
		responseCabs.put("Content-Type", "text/html");
		writeHeaders(pw, responseCabs);
		pw.print(message);
		pw.flush();
	}

	/**
	 * This method writes a response that informs the user that there has been
	 * an internal error in the server
	 * 
	 * @param pw
	 *            the output stream
	 */
	public static void writeResponseServerError(PrintWriter pw) {
		writeResponseLine(pw, UtilsHTTP.INTERNALERROR);
		String message = buildPredefinedResponse("</h1> Internal server error! </h1>");
		int tam = message.getBytes().length;
		HashMap<String, String> responseCabs = new HashMap<String, String>();
		responseCabs.put("Content-Length", tam + "");
		responseCabs.put("Content-Type", "text/html");
		writeHeaders(pw, responseCabs);
		pw.print(message);
		pw.flush();
	}

	/**
	 * This method writes a response that informs the user that the HTTP method
	 * is not implemented
	 * 
	 * @param pw
	 *            the output stream
	 * @param method
	 *            the HTTP method in the request
	 */
	public static void writeResponseNotImplemented(PrintWriter pw, String method) {
		writeResponseLine(pw, UtilsHTTP.NOTIMPLEMENTED);
		StringBuffer sb = new StringBuffer();
		sb.append("<h1> ");
		sb.append(method);
		sb.append(" is not implemented in this server </h1>");
		String message = buildPredefinedResponse(sb.toString());
		int tam = message.getBytes().length;
		HashMap<String, String> responseCabs = new HashMap<String, String>();
		responseCabs.put("Content-Length", tam + "");
		responseCabs.put("Content-Type", "text/html");
		writeHeaders(pw, responseCabs);
		pw.print(message);
		pw.flush();
	}

	/**
	 * This method is used to add to the response header the Content-Type of the
	 * resource
	 * 
	 * @param responseCabs
	 *            the response headers
	 * @param recurso
	 *            the resource being requested
	 */
	public static void addResponseHeaderContentType(
			HashMap<String, String> responseCabs, String recurso) {
		String extension = recurso.substring(recurso.lastIndexOf('.') + 1,
				recurso.length());

		// Some MIME types (this is just a sample)
		if (extension.equals("html"))
			responseCabs.put("Content-Type", "text/html");
		else if (extension.equals("png"))
			responseCabs.put("Content-Type", "image/png");
		else if (extension.equals("jpg"))
			responseCabs.put("Content-Type", "image/jpeg");
		else if (extension.equals("js"))
			responseCabs.put("Content-Type", "text/javascript");
		else if (extension.equals("css"))
			responseCabs.put("Content-Type", "text/css");
		else if (extension.equals("pdf"))
			responseCabs.put("Content-Type", "application/pdf");
		else if (extension.equals("json"))
			responseCabs.put("Content-Type", "text/json");
		else if (extension.equals("txt"))
			responseCabs.put("Content-Type", "text/plain");
		else if (extension.equals("java"))
			responseCabs.put("Content-Type", "text/plain");

	}

	/**
	 * This method writes the status line, the response headers and the resource
	 * 
	 * @param pw
	 *            the output stream (as a PrintWriter)
	 * @param out
	 *            the output stream (as an OutputStream)
	 * @param recurso
	 *            the resource
	 */

	public static void writeResource(PrintWriter pw, OutputStream out,
			String recurso) {
		File f = new File(recurso);

		System.out.println("Path of the resource: " + f.getAbsolutePath());

		if (f.isDirectory())
			UtilsHTTP.writeForbidden(pw);
		else if (!f.exists())
			UtilsHTTP.writeResponseNotFound(pw, recurso);
		else

			try {
				long tam = f.length();

				UtilsHTTP.writeResponseLine(pw, UtilsHTTP.OK);

				HashMap<String, String> responseCabs = new HashMap<String, String>();
				responseCabs.put("Content-Length: ", tam + "");

				UtilsHTTP.addResponseHeaderContentType(responseCabs, recurso);

				UtilsHTTP.writeHeaders(pw, responseCabs);

				FileInputStream inFile = new FileInputStream(recurso);

				// Read and write in chunks of 1024 bytes
				byte[] data = new byte[1024];
				int readed;

				while ((readed = inFile.read(data, 0, 1024)) > 0)
					out.write(data, 0, readed);

				out.flush();
				inFile.close();

				System.out.println("---------- RESPONSE ENDS -----");
			} catch (Exception ex) {
				UtilsHTTP.writeResponseServerError(pw);
			}
	}
}