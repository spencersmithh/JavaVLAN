import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class Router {

    public static void sendStarterRouterPacket(boolean hasRun, Parser routerParser) throws IOException, InterruptedException {
        if (!hasRun) {
            int timeMs = 1000;
            System.out.println("starter packet running in:"+ timeMs/1000);
            Thread.sleep(timeMs);

            StringBuilder startingTableData = new StringBuilder("0");

            // Send updated vector to all neighbors
            String entryItem = "";
            for (Map.Entry<String, routerRecord> entry : routerTable.entrySet()) {
                String destination = entry.getKey();
                int distance = entry.getValue().distance();
                String nextHop = entry.getValue().nextHop();

                entryItem = ";"+ destination +","+ distance +","+ nextHop
+               startingTableData.append(entryItem);
            }

            System.out.println(startingTableData);


//            byte[] routerFrameBytes = routerFrame.getBytes();
//
//            System.out.println("starter router table created, sending to neighbors. frame: "+ routerFrame);
//
//            String[] routerNeighbors = routerParser.getNeighbors();
//            for (String neighbor:routerNeighbors) {
//                Parser neigborParser = new Parser(neighbor);
//                DatagramPacket forwardRouterPacket = new DatagramPacket(routerFrameBytes, routerFrameBytes.length,neigborParser.getIP(), neigborParser.getPort());
//
//                // Send using a temporary DatagramSocket
//                DatagramSocket forwardRouterSocket = new DatagramSocket();
//                forwardRouterSocket.send(forwardRouterPacket);
//                forwardRouterSocket.close();
//            }
//            System.out.println("starter table sent to all neighbors");
        }
    }


    public static boolean hasRun = false;
    public static HashMap<String, routerRecord> routerTable = new HashMap<>();

    public static void main(String[] args) throws Exception {

        // start by user input with args??
        // start by calling specific function after sleeping for a few seconds this func changes a bool than no longer causes it to run

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

        System.out.println("sending out first router packet...");
        sendStarterRouterPacket(hasRun,routerParser);
        hasRun = true;



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

                            //TODO STOP FROM SENDING EMPTY PACKETS, make sure the table updating stops

                            List<String[]> routerFrameParts = new ArrayList<>();
                            for (String item:frameParts) {
                                if (item.equals("0")){
                                    continue;
                                }
                                String[] splitItem = item.split(",");
                                routerFrameParts.add(splitItem);
                            }

                            boolean updated = false;

                            for (String[] entry : routerFrameParts) {
                                String sender = entry[0];
                                int distance = Integer.parseInt(entry[1]);
                                String nextHop = entry[2];

                                String destination = nextHop.split("\\.")[0];
                                int totalCost = distance + 1;

                                //bellman ford
                                routerRecord current = routerTable.get(destination);
                                if (current == null || totalCost <= distance) {
                                    System.out.println("updated router table with: "+destination+": "+totalCost+", "+sender);
                                    routerTable.remove(destination);
                                    routerTable.put(destination, new routerRecord(totalCost, sender));
                                    updated = true;
                                }
                            }

                            if (updated) {
                                // Send updated vector to all neighbors
                                String routerFrame = "0;"+ routerFrameParts.toString();
                                byte[] routerFrameBytes = routerFrame.getBytes();

                                System.out.println("router table updated, sending to neighbors. frame: "+ routerFrame);

                                String[] routerNeighbors = routerParser.getNeighbors();
                                for (String neighbor:routerNeighbors) {
                                    Parser neigborParser = new Parser(neighbor);
                                    DatagramPacket forwardRouterPacket = new DatagramPacket(routerFrameBytes, routerFrameBytes.length,neigborParser.getIP(), neigborParser.getPort());

                                    // Send using a temporary DatagramSocket
                                    DatagramSocket forwardRouterSocket = new DatagramSocket();
                                    forwardRouterSocket.send(forwardRouterPacket);
                                    forwardRouterSocket.close();
                                }
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