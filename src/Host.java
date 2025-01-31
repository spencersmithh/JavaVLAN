import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Host {
    String name;
    String id;
    Integer port;
    InetAddress ip;
    static String[] neighbors;

    public static void main(String[] args) throws Exception {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter host name(ex. \"hostA\"): ");
        String hostname = input.nextLine();
        Host host = new Host(hostname);

        System.out.println("Enter destination name/MAC(ex. \"hostB\"): ");
        String destinationMac = input.nextLine();

        System.out.println("Enter packet message ");
        String message = input.nextLine();

        String frameMessage = host.name+";"+destinationMac+";"+message;

        byte[] frameBytes = host.convertStringToBytes(frameMessage);
        Parser neighbor = getNeighborParser();

        DatagramSocket socket = new DatagramSocket();
        DatagramPacket request = new DatagramPacket(frameBytes, frameBytes.length, neighbor.getIP(), neighbor.getPort());
        socket.send(request);
        DatagramPacket reply = new DatagramPacket(new byte[1024], 1024);
        socket.receive(reply);
        socket.close();

        //This is a partial implementation of the client part, may not fully work
        //TODO send frameBytes to neighbor, Listen for incoming frames

    }
    public Host(String name) throws UnknownHostException {
        this.name = name;
        Parser parser = new Parser(name);
        this.id = parser.getID();
        this.port = parser.getPort();
        this.ip = parser.getIP();

        this.neighbors = parser.getNeighbors();
    }
    private static Parser getNeighborParser() {
        return new Parser(neighbors[0]);
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
