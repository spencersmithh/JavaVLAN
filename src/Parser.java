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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] getNetInfo() {
        String rawMac = properties.getProperty(name);
        if (rawMac == null) {
            throw new IllegalArgumentException("No entry found for " + name + " in config.properties");
        }
        return rawMac.split(",");
    }

    public InetAddress getIP() throws UnknownHostException {
        return InetAddress.getByName(getNetInfo()[0]);
    }

    public Integer getPort() {
        return Integer.parseInt(getNetInfo()[1]);
    }

    public String[] getNeighbors() {
        String rawConnections;
        if (name.contains(".")) {
            String routerName = getVirtualIP().split("\\.")[1];
            rawConnections = properties.getProperty(routerName + "conn");
        } else {
            rawConnections = properties.getProperty(name + "conn");
        }
        if (rawConnections == null) {
            throw new IllegalArgumentException("No connection entries found for " + name + " in config.properties");
        }
        return rawConnections.split(",");
    }

    public String getID() {
        return name;
    }

    public String getVirtualIP() {
        return getNetInfo()[2];
    }

    public String getRouterName() {
        return getNetInfo()[3];
    }
}