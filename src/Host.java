import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

public class Host implements Runnable {
    private String name;
    private String id;
    private int port;
    private InetAddress ip;
    private Object[] mac;
    static String[] neighbors;
    private DatagramSocket socket;
    private volatile boolean running = true;

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

    private void processPacket(DatagramPacket packet) {
        System.out.println("Received packet from: " + packet.getAddress().getHostAddress());
        String data = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Data: " + data);
    }

    public void stop() {
        running = false;
        socket.close();
    }

    public static void main(String[] args) throws Exception {
        // START OF USER INPUT AND DEFINING VARIABLES
        String[] destinationMac = new String[2];
        String message = "";

        if (args[0].isEmpty()){
            System.out.println("hostname/arguments not given in run configuration...");
            System.exit(1);
        }
        String hostname = args[0];
        Host host = new Host(hostname);

        Scanner keyInput = new Scanner(System.in);
        System.out.println("Enter the destMAC (IP and port number) and message separated by a space");
        String userRequest = keyInput.nextLine();

        destinationMac[0] = userRequest.split(" ",3)[0];
        destinationMac[1] = userRequest.split(" ",3)[1];
        message = userRequest.split(" ",3)[2];
        String frameMessage = host.name + ";" + destinationMac[0] + " " + destinationMac[1] + ";" + message;

        byte[] frameBytes = host.convertStringToBytes(frameMessage);
        Parser neighbor = getNeighborParser();

        // START OF UDP IMPLEMENTATION
        try {
            DatagramSocket socket = host.getSocket();
            Thread thread = new Thread(host);
            thread.start();

            DatagramPacket request = new DatagramPacket(frameBytes, frameBytes.length, neighbor.getIP(), neighbor.getPort());
            socket.send(request);

            System.out.println("The host named " + hostname + " has send a message to a device with the following MAC address: " + destinationMac);

            while (host.running) { // listening...
                DatagramPacket reply = new DatagramPacket(new byte[1024], 1024);
                socket.receive(reply);
                String frame = new String(reply.getData(),0, reply.getLength());

                byte[] response = Arrays.copyOf(
                        reply.getData(),
                        reply.getLength()
                );

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
                if (dest.getMAC() == host.getMac()) {
                    host.processPacket(reply);
                }
            }

            host.stop();
            thread.join();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

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
    public Object[] getMac() {
        return mac;
    }
    private DatagramSocket getSocket() {
        return socket;
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
