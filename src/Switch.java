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
//
        String[] srcNeighbors = parser.getNeighbors();

        // create the switch table for port and hosts
        HashMap<String, String> switchTable = new HashMap<>();

        DatagramSocket socket = new DatagramSocket(portNumber);
        DatagramPacket frameRequest = new DatagramPacket(new byte[1024], 1024);

        while (true) {
            socket.receive(frameRequest);
            String frame = new String(frameRequest.getData(),0, frameRequest.getLength());
            System.out.println("Frame received: " + frame);
            // get the srcMAC and destMAC by deciphering the frame
//            try {
//                InetAddress srcIP = InetAddress.getByName("");
//            } catch (UnknownHostException e) {
//                e.printStackTrace();
//            }

            // Parse frame
            String[] frameParts = frame.split(";");
            if (frameParts.length < 3){
                System.out.println("Frame has incorrect length");
                continue;
            }

//            split frame
            String srcMAC = frameParts[0];
            String destMAC = frameParts[1];
            String message = frameParts[2];

//            split destMac
            String destIP = destMAC.split(",")[0];
            String destPort = destMAC.split(",")[1];

//            split srcMac
            String srcIP = srcMAC.split(",")[0];
            String srcPort = srcMAC.split(",")[1];

//            adds sourceMAC to IP table if not found
            if (!switchTable.containsKey(srcMAC)){
                switchTable.put(srcIP,srcPort);
                System.out.println("added " + srcMAC+ " to hashmap");
            }

//            if the destMAC is known forward to known location
            if (switchTable.containsKey(destIP)){
                System.out.println("destMac Known. forwarding packet...");
                // load frame into bytes for forwarding
                byte[] response = frame.getBytes();
                // create IP address
                InetAddress forwardingAddress = InetAddress.getByName(destIP);
                // create the packet
                DatagramPacket forwardPacket = new DatagramPacket(response, response.length, forwardingAddress,Integer.parseInt(destPort));
                // send the packet
                socket.send(forwardPacket);
                System.out.println("packet forwarded to: "+destIP + ":" + destPort);

            } else {
                // Flooding
                System.out.println("destIP not known starting flood...");
                for (String neighbor : srcNeighbors){
                    // creates a parser for each neighbor to get macs, could change if parser.java is changed
                    Parser newNeighborParser = new Parser(neighbor);
                    String[] newNeighborMAC = newNeighborParser.getID();
                    InetAddress newNeighborIP = InetAddress.getByName(newNeighborMAC[0]);

                    // check to stop sending back to source
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