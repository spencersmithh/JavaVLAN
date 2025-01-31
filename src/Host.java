import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Host {
    String name;
    String[] id;
    String port;
    InetAddress ip;
    String[] neighbors;

    public static void main(String[] args) throws UnknownHostException {
        String testString = "Hello";
        Host host = new Host("hostA");
        String destinationMac = "hostB";

        String frameMessage = host.name+";"+destinationMac+";"+testString;

        byte[] bytes = host.convertStringToBytes(frameMessage);
        String convertedMessage = host.convertBytestoString(bytes);

    }
    public Host(String name) throws UnknownHostException {
        this.name = name;
        Parser parser = new Parser(name);
        this.id = parser.getID();
        this.port=id[1];
        this.ip = InetAddress.getByName(id[0]);

        this.neighbors = parser.getNeighbors();
    }
    private byte[] convertStringToBytes(String string){
        Charset UTF_8 = StandardCharsets.UTF_8;
        return string.getBytes(UTF_8);
    }
    private String convertBytestoString(byte[] bytes){
        String s = new String(bytes, StandardCharsets.UTF_8);
        return s;
    }
}
