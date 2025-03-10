import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;

public class Router {
    public static void main(String[] args) throws Exception {
        if(args.length < 1){
            System.out.println("Switch name not provided in arguments...");
            System.exit(1);
        }

        String switchID = args[0];
        Parser parser = new Parser(switchID);

        String[] srcNeighbors = parser.getNeighbors();

        // switch table format = {"macName":"IP:Port"}
        HashMap<String, String[]> switchTable = new HashMap<>();

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
                // Parser dest = new Parser(frameParts[1]);

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
