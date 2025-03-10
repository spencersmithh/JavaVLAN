import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;

public class Router {
    public static void main(String[] args) throws Exception {
        /*
        HashMap<String, String[]> R1Table = new HashMap<>();
        R1Table.put("net1", new String[]{"3007" , "yes"});
        R1Table.put("net2", new String[]{"3008" , "yes"});
        R1Table.put("net3", new String[]{"net2.R2" , "no"});
        */

        if(args.length < 1){
            System.out.println("Switch name not provided in arguments...");
            System.exit(1);
        }

        String routerID = args[0];
        Parser parser = new Parser(routerID);
        InetAddress routerIP = parser.getRouterIP();
        int routerPort = parser.getRouterPort();

        String[] neighbors = parser.getNeighbors();

        try {
            while (true) {
                DatagramSocket socket = new DatagramSocket(parser.getPort());
                DatagramPacket frameRequest = new DatagramPacket(new byte[1024], 1024);

                socket.receive(frameRequest);
                String frame = new String(frameRequest.getData(), 0, frameRequest.getLength());
                System.out.println("Frame received: " + frame);
                int srcPort = frameRequest.getPort();

                // Parse frame
                String[] frameParts = frame.split(";");
                if (frameParts.length < 3) {
                    System.out.println("Frame has incorrect length");
                    continue;
                }

                Parser src = new Parser(frameParts[0]);
                Parser dest = new Parser(frameParts[1]);

                // re-create the frame based on the router configurations

                // checks table to see if IP address of destination is known

                //

                socket.close();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
