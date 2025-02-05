import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.HashMap;

public class Switch {
    public static void main(String[] args) throws Exception {
        if(args.length < 1){
            System.out.println("Switch name not provided in arguments...");
            System.exit(1);
        }

        String switchID = args[0];
        Parser parser = new Parser(switchID);

        String[] srcNeighbors = parser.getNeighbors();

        // create the switch table. format = {"macName":"IP:Port"}
            // we are not using the table values only using it for name lookups, maybe replace? or just keep for fast lookup
        HashMap<String, String[]> switchTable = new HashMap<>();

        DatagramSocket socket = new DatagramSocket(parser.getPort());
        DatagramPacket frameRequest = new DatagramPacket(new byte[1024], 1024);

        while (true) {
            socket.receive(frameRequest);
            String frame = new String(frameRequest.getData(),0, frameRequest.getLength());
            System.out.println("Frame received: " + frame);

            // Parse frame
            String[] frameParts = frame.split(";");
            if (frameParts.length < 3){
                System.out.println("Frame has incorrect length");
                continue;
            }

            Parser src = new Parser(frameParts[0]);
            Parser dest = new Parser(frameParts[1]);

            String newFrame = switchID + ";" + frameParts[1] + ";" + frameParts[2];

//            adds sourceMAC to IP table if not found
            if (!switchTable.containsKey(src.getID())){
                switchTable.put(src.getID(), src.getMAC());
                System.out.println("added " + src.getID() + " to hashmap");
            }

//            if the destMAC is known forward to known location
            if (switchTable.containsKey(dest.getID())){
                System.out.println("destMac Known. forwarding packet...");
                byte[] response = newFrame.getBytes();
                DatagramPacket forwardPacket = new DatagramPacket(response, response.length, dest.getIP(), dest.getPort());
                socket.send(forwardPacket);
                System.out.println("packet forwarded to: "+ dest.getID() + ":" + dest.getMAC());

            } else {
                // Flooding
                System.out.println("destIP not known starting flood...");
                for (String neighbor : srcNeighbors){
                    // creates a parser for each neighbor
                    Parser newNeighborParser = new Parser(neighbor);

                    // check to stop sending back to source
                    if (!Arrays.equals(newNeighborParser.getMAC(), src.getMAC())){
                        DatagramPacket flooder = new DatagramPacket(newFrame.getBytes(), newFrame.length(), newNeighborParser.getIP(), newNeighborParser.getPort());
                        socket.send(flooder);
                        System.out.println("Sent flood packet to: "+ newNeighborParser.getID() +":"+ Arrays.toString(newNeighborParser.getMAC()));
                    }
                }
                System.out.println("Flooding finished");
            }
        }
    }
}