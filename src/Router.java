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
                            String[] neighbors = routerParser.getNeighbors();

                            break;
                        default:
                            System.out.println("Unknown frame type: " + frameType);
                    }
                }
            }
        }
    }
}
