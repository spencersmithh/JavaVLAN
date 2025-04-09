import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class Router {

//    private static routerRecord getRouterRecord(String neighbor) throws UnknownHostException {
////         create parser object for the neighbor
//        Parser neighborParser = new Parser(neighbor);
//        // create the next hop tuple data type for that neighbor, goes in record
//        ipPortTuple<InetAddress, Integer> nextHop = new ipPortTuple<>(neighborParser.getIP(),neighborParser.getPort());
//        // create the record for the neighbor, return
//        return new routerRecord(neighbor,1, nextHop);
//    }

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
        HashMap<String, Record> routerTable = new HashMap<>();

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




        if (args[0].equals("R3")) {
            System.out.println("Sending test routing packet from:" + args[0]);

//            1 +";"+ name +";"+ router + ";" + ip + ";" + destinationVirtualIp + ";" + message
            String testMessage = "0;A;R1;;"; // A mock routing update
            byte[] messageBytes = testMessage.getBytes();

            DatagramSocket socket = new DatagramSocket(); // can auto-bind


//            InetAddress localhost = InetAddress.getByName("127.0.0.1");
//            int destinationPort = ports.get(0); // send to the first port in R1’s list

            InetAddress destIP = InetAddress.getByName("10.228.241.22");
            int destPort = 3007;
            DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length,destIP, destPort);

            socket.send(packet);
            socket.close();
        }


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

                    SocketAddress sender = channel.receive(buffer);
                    buffer.flip();

                    String frame = new String(buffer.array(), 0, buffer.limit());
                    int port = channelPortMap.get(channel);
                    System.out.println("Received on port " + port + ": " + frame);

                    // Frame parsing
                    String[] frameParts = frame.split(";");
                    String frameType = frameParts[0];

                    switch (frameType) {
                        case "2": // "2" flag means flood packet
                            System.out.println("Flood frame ignored...");
                            break;
                        case "1": // "1" flag means user packet
                            // TODO: Forward user packet
                            break;
                        case "0": // else "0" flag means it's a routing update
                            // TODO: Routing update – apply Bellman-Ford logic here
                            System.out.println("GOT HERE");
                            break;
                        default:
                            System.out.println("Unknown frame type: " + frameType);
                    }
                }
            }
        }
    }

}
