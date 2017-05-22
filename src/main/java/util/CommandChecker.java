package util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by neiro on 22.05.17.
 */
public class CommandChecker {

    private static Map<String, ParseType> supportedCommandsMap;

    static {
        supportedCommandsMap = new HashMap<>();

        supportedCommandsMap.put("grep", ParseType.GREP);
        supportedCommandsMap.put("regex", ParseType.REGEX);
    }

    public static boolean isCommandSupported(String command) {
        return supportedCommandsMap.containsKey(command);
    }

}
