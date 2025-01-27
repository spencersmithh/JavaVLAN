import java.io.FileInputStream;
import java.io.IOException;
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
    public String[] getNeighbors() {
        String rawConnections = properties.getProperty(name + "conn");
        if (rawConnections == null) {
            return new String[0];
        }
        rawConnections = rawConnections.replaceAll("[()\\s]", "");
        return rawConnections.split(",");
    }
}