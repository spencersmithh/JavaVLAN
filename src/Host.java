import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Host{
    private static String name;
    private static String id;
    private static int port;
    private static InetAddress ip;
    private static String[] mac;
    private static String[] neighbors;
    private static DatagramSocket socket;
    private volatile boolean running = true;

    public Host(String name) throws UnknownHostException, SocketException {
        this.name = name;
        Parser parser = new Parser(name);
        id = parser.getID();
        port = parser.getPort();
        ip = parser.getIP();
        mac = parser.getMAC();
        socket = new DatagramSocket(port);

        neighbors = parser.getNeighbors();
    }

        static class listen implements Runnable {
            @Override
            public void run() {
                while (true) {
                    try {
                        byte[] buffer = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);

                        String inputFrame = new String(packet.getData(), 0, packet.getLength());

                        // Parse frame
                        String[] frameParts = inputFrame.split(";");
                        if (frameParts.length < 3) {
                            System.out.println("Frame has incorrect length");
                            continue;
                        }

                        System.out.println("Incoming Packet: ");
                        System.out.println(Arrays.toString(frameParts));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        static class send implements Runnable {
            @Override
            public void run() {
                String destinationMac;
                String message = "";

                while (true) {
                    Scanner keyInput = new Scanner(System.in);
                    System.out.println("To send a message, enter the destMAC(device name) and message separated by a space");
                    String userRequest = keyInput.nextLine();

                    destinationMac = userRequest.split(" ",2)[0];
                    message = userRequest.split(" ",2)[1];
                    String frameMessage = name +";"+ destinationMac + ";" + message;

                    byte[] frameBytes = convertStringToBytes(frameMessage);
                    Parser neighbor = getNeighborParser();

                    // START OF UDP IMPLEMENTATION

                    DatagramPacket request = null;
                    try {
                        request = new DatagramPacket(frameBytes, frameBytes.length, neighbor.getIP(), neighbor.getPort());
                        socket.send(request);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    System.out.println("The host named " + name + " has send a message to a device with the following MAC address: " + destinationMac);
                }
            }
        }

    public static void main(String[] args) throws SocketException, UnknownHostException {
        // START OF USER INPUT AND DEFINING VARIABLES
        if (args[0].isEmpty()){
            System.out.println("hostname/arguments not given in run configuration...");
            System.exit(1);
        }
        Host host = new Host(args[0]);
        System.out.println(name);

        ExecutorService es = Executors.newFixedThreadPool(2);
        Runnable threadListen = new listen();
        Runnable threadSend = new send();
        es.submit(threadListen);
        es.submit(threadSend);

    }

    public Object[] getMac() {
        return mac;
    }
    private DatagramSocket getSocket() {
        return socket;
    }
    private static Parser getNeighborParser() {
        return new Parser(neighbors[0]);
    }
    private static byte[] convertStringToBytes(String string){
        Charset UTF_8 = StandardCharsets.UTF_8;
        return string.getBytes(UTF_8);
    }

    private static String convertBytesToString(byte[] bytes){
        String s = new String(bytes, StandardCharsets.UTF_8);
        return s;
    }
}