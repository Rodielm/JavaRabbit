package cs.edu.uv.http.dynamicresponse;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.fileupload.MultipartStream;

/**
 * This class processes the multipart body of a request
 */
public class MultipartUtils {
	private ThingsAboutRequest request;

	/**
	 * Constructor
	 * 
	 * @param req the object that encapsulates information about the request
	 * @throws Exception
	 */
	public MultipartUtils(ThingsAboutRequest req) throws Exception {
		System.out.println("Creating MultipartUtils object");
		this.request = req;
	}

	/**
	 * To check if the request is of type multipart (Content-Type header)
	 * 
	 * @param headers all the headers in the request
	 * @return
	 */
	public static boolean isMultipartFormData(HashMap<String, String> headers) {
		return headers.get("Content-Type").contains("multipart");
	}

	/**
	 * Obtains the boundary
	 * 
	 * @return the boundary
	 */
	private String getBoundary() {
		String ct = request.getHeader("Content-Type");
		String boundary = ct.split(";")[1].split("=")[1].trim();
		return boundary;
	}

	static String getParamName(String header) {
		Pattern pattern = Pattern.compile("(.*?); name=\\\"(.*?)\\\"\\r\\n\\r\\n");
		Matcher matcher = pattern.matcher(header);
		String param = null;
		if (matcher.matches())
			param = matcher.group(2);
		return param;
		
	}
	
	static String getParamNameInFile(String header) {
		Pattern pattern = Pattern.compile("(.*?); name=\\\"(.*?)\\\".*\\r\\n.*\\r\\n\\r\\n");
		Matcher matcher = pattern.matcher(header);
		String param = null;
		if (matcher.matches())
			param = matcher.group(2);
		return param;
		
	}

	static String getFileName(String header) {
		Pattern pattern = Pattern.compile("(.*?); filename=\\\"(.*?)\\\"\\r\\n.*\\r\\n\\r\\n");
		Matcher matcher = pattern.matcher(header);	
		String param = null;
		if (matcher.matches())
			param = matcher.group(2);
		return param;
	}

    static String generateRandomFileName(String fileName){
		String[] data = fileName.split("\\.");
		String fileExt="";
		if (data.length > 1){
		   // Assume that the last token is the extension of the file				
		   fileExt = data[data.length-1];
		}
		String newFileName = UUID.randomUUID().toString();
		String fileWithExt = newFileName;
		if (!fileExt.equals(""))
		   fileWithExt = newFileName + "." + fileExt;
		return fileWithExt;
	}

	public void parseMultipart(HashMap<String, String> fields, HashMap<String, String> files, String path)
			throws Exception {
		MultipartStream multipartStream = new MultipartStream(request.getInputStream(), getBoundary().getBytes(), 4096,
				null);
		boolean nextPart = multipartStream.skipPreamble();

		while (nextPart) {
			String header = multipartStream.readHeaders();
			System.out.println(header);
			if (!header.contains("filename")) {
				String param = getParamName(header);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				multipartStream.readBodyData(out);
				String fieldValue = new String(out.toByteArray());
				fields.put(param, fieldValue);
				System.out.println("Stored parameter from multipart <" +param +"," + fieldValue+ ">");
			} else {
				String param = getParamNameInFile(header);				
				String file = getFileName(header);
				String newFileWithExt = generateRandomFileName(file);
				if (!path.endsWith("/"))
					path = path + "/";
				OutputStream out = new BufferedOutputStream(new FileOutputStream(path + newFileWithExt));
				multipartStream.readBodyData(out);
				out.flush();
				out.close();
				files.put(param, newFileWithExt);
				System.out.println("Stored file from multipart <" +param +"," + newFileWithExt + ">");
			}
			nextPart = multipartStream.readBoundary();
		}

	}
}