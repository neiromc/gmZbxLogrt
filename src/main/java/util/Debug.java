package util;

import org.slf4j.Logger;

/**
 * Created by neiro on 05.06.17.
 */
public class Debug {

    public static void print(Logger logger, Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getMessage());
        sb.append("\n--------- STACK TRACE ---------\n");
        for (StackTraceElement se : e.getStackTrace()) {
            sb.append(se.toString() + "\n");
        }
        sb.append("-------------------------------");

        logger.debug(sb.toString());
    }

}
