import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;

public class Switch {
    public static void main(String[] args) throws Exception {
        if(args.length < 1){
            System.out.println("Switch name not provided in arguments...");
            System.exit(1);
        }

        String switchID = args[0];
        Parser parser = new Parser(switchID);

        int portNumber = Integer.parseInt(parser.getID()[1]);

        String[] srcNeighbors = parser.getNeighbors();

        HashMap<String, String> switchTable = new HashMap<>();

        DatagramSocket socket = new DatagramSocket(portNumber);
        DatagramPacket frameRequest = new DatagramPacket(new byte[1024], 1024);

        while (true) {
            socket.receive(frameRequest);
            String frame = new String(frameRequest.getData(),0, frameRequest.getLength());
            System.out.println("Frame received: " + frame);

            String[] frameParts = frame.split(";");
            if (frameParts.length < 3){
                System.out.println("Frame has incorrect length");
                continue;
            }

            String srcMAC = frameParts[0];
            String destMAC = frameParts[1];
            String message = frameParts[2];


            String destIP = destMAC.split(",")[0];
            String destPort = destMAC.split(",")[1];

            String srcIP = srcMAC.split(",")[0];
            String srcPort = srcMAC.split(",")[1];

            if (!switchTable.containsKey(srcMAC)){
                switchTable.put(srcIP,srcPort);
                System.out.println("added " + srcMAC+ " to hashmap");
            }

            if (switchTable.containsKey(destIP)){
                System.out.println("destMac Known. forwarding packet...");
                byte[] response = frame.getBytes();
                InetAddress forwardingAddress = InetAddress.getByName(destIP);
                DatagramPacket forwardPacket = new DatagramPacket(response, response.length, forwardingAddress,Integer.parseInt(destPort));
                socket.send(forwardPacket);
                System.out.println("packet forwarded to: "+destIP + ":" + destPort);

            } else {
                System.out.println("destIP not known starting flood...");
                for (String neighbor : srcNeighbors){
                    Parser newNeighborParser = new Parser(neighbor);
                    String[] newNeighborMAC = newNeighborParser.getID();
                    InetAddress newNeighborIP = InetAddress.getByName(newNeighborMAC[0]);

                    if (!newNeighborMAC[0].equals(srcIP)){
                        DatagramPacket flooder = new DatagramPacket(frame.getBytes(), frame.length(), newNeighborIP,Integer.parseInt(newNeighborMAC[1]));
                        socket.send(flooder);
                        System.out.println("Sent flood packet to: "+ newNeighborMAC[0] +":"+newNeighborMAC[1]);
                    }
                }
            }
        }
    }
}