package handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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

    public int handleByRegex(String regexp, boolean caseSensitivity) {
        int linesCount = 0;

        String regexpStr = regexp;
        if ( ! caseSensitivity )
            regexpStr = regexpStr.toLowerCase();

        Pattern p = Pattern.compile(regexpStr);

        String sourceStr;

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

        return linesCount;
    }

}
