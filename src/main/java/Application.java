import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ErrorCodes;
import yaml.Config;
import yaml.ConfigHandler;
import yaml.GrepTypes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by neiro on 19.05.17.
 */
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private static Path savePointFileNamePath;

    private static String processedLogFile;

    private static Config config;

    private static final String savePointFileName = "save_point_%s.dat";

    public static void main(String[] args) {

        long startupTime = System.currentTimeMillis();

        logger.info("Running application...");

        ConfigHandler configHandler = ConfigHandler.getInstanse();
        if ( ! configHandler.load(args)) {
            System.exit(0);
        } else {
            config = configHandler.getConfig();

            savePointFileNamePath = Paths.get(config.install_dir
                    + "/" + String.format(savePointFileName,config.save_point_suffix));

            processedLogFile = config.handler.file_name;
        }

        Path logFilePath = Paths.get(processedLogFile);

        //load last log
        long startLineSeq = 0;

        if ( Files.exists(savePointFileNamePath) ) {
            long savePointFileSize = loadLastLogFile(savePointFileNamePath);

            if ( savePointFileSize >= 0 ) {
                try {
                    long fileSizeLog = 0;
                    if ( Files.exists(logFilePath) )
                        fileSizeLog = Files.size(logFilePath);
                    else {
                        logger.error("File {} not found", logFilePath.toAbsolutePath());
                        System.exit(0);
                    }

                    logger.info("Log file size (fileSizeLog): {}, Last file size (fileSizeLogLst): {}, Decision: {}",
                            fileSizeLog,
                            savePointFileSize,
                            (fileSizeLog == savePointFileSize) ? "No changes detected, skip" : "Has a changes, will be processed"
                    );

                    if ( fileSizeLog > savePointFileSize ) {
                        // log file is appended
                        logger.info("File is appended");
                        startLineSeq = savePointFileSize;
                    } else
                    if ( fileSizeLog < savePointFileSize ) {
                        // log file is rotated
                        logger.info("File is rotated");
                        startLineSeq = 0;
                    } else {
                        // log file does not have changes
                        logger.info("Processed 0 lines at {} ms", (System.currentTimeMillis() - startupTime));
                        logger.info("Complete (skip)");
                        System.out.println(0);
                        System.exit(0);


                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        } else {
            logger.warn("Save point file not found. Create new: " + savePointFileNamePath);
        }

        // get lines
        String charset = (config.handler.charset == null) ? "utf-8" : config.handler.charset;
        logger.info("Grep type \"{}\" in array {}",
                config.handler.grep.type,
                Arrays.toString(config.handler.grep.expression));
        long[] result = checkLogFile(logFilePath, startLineSeq, charset, config);

//        System.out.println("total: " + result[0]);
//        System.out.println("matched: " + result[1]);

        long totalTime = System.currentTimeMillis() - startupTime;
        double speed = result[0] / totalTime * 1000;

        logger.info("Total lines is {}. Find {} lines at {} ms ({} lines/sec)", result[0], result[1], totalTime, speed);
        logger.info("Complete");

        System.out.println(result[1]);

    }



    private static long[] checkLogFile(Path filePath, long startSeq, String charset, Config config) {
        GrepTypes grepType = config.handler.grep.type;
        String[] grepArray = config.handler.grep.expression;
        boolean caseSensitivity = config.handler.case_sensitivity;

        long countTotal = 0;
        long countMatched = 0;

        logger.info("Try to loading log file: " + filePath.toAbsolutePath());

        try {
            long fileSize = Files.size(filePath);

            BufferedReader br = Files.newBufferedReader(filePath, Charset.forName(charset));
            if (startSeq > 0) {
                logger.info("Skip old bytes. Move to {} byte", startSeq);
                br.skip(startSeq);
            }

            String line;
            while ( (line = br.readLine()) != null ) {

                if (! caseSensitivity )
                    line = line.toLowerCase();

                if ( getGrepResult(line, grepArray, caseSensitivity, grepType) )
                    countMatched++;

                countTotal++;
            }

            br.close();

            //long lastSeq = fileSize;
            logger.info("Start line (startSeq): " + startSeq);
            logger.info("Log file size:  " + fileSize);

            logger.info("Processed lines count {}, matched {} lines ", countTotal, countMatched);
            saveLastLogFile(savePointFileNamePath, fileSize);

        } catch (FileNotFoundException e) {
            System.out.println(ErrorCodes.ERROR_FILE_NOT_FOUND);
            logger.error("File {} not found", filePath.toAbsolutePath());
            if ( logger.isDebugEnabled() ) {
                logger.debug(e.getMessage() + "\n" + e.getCause());
            }
        } catch (IOException e) {
            System.out.println(ErrorCodes.ERROR_CANT_READ_FILE);
            logger.error("Can't read file {}. I/O error", filePath.toAbsolutePath());
            if ( logger.isDebugEnabled() ) {
                logger.debug("----------------");
                logger.debug(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
                logger.debug("----------------");
            }
//            e.printStackTrace();
        }

        return new long[]{countTotal, countMatched};

    }

    private static boolean getGrepResult(String s, String[] grepArray, boolean caseSensitivity, GrepTypes grepType) {

        if ( grepType.equals(GrepTypes.AND)) {
            for (String grepStr : grepArray) {
                if (! caseSensitivity)
                    grepStr = grepStr.toLowerCase();

                if (!s.contains(grepStr))
                    return false;
            }
            return true;
        }

        if ( grepType.equals(GrepTypes.OR)) {
            boolean orResult = false;
            for (String grepStr : grepArray) {
                if (! caseSensitivity)
                    grepStr = grepStr.toLowerCase();

                if ( s.contains(grepStr) ) {
                    orResult = true;
                    break;
                }

            }

            return orResult;


        }

        logger.error("Bad type of grep: " + grepType);
        return false;

    }

    private static void saveLastLogFile(Path fileNamePath, long fileSize) {
        //String s = String.valueOf(logPair.getSeq()) + "\n" + logPair.getFileSize();
        logger.info("Save lastLog ({})... => fileSize: {}",
                fileNamePath.toAbsolutePath(),
//                String.valueOf(logPair.getSeq()),
                fileSize
        );

        try {
            Files.write(fileNamePath, String.valueOf(fileSize).getBytes()).toFile();
        } catch (IOException e) {
            System.out.println(ErrorCodes.ERROR_CANT_SAVE_FILE);
            logger.error("Can't save file {}. I/O error", fileNamePath.toAbsolutePath());
            if ( logger.isDebugEnabled() ) {
                logger.debug(e.getMessage() + "\n" + e.getCause());
            }
        }
    }

    private static long loadLastLogFile(Path fileNamePath) {
        logger.info("Trying loading last log from: " + fileNamePath.toAbsolutePath());

        long fileSize;
        try {
            String s = Files.readAllLines(fileNamePath).get(0);
            if ( s != null ) {
                if ( (fileSize = Long.parseLong(s)) > 0 ) {
                    logger.info("Succesfully get fileSize from save point file: {} bytes", fileSize);
                    return Long.parseLong(s);
                }
            }
//            logger.info(fileNamePath + " size = " + al.size());
//            logger.info("Succesfully get data: " + s);

        } catch (IOException e) {
            System.out.println(ErrorCodes.ERROR_CANT_LOAD_FILE);
            logger.error("Can't load file {}. I/O error", fileNamePath.toAbsolutePath());
            if ( logger.isDebugEnabled() ) {
                logger.debug(e.getMessage() + "\n" + e.getCause());
            }
        }

        return -1;

    }

}
