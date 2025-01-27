import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
public class Switch {
    // TODO: get id as command-line argument, parses the config file to get its neighbors
    private String name;
    private InetAddress IP;
    private int portNumber;
    private final Parser parser;
    private String[] neighbors;

    public Switch(String name) {
        name = this.name;
        parser = new Parser(name);
        try {
            IP = InetAddress.getByName(parser.getID()[0]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        portNumber = Integer.parseInt(parser.getID()[1]);
        neighbors = parser.getNeighbors();
    }

    // TODO: main

    // TODO: receive frame, decipher the frame, srcMAC, destMAC, search table, flood, adjust table

    // LUKAS: DECIPHER FRAME (GET SRC AND DEST MACS), LATER: FLOODING

    // ELAINE: RECEIVES FRAME

    // BOTH: HASH MAP/TABLE
}
