import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class Router {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Switch name not provided in arguments...");
            System.exit(1);
        }

        // Get router ports from parser
        Parser routerParser = new Parser(args[0]);
        List<Integer> ports = routerParser.getRouterPorts();

        // Set up selector and bind all ports
        Selector selector = Selector.open();
        Map<DatagramChannel, Integer> channelPortMap = new HashMap<>();

        for (int port : ports) {
            DatagramChannel channel = DatagramChannel.open();
            channel.socket().bind(new InetSocketAddress(port));
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
            channelPortMap.put(channel, port);
            System.out.println("Listening on port: " + port);
        }

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        // POPULATE ROUTER TABLE ----------------------------------
        String[] neighbors = routerParser.getNeighbors();
        HashMap<String, routerRecord> routerTable = new HashMap<>();

        // populate initial neighbor table
        for (String neighbor:neighbors) {

            // split net info
            String netDestination = neighbor.split("\\.")[0];

            // get all info for the table entry (a bit complicated)
            routerRecord recordEntry = new routerRecord(1, neighbor);
            // put all info into local router table
            routerTable.put(netDestination, recordEntry);
        }

        System.out.println("\n--- Routing Table ---");
        for (String dest : routerTable.keySet()) {
            Record entry = routerTable.get(dest);
            System.out.println(dest + " -> " + entry);
        }
        System.out.println("---------------------\n");




//        if (args[0].equals("R3")) {
//            System.out.println("Sending test routing packet from:" + args[0]);
//
////            1 +";"+ name +";"+ router + ";" + ip + ";" + destinationVirtualIp + ";" + message
//            String testMessage = "0;A;R1;;"; // A mock routing update
//            byte[] messageBytes = testMessage.getBytes();
//
//            DatagramSocket socket = new DatagramSocket(); // can auto-bind
//
//
////            InetAddress localhost = InetAddress.getByName("127.0.0.1");
////            int destinationPort = ports.get(0); // send to the first port in R1’s list
//
//            InetAddress destIP = InetAddress.getByName("10.228.241.22");
//            int destPort = 3007;
//            DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length,destIP, destPort);
//
//            socket.send(packet);
//            socket.close();
//        }


        while (true) {
            selector.select(); // Block until something is ready
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();

            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();

                if (key.isReadable()) {
                    DatagramChannel channel = (DatagramChannel) key.channel();
                    buffer.clear();

                     // SocketAddress sender = channel.receive(buffer);
                     // MAY NEED THE FLIP
                     buffer.flip();

                    String frame = new String(buffer.array(), 0, buffer.limit());
                    int port = channelPortMap.get(channel);
                    System.out.println("Received on port " + port + ": " + frame);

                    // Frame parsing
                    String[] frameParts = frame.split(";");
                    String frameType = frameParts[0];

                    switch (frameType) {
                        case "2": // Flood packet
                            System.out.println("Flood frame ignored...");
                            break;
                        case "1": // User packet
                            // Frame format: 1  name  router  ip  destinationVirtualIp  message;

                            // splits destination virtualIp to get just the destination net info, for searching in routerTable
                            String destVirtualIP = frameParts[4];
                            String destNet = destVirtualIP.split("\\.")[0];

                            routerRecord nextHopRecord = routerTable.get(destNet);
                            if (nextHopRecord == null) {
                                System.out.println("No route to destination: " + destVirtualIP);
                                break;
                            }
                            Parser nextHopParser = new Parser(nextHopRecord.nextHop());

                            byte[] forwardBytes = frame.getBytes();
                            DatagramPacket forwardPacket = new DatagramPacket(forwardBytes, forwardBytes.length,nextHopParser.getIP(), nextHopParser.getPort());

                            // Send using a temporary DatagramSocket
                            DatagramSocket forwardSocket = new DatagramSocket();
                            forwardSocket.send(forwardPacket);
                            forwardSocket.close();

                            System.out.println("Forwarded packet to " + destVirtualIP + " via " + nextHopParser.getIP().toString() + ":" +nextHopParser.getPort());

                            break;
                        case "0": // Routing update
                            // Frame format: 0
                            String sender = frameParts[2];  // e.g., R3
                            String vectorData = frameParts[5]; // e.g., "A,2|B,4"

                            String[] entries = vectorData.split("\\|");
                            boolean updated = false;

                            for (String entryStr : entries) {
                                String[] entryParts = entryStr.split(",");
                                String dest = entryParts[0];
                                int costFromSender = Integer.parseInt(entryParts[1]);

                                int totalCost = 1 + costFromSender; // cost to sender + cost from sender to dest

                                routerRecord current = routerTable.get(dest);
                                if (current == null || totalCost < current.distance()) {
                                    routerTable.put(dest, new routerRecord(totalCost, sender));
                                    updated = true;
                                }
                            }

                            if (updated) {
                                // Send updated vector to all neighbors

                                String routerFrame = "0;";
                                byte[] routerFrameBytes = routerFrame.getBytes();

                                String[] routerNeighbors = routerParser.getNeighbors();
                                for (String neighbor:routerNeighbors) {

                                }

//                                DatagramPacket forwardRouterPacket = new DatagramPacket(routerFrameBytes, routerFrameBytes.length,nextHopParser.getIP(), nextHopParser.getPort());

                                // Send using a temporary DatagramSocket
                                DatagramSocket forwardRouterSocket = new DatagramSocket();
//                                forwardRouterSocket.send(forwardRouterPacket);
                                forwardRouterSocket.close();
                            }
                            break;
                        default:
                            System.out.println("Unknown frame type: " + frameType);
                    }
                }
            }
        }
    }
}
