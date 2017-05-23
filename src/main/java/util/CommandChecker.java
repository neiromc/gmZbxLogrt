package util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by neiro on 22.05.17.
 */
public class CommandChecker {

//    private static Map<String, ParseType> supportedCommandsMap;
    private static List<String> supportedCommandsList;

    static {
//        supportedCommandsMap = new HashMap<>();
        supportedCommandsList = new ArrayList<>();

        // Supported
//        supportedCommandsMap.put("grep", ParseType.GREP);
        supportedCommandsList.add("grep");
        supportedCommandsList.add("regex");

        // May supported in future (see implementation in ParseHandler.handle)
//        supportedCommandsMap.put("regex", ParseType.REGEX);
    }

    public static boolean isCommandSupported(String command) {
        return supportedCommandsList.contains(command);
//        return supportedCommandsMap.containsKey(command);
    }

}
