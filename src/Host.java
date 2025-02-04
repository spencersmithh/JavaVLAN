import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

public class Host {
    String name;
    String id;
    Integer port;
    InetAddress ip;
    String mac;
    static String[] neighbors;

    /*
    public void run() {
        while (true) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                processPacket(packet);
            } catch (IOException e) {
                if (!running) {
                    break;
                }
                e.printStackTrace();
            }
        }
    }

     */

    public static void main(String[] args) throws Exception {
        String destinationMac = "";
        String message = "";

        if (args[0].isEmpty()){
            System.out.println("hostname/arguments not given in run configuration...");
            System.exit(1);
        }
        String hostname = args[0];
        Host host = new Host(hostname);

        Scanner keyInput = new Scanner(System.in);
        System.out.println("Enter the destMAC and message separated by a space");
        String userRequest = keyInput.nextLine();

        destinationMac = userRequest.split(" ",3)[0] + " " + userRequest.split(" ",3)[1];
        message = userRequest.split(" ",3)[2];

        String frameMessage = host.name + ";" + destinationMac + ";" + message;

        byte[] frameBytes = host.convertStringToBytes(frameMessage);
        Parser neighbor = getNeighborParser();

        DatagramSocket socket = new DatagramSocket();
        DatagramPacket request = new DatagramPacket(frameBytes, frameBytes.length, neighbor.getIP(), neighbor.getPort());
        socket.send(request);

        System.out.println("The host named " + hostname + " has send a message to a device with the following MAC address: " + destinationMac);

        while (true) { // listening...
            DatagramPacket reply = new DatagramPacket(new byte[1024], 1024);
            socket.receive(reply);
            String frame = new String(reply.getData(),0, reply.getLength());

            byte[] response = Arrays.copyOf(
                    reply.getData(),
                    reply.getLength()
            );

            System.out.println(host.convertBytesToString(response));
            System.out.println("Frame received: " + frame);

            // Parse frame
            String[] frameParts = frame.split(";");
            if (frameParts.length < 3){
                System.out.println("Frame has incorrect length");
                continue;
            }

            Parser src = new Parser(frameParts[0]);
            Parser dest = new Parser(frameParts[1]);

            // TODO: test if the destination MAC matches the current host MAC.
            //      if it does, print out the message.
            //      if it doesn't, ignore.

            // somehow figure out where to put the sock.close() statement where it's not unreachable.
        }
        // socket.close();
    }

    public Host(String name) throws UnknownHostException {
        this.name = name;
        Parser parser = new Parser(name);
        id = parser.getID();
        port = parser.getPort();
        ip = parser.getIP();
        mac = parser.getMAC();

        neighbors = parser.getNeighbors();
    }
    private static Parser getNeighborParser() {
        return new Parser(neighbors[0]);
    }
    private byte[] convertStringToBytes(String string){
        Charset UTF_8 = StandardCharsets.UTF_8;
        return string.getBytes(UTF_8);
    }

    private String convertBytesToString(byte[] bytes){
        String s = new String(bytes, StandardCharsets.UTF_8);
        return s;
    }
}
