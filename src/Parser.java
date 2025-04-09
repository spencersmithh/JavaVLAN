import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
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

    // returns String[] "IP,Port"
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
            // TODO remove this is prob wrong, meant for our old host system, prob not used with . also
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

    //    used for switch to get host names
        public String getID() {
            return name;
        }

    //    for host to get its own virtIP
        public String getVirtualIP() {
            return getNetInfo()[2];
        }

    //    for host to get its router name
        public String getRouterName() {
            return getNetInfo()[3];
        }

    //  gets the routers neighbors based of ex: R1conn in config
//    public String[] getRouterNeighbors(){
//        String[] neighbors = properties.getProperty(getID() + "conn").split(",");
//
//        return neighbors;
//    }

    //    get the routers own ports
        public List<Integer> getRouterPorts(){
            // gets the names of the ports based of the R1-R6 in config
            String[] ports = properties.getProperty(getID()).split(",");
            List<Integer> finalPorts = new ArrayList<>();

            for (String port: ports) {
                finalPorts.add(Integer.valueOf(properties.getProperty(port).split(",")[1]));
            }

            return finalPorts;
        }
    }