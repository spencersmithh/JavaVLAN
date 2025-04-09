import java.io.IOException;
import java.net.*;

public class RouterOLDF {
    private static Parser parser;
    private static String routerID;
    private static int[] neighborPorts;

    public RouterOLDF(String[] args) {
        routerID = args[0];
        parser = new Parser(routerID);
        neighborPorts = parser.getRouterNeighborPorts();
    }

    public int[] getNeighborPorts() {
        return neighborPorts;
    }

    public static class portProcess implements Runnable {
        private int port;

        public portProcess(int port) {
            this.port = port;
        }

        public int changePort(int currentPort) {
            int len = neighborPorts.length;
            int newPort = -1;

            for (int i = 0; i < len; i++) {
                if (currentPort == neighborPorts[i]) {
                    if (i != len-1) {
                        newPort = neighborPorts[i+1];
                    } else { newPort = neighborPorts[0]; }
                }
            }

            return newPort;
        }

//        } // THIS HAS TO CHANGE
        @Override
        public void run() {
            // TODO: Take out all hard-coded net stuff.
            //       Distance Vector stuff --> generate initial DV, send to neighbors
            //          JAVA Record: (cost, next_hop) --> each destination represented by a record --> key: destination, value: record
            //       Differentiate between two different kinds of packets (host vs. router) --> add header to frame (0 or 1)
            //       Add new logic to process DV frame

            while (true) {
                try {
                    System.out.println("Listening port: " + port);
                    DatagramSocket socket = new DatagramSocket(port);
                    DatagramPacket frameRequest = new DatagramPacket(new byte[1024], 1024);

//                    System.out.println(side + " awaiting frame...");
//                    System.out.println(side + " Frame received: " + frame);

                    System.out.println("Port " + port + " is awaiting frame...");
//                    TODO: determine frame type
                    socket.receive(frameRequest);
                    String frame = new String(frameRequest.getData(), 0, frameRequest.getLength());
                    System.out.println("Port " + port + " received frame: " + frame);

                    // Parse frame
                    String[] frameParts = frame.split(";");
                    if (frameParts.length < 5) {
                        if (frameParts.length == 3){
                            // need a better system for rejecting flood frames, should the router be getting these anyway??
                            // System.out.println(side + " ignoring flood frame: " + frame);
                            System.out.println("Port " + port + " ignoring flood frame: " + frame);
                        }else{
                            // System.out.println(side + " Frame has incorrect length");
                            System.out.println("Port " + port + ": Frame has incorrect length");
                        }
                        socket.close();
                        continue;
                    }

                    //        MAYBE CHANGE THIS LATER
//                    HashMap<String, String[]> R1Table = new HashMap<>();
//                    R1Table.put("net1", new String[]{"S1" , "yes"});
//                    R1Table.put("net2", new String[]{"net2.R2" , "yes"});
//                    R1Table.put("net3", new String[]{"net2.R2" , "no"});
//
//                    HashMap<String, String[]> R2Table = new HashMap<>();
//                    R2Table.put("net1", new String[]{"net2.R1" , "no"});
//                    R2Table.put("net2", new String[]{"net2.R1", "yes"});
//                    R2Table.put("net3", new String[]{"S2" , "yes"});

                    try {
                        String newFrame;
                        String net = frameParts[3].split("\\.")[0];

                        if (!R1Table.containsKey(net) || !R2Table.containsKey(net)) {
                            System.out.println("There should always be a key in the table...");
                            System.exit(1);
                        }

//                        String destMAC;
//                        // side = swapSide(side);
//                        port = changePort(port);
//                        if (routerID.equals("R1")) {
//                            destMAC = R1Table.get(net)[0];
//                            if (R1Table.get(net)[1].equals("yes")) {
//                                newFrame = "net1.R1" + ";" + frameParts[3].split("\\.")[1] + ";" + frameParts[2] + ";" + frameParts[3] + ";" + frameParts[4];
//                                // System.out.println(side + " directly connected, created frame: " + newFrame);
//                            } else {
//                                newFrame = "net2.R1" + ";" + R1Table.get(net)[0] + ";" + frameParts[2] + ";" + frameParts[3] + ";" + frameParts[4];
//                                // System.out.println(side + " in-directly connected, created frame: " + newFrame);
//                            }
//                            System.out.println("Port " + port + " is directly connected, created frame: " + newFrame);
//                        } else {
//                            destMAC = R2Table.get(net)[0];
//                            if (R2Table.get(net)[1].equals("yes")) {
//                                newFrame = "net3.R2" + ";" + frameParts[3].split("\\.")[1] + ";" + frameParts[2] + ";" + frameParts[3] + ";" + frameParts[4];
//                                // System.out.println(side + " directly connected, created frame: " + newFrame);
//                            } else {
//                                newFrame = "net2.R2" + ";" + R2Table.get(net)[0] + ";" + frameParts[2] + ";" + frameParts[3] + ";" + frameParts[4];
//                                // System.out.println(side + " in-directly connected, created frame: " + newFrame);
//                            }
//                            System.out.println("Port " + port + " is directly connected, created frame: " + newFrame);
//                        }

//                      System.out.println(side + " sending out final packet: " + newFrame);
//                      newFrame = "net1.R1" + ";" + frameParts[3].split("\\.")[1] + ";" + frameParts[2] + ";" + frameParts[3] + ";" + frameParts[4];
//                      destMAC = R2Table.get(net)[0];
//

                        System.out.println("Port " + port + " is sending out final packet: " + newFrame);
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
                port = changePort(port);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        RouterOLDF router = new RouterOLDF(args);
        if(args.length < 1){
            System.out.println("Switch name not provided in arguments...");
            System.exit(1);
        }

        int[] nPorts = router.getNeighborPorts();
        int numNeighbors = nPorts.length;

//        if (numNeighbors == 4) {
//            ExecutorService es = Executors.newFixedThreadPool(4);
//            Runnable port1 = new Router.portProcess(nPorts[0]);
//            Runnable port2 = new Router.portProcess(nPorts[1]);
//            Runnable port3 = new Router.portProcess(nPorts[2]);
//            Runnable port4 = new Router.portProcess(nPorts[3]);
//            es.submit(port1);
//            es.submit(port2);
//            es.submit(port3);
//            es.submit(port4);
//        } else if (numNeighbors == 3) {
//            ExecutorService es = Executors.newFixedThreadPool(3);
//            Runnable port1 = new Router.portProcess(nPorts[0]);
//            Runnable port2 = new Router.portProcess(nPorts[1]);
//            Runnable port3 = new Router.portProcess(nPorts[2]);
//            es.submit(port1);
//            es.submit(port2);
//            es.submit(port3);
//        } else if (numNeighbors == 2) {
//            ExecutorService es = Executors.newFixedThreadPool(2);
//            Runnable port1 = new Router.portProcess(nPorts[0]);
//            Runnable port2 = new Router.portProcess(nPorts[1]);
//            es.submit(port1);
//            es.submit(port2);
//        } else {
//            System.out.println("Incorrect number of neighbors!");
//        }

//        ExecutorService es = Executors.newFixedThreadPool(2);
//        Runnable portProcess1 = new Router.portProcess("L");
//        Runnable portProcess2 = new Router.portProcess("R");
//        es.submit(portProcess1);
//        es.submit(portProcess2);
    }
}
