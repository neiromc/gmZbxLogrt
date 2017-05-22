package handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by neiro on 22.05.17.
 */
public class GrepHandler {

    private List<String> lines = new ArrayList();


    public GrepHandler(final List lines) {
        this.lines = lines;
    }

    public int handle(String grepType, String[] grepStrings, boolean useCaseSensitivity) {
        if ( grepType.equals("grep"))
           return handleByGrep(grepStrings, useCaseSensitivity);

        return -1;
    }

    //type, array, use_case_sensitivity
    //"grep", a["com.apple","mdworker", "Pushing respawn"], false
    private int handleByGrep(String[] grepStrings, boolean useCaseSensitivity) {

        System.out.printf("Try to find: %s\n", Arrays.toString(grepStrings));

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
                System.out.printf("%4d %d  =>  %s\n", i, grepCount, sourceStr);
                linesCount++;
            }

        }

        return linesCount;
    }

}
