import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
public class Switch {
    // TODO: get id as command-line argument, parses the config file to get its neighbors
    //
    private int id;
    private Parser parser = new Parser();

    public Switch(int id) {
        id = this.id;

    }
}
