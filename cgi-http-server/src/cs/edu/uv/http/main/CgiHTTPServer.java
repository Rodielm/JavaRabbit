package cs.edu.uv.http.main;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cs.edu.uv.http.cgiresponse.ProcessAndArgsOrder;
import cs.edu.uv.http.cgiresponse.TaskCGIResponse;
import cs.edu.uv.http.cgiresponse.UtilsCGI;
import cs.edu.uv.http.staticresponse.TaskStaticResponse;
import cs.edu.uv.http.staticresponse.UtilsHTTP;

/**
 * This is the class with the main method Juan Gutierrez Aguado (Dep.
 * Informatica, Univ. Valencia, Spain)
 */
class CgiHTTPServer {
	private static String path;
	private static String priv;
	private static String accessFile;
	private static int port;
	private static int nThreads;
	private static String cgiMappingsFile;

	/**
	 * This method reads properties from a configuration file. If the file does
	 * not exists then default values are used. The values are stored in the
	 * fields of this class
	 */
	private static void readProperties() {
		try {
			Properties p = new Properties();
			p.load(new FileInputStream("config.ini"));
			path = p.getProperty("PATH", "/var/web/");
			priv = p.getProperty("AUTH_PREFIX", "private");
			accessFile = p.getProperty("ACCESS_FILE", ".access");
			nThreads = Integer.parseInt(p.getProperty("NUM_THREADS", "50"));
			port = Integer.parseInt(p.getProperty("SERVER_PORT", "8080"));
			cgiMappingsFile = p.getProperty("CGI_MAPPINGS_FILE",
					"cgi_mappings.txt");
			p = null;
		} catch (Exception ex) {
			System.out
					.println("You can provide a config.ini file to configure the server.");
			System.out.println("PATH=path");
			System.out.println("AUTH_PREFIX=prefix");
			System.out.println("ACCESS_FILE=file");
			System.out
					.println("NUM_THREADS=maximum numer of concurrent threads");
			System.out.println("SERVER_PORT=port");
			System.out.println("CGI_MAPPINGS_FILE=file");
		}

		System.out
				.println("-----  THIS SERVER WAS DESIGNED FOR TEACHING OR RESEARCH PURPOSES ----------");
		System.out
				.println("-----  IT CANNOT BE USED IN PRODUCTION ENVIRONMENTS ------------");
		System.out
				.println("------ Computer Science Dept - Univ. Valencia (Spain) ----------");
		System.out.println();

		System.out.println("Server configuration values are:");

		System.out.println("Resource location: " + path);
		System.out
				.println("Resources that require user and password are located"
						+ "in a directory that starts with : " + priv);
		System.out.println("Default file with user;pass : " + accessFile);
		System.out.println("Maximum number of concurrent requests attended: "
				+ nThreads);
		System.out.println("Default server port :" + port);
		System.out.println("CGI mappings file :" + cgiMappingsFile);

	}

	public static void main(String[] args) {

		readProperties();

		try {
			ServerSocket s = new ServerSocket(port);

			ExecutorService ex = Executors.newFixedThreadPool(nThreads);

			HashMap<String, ProcessAndArgsOrder> mapsCGI;

			Socket canal = null;
			while (true) {
				try {
					// To allow changes in the mappings even when the server is
					// running
					mapsCGI = UtilsCGI.getCGIMappings(cgiMappingsFile);

					canal = s.accept();

					System.out.println("---- New request from : "
							+ canal.getInetAddress().toString());

					InputStream in = canal.getInputStream();

					String request = UtilsHTTP.readLine(in);

					System.out.println("------- " + request);

					String rec = UtilsHTTP.getResource(request);

					ProcessAndArgsOrder p = null;

					boolean done = false;

					if (mapsCGI.size() > 0) {
						// Search if the requested resource starts with the URL
						// defined in the CGI mappings file
						Set<String> sm = mapsCGI.keySet();
						for (String m : sm)
							if (rec.startsWith(m))
								p = mapsCGI.get(m);

						if (p != null) {
							// Dinamic CGI response
							System.out.println("     CGI");
							ex.execute(new TaskCGIResponse(canal, p, request));
							done = true;
						}
					}
					if (!done) {
						// Static response
						System.out.println("    STATIC");
						ex.execute(new TaskStaticResponse(canal, path, priv,
								accessFile, request));
					}
				} catch (Exception exc) {
					PrintWriter pw = new PrintWriter(canal.getOutputStream());
					UtilsHTTP.writeResponseServerError(pw);
					pw.close();
					canal.close();
					exc.printStackTrace();
				}
			}

		} catch (Exception ex) {
			System.out.println("Error launching the server");
			ex.printStackTrace();
		}
	}
}
