import java.net.InetAddress;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        if (args.length != 2) {
            System.out.println("Please specify switch 1 id: ");
            return;
        }
        String s1ID = args[0];

        Switch s1 = new Switch(s1ID);
    }
}