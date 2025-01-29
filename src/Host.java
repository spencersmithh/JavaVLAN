import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Host {
    String name = "hostA";

    Parser parser = new Parser(name);
    //String[] id = parser.getID();
    //String port = id[1];
    //InetAddress ip = InetAddress.getByName(id[0]);

    String frameMessage;

    public static void main(String[] args) throws UnknownHostException {
        String testString = "Hello";
        Host host = new Host("hostA");
        
        //This code block just tests those two functions
        /*
        byte[] bytes = host.convertStringToBytes(testString);
        System.out.println(bytes);
        String outString = host.convertBytestoString(bytes);
        System.out.println(outString);
         */
    }

    public Host(String name) throws UnknownHostException {

    }

    public byte[] convertStringToBytes(String string){
        Charset UTF_8 = StandardCharsets.UTF_8;
        return string.getBytes(UTF_8);
    }
    private String convertBytestoString(byte[] bytes){
        String s = new String(bytes, StandardCharsets.UTF_8);
        return s;
    }
}
