import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class test {
    public static void main(String[] args) {

//        ArrayList<String[]> routerFrameParts = new ArrayList<>();
//
////        String[] splitItem = item.split(",");
//        routerFrameParts.add(new String[]{"hello", "yo"});
//
//        String out = routerFrameParts.toString();
//
//        System.out.println(out);
//        System.out.println(routerFrameParts);
        List<String> list = new ArrayList<>();
        list.add("Apple");
        list.add("Banana");
        list.add("Orange");

        String str = list.toString();
        System.out.println(str); // Output: [Apple, Banana, Orange]
    }
}

