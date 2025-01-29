import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Host {
    String name = "";
    int port = 0;
    String ip = "";

    //String id = parser.getID(name);
    //port = split from id
    //ip = split from id

    //String id = ip + ":" + port;

    String frameMessage;
    public Host(String id){

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
        return null;
    }
}
