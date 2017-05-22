package handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by neiro on 22.05.17.
 */
public class ParseHandler {

    private static final Logger logger = LoggerFactory.getLogger(ParseHandler.class);

    private List<String> lines = new ArrayList();


    public ParseHandler(final List lines) {
        this.lines = lines;
    }

    public int handle(String grepType, String[] grepStrings, boolean useCaseSensitivity) {
        if ( grepType.equals("grep"))
           return handleByGrep(grepStrings, useCaseSensitivity);

        logger.debug("Handle method return -1. Always is mean that command"
                + " not supported. In your case the command is {}", grepType);
        return -1;
    }

    //type, array, use_case_sensitivity
    //"grep", a["com.apple","mdworker", "Pushing respawn"], false

    /**
     * Handle log lines by grep. All lines from grepStrings array must be matched
     * @param grepStrings matched strings
     * @param useCaseSensitivity set to false if method must be compare all words from line as lower cased
     * @return number of lines found
     */
    private int handleByGrep(String[] grepStrings, boolean useCaseSensitivity) {

        logger.info("Try to find: {}", Arrays.toString(grepStrings));

        String sourceStr;
        int linesCount = 0;
        int grepCount;
        for (int i = 0; i < lines.size(); i++) {
            grepCount = 0;
            if (! useCaseSensitivity )
                sourceStr = lines.get(i).toLowerCase();
            else
                sourceStr = lines.get(i);

            for (int j = 0; j < grepStrings.length; j++) {
                if ( ! sourceStr.contains(grepStrings[j].toLowerCase()) ) {
                    continue;
                }
                grepCount++;
            }


            if ( grepCount == grepStrings.length ) {
                logger.debug("%4d %d  =>  %s\n", i, grepCount, sourceStr);
                linesCount++;
            }

        }

        return linesCount;
    }

}
