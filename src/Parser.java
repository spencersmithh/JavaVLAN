import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Properties;

// NOTE
// a getRouter function would help a lot in this, given the net(#) return the router...

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

    public String[] getMAC() {
        String rawMac = properties.getProperty(name);
        if (rawMac == null) {
            throw new IllegalArgumentException("No entry found for " + name + " in config.properties");
        }
        return rawMac.split(",");
    }

    public String[] getNeighbors() {
        String rawConnections = properties.getProperty(name + "conn");
        if (rawConnections == null) {
            throw new IllegalArgumentException("No connection entries found for " + name + " in config.properties");
        }
        return rawConnections.split(",");
    }

    public String getID() {
        return name;
    }

    public InetAddress getIP() throws UnknownHostException {
        return InetAddress.getByName(getMAC()[0]);
    }

    public Integer getPort(){
        return Integer.parseInt(getMAC()[1]);
    }

    public String getRouterName() {
        // NOTE needs work and testing, should return the name of router, we will prob want a router class
        String routerOptions = properties.getProperty(name.split(".")[0] + "R");
        return routerOptions;
    }
}