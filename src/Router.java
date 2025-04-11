import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Router {

    public static void printRouterTable(){
        System.out.println("\n--- Routing Table ---");
        for (String dest : routerTable.keySet()) {
            Record entry = routerTable.get(dest);
            System.out.println(dest + " -> " + entry);
        }
        System.out.println("---------------------\n");
    }

    public static String createRouterFrame(Parser routerParser){
        String starerRouterFrame = "0;" + routerParser.getID();
        for (Map.Entry<String, routerRecord> entry : routerTable.entrySet()) {
            String destination = entry.getKey();
            int distance = entry.getValue().distance();
            String nextHop = entry.getValue().nextHop();

            starerRouterFrame += ";"+ destination +","+ distance +","+ nextHop;
        }
        return starerRouterFrame;
    }

    public static void sendStarterRouterPacket(Parser routerParser) throws IOException {
        // call to create router frame
        String starerRouterFrame = createRouterFrame(routerParser);

        byte[] routerFrameBytes = starerRouterFrame.getBytes();
        System.out.println("SENDING Router table update: "+ starerRouterFrame);

        String[] routerNeighbors = routerParser.getNeighbors();
        for (String neighbor:routerNeighbors) {
            if (neighbor.contains("S")){
                // skips sending starting router packet to switches
                continue;
            }

//            System.out.println("sending router packet to: " + neighbor);

            Parser neigborParser = new Parser(neighbor);
            DatagramPacket forwardRouterPacket = new DatagramPacket(routerFrameBytes, routerFrameBytes.length,neigborParser.getIP(), neigborParser.getPort());

            // Send using a temporary DatagramSocket
            DatagramSocket forwardRouterSocket = new DatagramSocket();
            forwardRouterSocket.send(forwardRouterPacket);
            forwardRouterSocket.close();
        }
//        System.out.println("starter table sent to all neighbors");
    }

    public static HashMap<String, routerRecord> routerTable = new HashMap<>();

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Switch name not provided in arguments...");
            System.exit(1);
        }

        // Get router ports from parser
        String routerName = args[0];
        Parser routerParser = new Parser(routerName);
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
            routerRecord recordEntry = new routerRecord(1, neighbor);
            // put all info into local router table
            routerTable.put(netDestination, recordEntry);
        }
//        System.out.println("starting routing table");
//        printRouterTable();

        //wait 25 seconds to ensure all routers are running at the same time
//        int timeMs = 25000;
//        System.out.println("starter packet running in:"+ timeMs/1000);
//        Thread.sleep(timeMs);

//        sendStarterRouterPacket(routerParser);
        System.out.println("sending out first router packet...");

        // Schedule periodic routing packet every 60 seconds
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                sendStarterRouterPacket(routerParser);
            } catch (IOException e) {
                System.err.println("Error sending scheduled router packet: " + e.getMessage());
            }
        }, 30, 60, TimeUnit.SECONDS);

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

                    // NEED THE FLIP
                    SocketAddress sender = channel.receive(buffer);
                    if (sender == null){
                        System.out.println("received null packet");
                        continue;
                    }

                    buffer.flip();

                    String frame = new String(buffer.array(), 0, buffer.limit());
                    int port = channelPortMap.get(channel);
                    System.out.println("RECEIVED on port " + port + ": " + frame);

                    // Frame parsing
                    String[] frameParts = frame.split(";");
                    String frameType = frameParts[0];
                    String senderRouterName = frameParts[1];

                    switch (frameType) {
                        case "2": // Flood packet, ensures no flood packets go through
                            System.out.println("Flood frame ignored...");
                            break;
                        case "1": // User packet, Frame format: 1  name  router  ip  destinationVirtualIp  message;
                            // splits destination virtualIp to get just the destination net info, for searching in routerTable
                            String destVirtualIP = frameParts[4];
                            String destNet = destVirtualIP.split("\\.")[0];

                            routerRecord nextHopRecord = routerTable.get(destNet);
                            if (nextHopRecord == null) {
                                System.out.println("No route to destination: " + destVirtualIP);
                                break;
                            }

                            // scum away the net info to find switch "ip,port" through the config
                            String nextHopParserVIP = nextHopRecord.nextHop();
                            if (nextHopParserVIP.contains("S")){
                                nextHopParserVIP = nextHopParserVIP.substring(5);
                            }
                            Parser nextHopParser = new Parser(nextHopParserVIP);

                            byte[] forwardBytes = frame.getBytes();
                            DatagramPacket forwardPacket = new DatagramPacket(forwardBytes, forwardBytes.length,nextHopParser.getIP(), nextHopParser.getPort());

                            // Send using a temporary DatagramSocket
                            DatagramSocket forwardSocket = new DatagramSocket();
                            forwardSocket.send(forwardPacket);
                            forwardSocket.close();

                            System.out.println("FORWARD packet to " + destVirtualIP + " via " + nextHopParser.getIP().toString() + ":" +nextHopParser.getPort());

                            break;
                        case "0": // Routing update
                            String[] routerStrippedFrame = frame.substring(5).split(";"); // skips "0;R1;

                            List<String[]> routerFrameParts = new ArrayList<>();
                            for (String item:routerStrippedFrame) {
                                String[] splitItem = item.split(",");
                                routerFrameParts.add(splitItem);
                            }

                            String senderRouterVIP = "";
                            for (String neighbor:neighbors){
                                if (neighbor.contains(senderRouterName)){
                                    senderRouterVIP = neighbor;
                                }
                            }

                            boolean updated = false;

                            for (String[] entry : routerFrameParts) {
                                String destination = entry[0];
                                int distance = Integer.parseInt(entry[1]);
                                int totalCost = distance + 1;

                                // skips checking self location
                                if (destination.equals(routerParser.getID())) continue;

                                //bellman ford
                                routerRecord current = routerTable.get(destination);

                                if (current == null || totalCost < current.distance()) {
                                    System.out.println("UPDATED router table (sending to neighbors): " + destination + ": " + totalCost + ", " + senderRouterVIP);
                                    routerTable.remove(destination);
                                    routerTable.put(destination, new routerRecord(totalCost, senderRouterVIP));
                                    updated = true;
                                }
                            }
                            if (updated) {
                                String routerFrame = createRouterFrame(routerParser); // call to create router frame with all data in routerTable hashmap

                                byte[] routerFrameBytes = routerFrame.getBytes();

                                System.out.println("UPDATED router table (sending to neighbors): "+ routerFrame);

                                String[] routerNeighbors = routerParser.getNeighbors();
                                for (String neighbor:routerNeighbors) {
                                    if (neighbor.contains("S")){
                                        // skips sending starting router packet to switches
                                        continue;
                                    }

                                    Parser neigborParser = new Parser(neighbor);
                                    DatagramPacket forwardRouterPacket = new DatagramPacket(routerFrameBytes, routerFrameBytes.length,neigborParser.getIP(), neigborParser.getPort());

                                    // Send using a temporary DatagramSocket
                                    DatagramSocket forwardRouterSocket = new DatagramSocket();
                                    forwardRouterSocket.send(forwardRouterPacket);
                                    forwardRouterSocket.close();

                                    printRouterTable();
                                }else{
                                    System.out.println("NO update");
                                }
                            }
                            break;
                        default:
                            System.out.println("Unknown frame type: " + Arrays.toString(frameParts));
                            System.exit(1);

                    }
                }
            }
        }
    }
}