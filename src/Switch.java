import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
public class Switch {
    // TODO: get id as command-line argument, parses the config file to get its neighbors
    private String name;
    private InetAddress IP;
    private int portNumber;
    private final Parser parser;
    private Arrays neighbors[];


    public Switch(String name) {
        name = this.name;
        parser = new Parser(name);
        IP = parser.getID()[0];
        portNumber = parser.getID()[1];
        neighbors = getNeighbors();
    }
}
