import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Router {
    private static Parser parser;
    private static String routerID;

    public Router(String[] args) {
        routerID = args[0];
        parser = new Parser(routerID);
    }

    public static Integer getPortSide(String side){
        int[] r1ports = new int[]{3007, 3008};
        int[] r2ports = new int[]{3009, 3010};

        if (routerID.equals("R1")){
            if (side.equals("L")){
                return r1ports[0];
            }else{
                return r1ports[1];
            }
        }else{
            if (side.equals("L")){
                return r2ports[0];
            }else{
                return r2ports[1];
            }
        }
    }

    public static class leftPort implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    DatagramSocket leftSocket = new DatagramSocket(getPortSide("L"));
                    DatagramPacket frameRequest = new DatagramPacket(new byte[1024], 1024);

                    leftSocket.receive(frameRequest);
                    String frame = new String(frameRequest.getData(), 0, frameRequest.getLength());
                    System.out.println("Frame received: " + frame);

                    // Parse frame
                    String[] frameParts = frame.split(";");
                    if (frameParts.length < 5) {
                        System.out.println("Frame has incorrect length");
                        System.exit(1);
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static class rightPort implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    DatagramSocket rightSocket = new DatagramSocket(getPortSide("R"));
                    DatagramPacket frameRequest = new DatagramPacket(new byte[1024], 1024);

                    rightSocket.receive(frameRequest);
                    String frame = new String(frameRequest.getData(), 0, frameRequest.getLength());
                    System.out.println("Frame received: " + frame);

                    // Parse frame
                    String[] frameParts = frame.split(";");
                    if (frameParts.length < 5) {
                        System.out.println("Frame has incorrect length");
                        System.exit(1);
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new Router(args);

        if(args.length < 1){
            System.out.println("Switch name not provided in arguments...");
            System.exit(1);
        }

        ExecutorService es = Executors.newFixedThreadPool(2);
        Runnable leftPort = new Router.leftPort();
        Runnable rightPort = new Router.rightPort();
        es.submit(leftPort);
        es.submit(rightPort);

//        MAYBE CHAGE THIS LATER
        HashMap<String, String[]> R1Table = new HashMap<>();
        R1Table.put("net1", new String[]{"S1" , "yes"});
        R1Table.put("net2", new String[]{"net2.R2" , "yes"});
        R1Table.put("net3", new String[]{"net2.R2" , "no"});

        HashMap<String, String[]> R2Table = new HashMap<>();
        R2Table.put("net1", new String[]{"net1.R1" , "no"});
        R2Table.put("net2", new String[]{"net2.R1", "yes"});
        R2Table.put("net3", new String[]{"S2" , "yes"});

        try {
            while (true) {
                DatagramSocket socket = new DatagramSocket(parser.getPort());
                DatagramPacket frameRequest = new DatagramPacket(new byte[1024], 1024);

                socket.receive(frameRequest);
                String frame = new String(frameRequest.getData(), 0, frameRequest.getLength());
                System.out.println("Frame received: " + frame);

                // Parse frame
                String[] frameParts = frame.split(";");
                if (frameParts.length < 5) {
                    System.out.println("Frame has incorrect length");
                    continue;
                }

                String newFrame;
                String net = frameParts[3].split("\\.")[0];

                if (!R1Table.containsKey(net) || !R2Table.containsKey(net)){
                    System.out.println("THERE A BIG ISSUES CUZZO");
                    System.exit(1);
                }

                String destMAC;
                if (routerID.equals("R1")){
                    destMAC = R1Table.get(net)[0];
                    if (R1Table.get(net)[1].equals("yes")){
                        newFrame = parser.getID() + ";" + frameParts[1] + ";" + frameParts[2] + ";" + frameParts[3] + ";" + frameParts[4];
                    }else{
                        newFrame = parser.getID() + ";" + R1Table.get(net)[0] + ";" + frameParts[2] + ";" + frameParts[3] + ";" + frameParts[4];
                    }

                }else {
                    destMAC = R2Table.get(net)[0];
                    if (R2Table.get(net)[1].equals("yes")) {
                        newFrame = parser.getID() + ";" + frameParts[1] + ";" + frameParts[2] + ";" + frameParts[3] + ";" + frameParts[4];
                    } else {
                        newFrame = parser.getID() + ";" + R2Table.get(net)[0] + ";" + frameParts[2] + ";" + frameParts[3] + ";" + frameParts[4];
                    }
                }

                Parser destMacParser = new Parser(destMAC);
                DatagramPacket finalPacket = new DatagramPacket(newFrame.getBytes(), newFrame.length(), destMacParser.getIP(), destMacParser.getPort());
                socket.send(finalPacket);
                socket.close();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
