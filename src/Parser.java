import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Properties;

public class Parser {
    private final String name;
    private final Properties properties = new Properties();

    public Parser(String name) {
        this.name = name;

        try (FileInputStream fis = new FileInputStream("src/config.properties")) {
            properties.load(fis);
            String switch1ID=properties.getProperty("switch1");
            System.out.println(switch1ID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getID() {
        String rawID = properties.getProperty(name);
        if (rawID == null) {
            throw new IllegalArgumentException("No entry found for " + name + " in config.properties");
        }
        String id = rawID.replaceAll("()", "");
        return id;
    }

    public String[] getNeighbors() {
        String rawConnections = properties.getProperty(name + "conn");
        if (rawConnections == null) {
            throw new IllegalArgumentException("No connection entries found for " + name + " in config.properties");
        }
        rawConnections = rawConnections.replaceAll("()", "");
        return rawConnections.split(",");
    }

    public String getMAC() {
        return name;
    }

    public InetAddress getIP() throws UnknownHostException {
        return InetAddress.getByName(getID().split(",")[0]);
    }

    public Integer getPort(){
        return Integer.parseInt(getID().split(",")[1]);
    }
}