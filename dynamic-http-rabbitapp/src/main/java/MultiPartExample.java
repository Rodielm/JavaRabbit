
import java.io.PrintWriter;
import java.io.StringWriter;
import cs.edu.uv.http.dynamicresponse.MultipartUtils;
import cs.edu.uv.http.dynamicresponse.ResponseClass;
import cs.edu.uv.http.dynamicresponse.ThingsAboutRequest;
import cs.edu.uv.http.dynamicresponse.ThingsAboutResponse;

public class MultiPartExample extends ResponseClass {
	public void ifPost(ThingsAboutRequest req, ThingsAboutResponse resp) throws Exception {

		
		try {
			if (MultipartUtils.isMultipartFormData(req.getHeaders())) {
				MultipartUtils multipartUtils = new MultipartUtils(req);				
				// String procesado = multipartUtils.getFormFieldMultipart("procesado");
				// System.out.println(procesado);		
				// multipartUtils.saveFormFileMultipart("image", "/tmp/out.png");
				resp.flushResponseHeaders();
				PrintWriter pw = resp.getWriter();
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
