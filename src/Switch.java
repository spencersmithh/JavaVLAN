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

        // create the switch table. format = {"macName";"IP:Port"}
        HashMap<String, String> switchTable = new HashMap<>();
        // populate switch table with that subnets router
        if (args[0].equals("S1")){
            Parser routerR1 = new Parser("net1.R1");
            switchTable.put("net1.R1", routerR1.getIP().toString().replace("/","") + ";" + routerR1.getPort());
        }else{
            Parser routerR2 = new Parser("net3.R2");
            switchTable.put("net3.R2",  routerR2.getIP().toString().replace("/","") + ";" + routerR2.getPort() );
        }
        System.out.println("populated table with router info");

        DatagramSocket socket = new DatagramSocket(parser.getPort());
        DatagramPacket frameRequest = new DatagramPacket(new byte[1024], 1024);

        while (true) {
            socket.receive(frameRequest);
            String frame = new String(frameRequest.getData(),0, frameRequest.getLength());
            System.out.println("Frame received: " + frame);
            String fromID = frameRequest.getAddress().toString().replace("/", "") + ";" + frameRequest.getPort();

            // Parse frame
            String[] frameParts = frame.split(";");
            if (frameParts.length < 4){
                System.out.println("Frame has incorrect length");
                continue;
            }

            Parser src = new Parser(frameParts[1]);
            Parser dest = new Parser(frameParts[2]);

//            adds sourceMAC to IP table if not found
            if (!switchTable.containsKey(src.getID())){
                switchTable.put(src.getID(), fromID);
                System.out.println("added " + src.getID() + " " + fromID + " to hashmap");
            }

//            if the destMAC is known forward to known location
            if (switchTable.containsKey(dest.getID())){
//                System.out.println("destMac Known. forwarding packet...");
                byte[] response = frame.getBytes();

                InetAddress toAddress = InetAddress.getByName(switchTable.get(dest.getID()).split(";")[0]);
                int toPort = Integer.parseInt(switchTable.get(dest.getID()).split(";")[1]);

                DatagramPacket forwardPacket = new DatagramPacket(response, response.length, toAddress, toPort);
                socket.send(forwardPacket);
                System.out.println("destMac Known. packet forwarded to: "+ dest.getID() + ":" + switchTable.get(dest.getID()));

            } else {
                // Flooding
                System.out.println("destIP not known starting flood...");
                for (String neighbor : srcNeighbors) {
                    if (neighbor.contains("R")){
                        continue;
                    }
                    // creates a parser for each neighbor
                    Parser newNeighborParser = new Parser(neighbor);

                    // make frame have a 2 flag at the start
                    frameParts[0] = "2";
                    String newFrame = String.join(";", frameParts);


                    // check to stop sending back to source
                    String neighborID = newNeighborParser.getIP().toString() + ";" + newNeighborParser.getPort();

                    if (!fromID.equals(neighborID.replace("/",""))){
                        System.out.println("successful flood: "+ neighbor + " " + neighborID.replace("/","") + " " + fromID);
                        DatagramPacket flooder = new DatagramPacket(newFrame.getBytes(), newFrame.length(), newNeighborParser.getIP(), newNeighborParser.getPort());
                        socket.send(flooder);
                    }
                }
                System.out.println("Flooding finished");
            }
        }
    }
}