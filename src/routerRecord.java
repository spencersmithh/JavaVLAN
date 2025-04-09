// this is the record containing dest, distance, next hop
public record routerRecord(int distance, String nextHop) {}

// this tuple data type contains the ip and port, it goes as the 3 arg of the record above
//class ipPortTuple<IP, PORT> {
//    private final IP ip;
//    private final PORT port;
//
//    public ipPortTuple(IP ip, PORT port) {
//        this.ip = ip;
//        this.port = port;
//    }
//
//    public  IP getIP() {
//        return ip;
//    }
//
//    public PORT getPORT() {
//        return port;
//    }
//}