package cs.edu.uv.http.dynamicresponse;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class TestHeaders {

    @Test
    public void test1() {
        StringBuilder header = new StringBuilder();
        header.append("Content-Disposition: form-data; name=\"ACTION\"\r\n\r\n");
        String param = MultipartUtils.getParamName(header.toString());
        
        assertEquals("ACTION", param);
    }

    @Test
    public void test2() {
        StringBuilder header2 = new StringBuilder();
        header2.append("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\"hello.o\"\r\n");
        header2.append("Content-Type: application/x-object\r\n\r\n");
        String name = MultipartUtils.getParamNameInFile(header2.toString());
        String file = MultipartUtils.getFileName(header2.toString());
        
        assertEquals("uploadedfile",name);
        assertEquals("hello.o", file);
    }


    @Test
    public void test3() {
        String f = MultipartUtils.generateRandomFileName("out.png");
        System.out.println(f);
        assumeTrue(f.contains(".png"));
    }



    @Test
    public void test4() {
        String f = MultipartUtils.generateRandomFileName("out");
        System.out.println(f);
        
        f = MultipartUtils.generateRandomFileName("out.png.pdf");
        System.out.println(f);
        
    }


    
}