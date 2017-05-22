package util;

/**
 * Created by neiro on 22.05.17.
 */
public class Usage {

    public static final String UsageMessage = "java -jar gmZbxLogrt-X.X.X.jar config.yml";

    public static boolean showWithError(String s) {
        System.out.println("Error: " + s);
        System.out.println(UsageMessage);

        //by default
        return false;
    }

}
