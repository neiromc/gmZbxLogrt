package handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by neiro on 22.05.17.
 */
public class ParseHandler {

    private static final Logger logger = LoggerFactory.getLogger(ParseHandler.class);

    private List<String> lines = new ArrayList();


    public ParseHandler(final List lines) {
        this.lines = lines;
    }

//    public int handle(String[] grepStrings, boolean useCaseSensitivity) {
////        if ( grepType.equals("grep"))
//           return handleByGrep(grepStrings, useCaseSensitivity);
//
////        logger.debug("Handle method return -1. Always is mean that command"
////                + " not supported. In your case the command is {}", grepType);
////        return -1;
//    }

//    public int handle(String regex, boolean useCaseSensitivity) {
//        if ( grepType.equals("grep"))
//            return handleByGrep(grepStrings, useCaseSensitivity);
//
//        logger.debug("Handle method return -1. Always is mean that command"
//                + " not supported. In your case the command is {}", grepType);
//        return -1;
//    }



    //type, array, use_case_sensitivity
    //"grep", a["com.apple","mdworker", "Pushing respawn"], false

    /**
     * Handle log lines by grep. All lines from grepStrings array must be matched
     * @param grepStrings matched strings
     * @param caseSensitivity set to false if method must be compare all words from line as lower cased
     * @return number of lines found
     */
    public int handleByGrep(String[] grepStrings, boolean caseSensitivity) {

        logger.info("Try to find: {}", Arrays.toString(grepStrings));

        String sourceStr;
        int linesCount = 0;
        int grepCount;
        for (int i = 0; i < lines.size(); i++) {
            grepCount = 0;
            if (! caseSensitivity )
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
                logger.debug("{} {}  =>  {}", i, grepCount, sourceStr);
                linesCount++;
            }

        }

        return linesCount;
    }

    public int handleByRegex(String regexp, boolean caseSensitivity) {
        int linesCount = 0;
        String regexpStr = regexp;

//        String s1 = "may 23 09:09:58 nibiruqsilver com.apple.xpc.launchd[1] (com.apple.mdworker.single.05000000-0000-0000-0000-000000000000): service only ran for 6 seconds. pushing respawn out by 4 seconds.";
//
//        Pattern p = Pattern.compile("^.*(com.apple).*(mdworker).*(pushing respawn).*$");
        if ( ! caseSensitivity )
            regexpStr = regexpStr.toLowerCase();

        Pattern p = Pattern.compile(regexpStr);
//        System.out.println(p.matcher(s1).matches());

        String sourceStr;
//        int count;

        for (int i = 0; i < lines.size(); i++) {
            if (! caseSensitivity )
                sourceStr = lines.get(i).toLowerCase();
            else
                sourceStr = lines.get(i);

                if ( p.matcher(sourceStr).matches()) {
                    linesCount++;
                    logger.debug("{} {}  =>  {}", i, linesCount, sourceStr);
                }
        }


//        if ( linesCount > 0 )
//            logger.debug("{} {}  =>  {}", i, linesCount, sourceStr);

        return linesCount;
    }

}
