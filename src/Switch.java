import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
public class Switch {
    public static void main(String[] args) throws Exception {
        String switchID = args[0];
        Parser parser = new Parser(switchID);

        // TBH I'm not exactly sure what I wanted this for...
        try {
            InetAddress IP = InetAddress.getByName(parser.getID()[0]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        int portNumber = Integer.parseInt(parser.getID()[1]);

        String[] neighbors = parser.getNeighbors();

        // create the switch table for port and hosts

        DatagramSocket socket = new DatagramSocket(portNumber);
        DatagramPacket frameRequest = new DatagramPacket(new byte[1024], 1024);

        while (true) {
            socket.receive(frameRequest);
            byte[] frame = Arrays.copyOf(frameRequest.getData(), frameRequest.getLength());
            // get the srcMAC and destMAC by deciphering the frame

            // update the table with the srcMAC if it's not already in there

            // check to see if destMAC is in the table

            // if it is, send directly to the destMAC

            // else, flood the LAN and adjust the table
        }
    }

    // TODO: receive frame, decipher the frame, srcMAC, destMAC, search table, flood, adjust table

    // LUKAS: DECIPHER FRAME (GET SRC AND DEST MACS), LATER: FLOODING

    // ELAINE: RECEIVES FRAME

    // BOTH: HASH MAP/TABLE
}
