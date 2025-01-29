import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Host {
    String name = "";

    Parser parser = new Parser(name);
    String[] id = parser.getID();
    String port = id[0];
    InetAddress ip = InetAddress.getByName(parser.getID()[0]);

    String frameMessage;

    public static void main(String[] args) {

    }

    public Host(String id) throws UnknownHostException {

    }

    private byte[] convertStringToBytes(String string){
        Charset UTF_8 = StandardCharsets.UTF_8;
        return string.getBytes(UTF_8);
    }
    private String convertBytesToBinary(byte[] bytes){
        StringBuilder binary = new StringBuilder();
        for (byte b : bytes){
            int val = b;
            for(int i=0;i<8;i++){
                binary.append((val&128)==0?0:1);
                val<<=1;
            }
            binary.append(' ');
        }
        return binary.toString();
    }
    private String encryptBinary(String OGBinary){
        return null;
    }
    private byte[] convertBinaryToBytes(String binary){
        byte[] newBytes = new BigInteger(binary, 2).toByteArray();
        return newBytes;
    }
}
