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
            /*
            Not exactly sure what the following two lines actually do...
            String switch1ID=properties.getProperty("switch1");
            System.out.println(switch1ID);
            */
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getID() {
        String rawID = properties.getProperty(name);
        if (rawID == null) {
            throw new IllegalArgumentException("No entry found for " + name + " in config.properties");
        }
        // pretty sure the following line does nothing because "()" doesn't actually appear anywhere in the config file...
        String id = rawID.replaceAll("()", "");
        return id;
    }

    public String[] getNeighbors() {
        String rawConnections = properties.getProperty(name + "conn");
        if (rawConnections == null) {
            throw new IllegalArgumentException("No connection entries found for " + name + " in config.properties");
        }
        return rawConnections.split(",");
    }

    public String getMAC() { // does this actually return the mac, or just the id/name?
        return name; // my suggestion: getIP() + " " + getPort(); however, this would mean we'd have to change some things in the rest of project
    }

    public InetAddress getIP() throws UnknownHostException {
        return InetAddress.getByName(getID().split(",")[0]);
    }

    public Integer getPort(){
        // This is split up like this because I couldn't figure out what was broken here before...
        String portAsString = getID().split(",")[1];
        int port = Integer.parseInt(portAsString);
        return port;
    }
}