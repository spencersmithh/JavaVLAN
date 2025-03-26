import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Router {
    private static Parser parser;
    private static String routerID;
//    volatile static String portSide = "L";

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

    public static class portProcess implements Runnable {
        private String side;

        public String swapSide(String side){
            if (side.equals("R")){
                return "L";
            }else {
                return "R";
            }
        }

        public portProcess(String side) {
            this.side = side;
        }
        @Override
        public void run() {
            // TODO: Take out all hard-coded net stuff.
            //       Distance Vector stuff --> generate initial DV, send to neighbors
            //          JAVA Record: (cost, next_hop) --> each destination represented by a record --> key: destination, value: record
            //       Differentiate between two different kinds of packets (host vs. router) --> add header to frame (0 or 1)
            //       Add new logic to process DV frame

            while (true) {
                try {
                    System.out.println("listening side: " + side +" at port: " + getPortSide(side));
                    DatagramSocket socket = new DatagramSocket(getPortSide(side));
                    DatagramPacket frameRequest = new DatagramPacket(new byte[1024], 1024);

                    System.out.println(side + " awaiting frame...");
                    socket.receive(frameRequest);
                    String frame = new String(frameRequest.getData(), 0, frameRequest.getLength());
                    System.out.println(side + " Frame received: " + frame);

                    // Parse frame
                    String[] frameParts = frame.split(";");
                    if (frameParts.length < 5) {
                        if (frameParts.length == 3){
//                            need a better system for rejecting flood frames, should the router be getting these anyway??
                            System.out.println(side + " ignoring flood frame: " + frame);
                        }else{
                            System.out.println(side + " Frame has incorrect length");
                        }
                        socket.close();
                        continue;
                    }

                    //        MAYBE CHANGE THIS LATER
                    HashMap<String, String[]> R1Table = new HashMap<>();
                    R1Table.put("net1", new String[]{"S1" , "yes"});
                    R1Table.put("net2", new String[]{"net2.R2" , "yes"});
                    R1Table.put("net3", new String[]{"net2.R2" , "no"});

                    HashMap<String, String[]> R2Table = new HashMap<>();
                    R2Table.put("net1", new String[]{"net2.R1" , "no"});
                    R2Table.put("net2", new String[]{"net2.R1", "yes"});
                    R2Table.put("net3", new String[]{"S2" , "yes"});

                    try {
                        String newFrame;
                        String net = frameParts[3].split("\\.")[0];

                        if (!R1Table.containsKey(net) || !R2Table.containsKey(net)) {
                            System.out.println("There should always be a key in the table...");
                            System.exit(1);
                        }

                        String destMAC;
                        side = swapSide(side);
                        if (routerID.equals("R1")) {
                            destMAC = R1Table.get(net)[0];
                            if (R1Table.get(net)[1].equals("yes")) {
                                newFrame = "net1.R1" + ";" + frameParts[3].split("\\.")[1] + ";" + frameParts[2] + ";" + frameParts[3] + ";" + frameParts[4];
                                System.out.println(side + " directly connected, created frame: " + newFrame);
                            } else {
                                newFrame = "net2.R1" + ";" + R1Table.get(net)[0] + ";" + frameParts[2] + ";" + frameParts[3] + ";" + frameParts[4];
                                System.out.println(side + " in-directly connected, created frame: " + newFrame);
                            }
                        } else {
                            destMAC = R2Table.get(net)[0];
                            if (R2Table.get(net)[1].equals("yes")) {
                                newFrame = "net3.R2" + ";" + frameParts[3].split("\\.")[1] + ";" + frameParts[2] + ";" + frameParts[3] + ";" + frameParts[4];
                                System.out.println(side + " directly connected, created frame: " + newFrame);
                            } else {
                                newFrame = "net2.R2" + ";" + R2Table.get(net)[0] + ";" + frameParts[2] + ";" + frameParts[3] + ";" + frameParts[4];
                                System.out.println(side + " in-directly connected, created frame: " + newFrame);
                            }
                        }

                        System.out.println(side + " sending out final packet: " + newFrame);
                        Parser destMacParser = new Parser(destMAC);
                        DatagramPacket finalPacket = new DatagramPacket(newFrame.getBytes(), newFrame.length(), destMacParser.getIP(), destMacParser.getPort());
                        socket.send(finalPacket);
                        socket.close();

                    } catch (Exception e){
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                side = swapSide(side);
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
        Runnable portProcess1 = new Router.portProcess("L");
        Runnable portProcess2 = new Router.portProcess("R");
        es.submit(portProcess1);
        es.submit(portProcess2);
    }
}
