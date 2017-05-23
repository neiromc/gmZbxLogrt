import handler.ParseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.LogPair;
import yaml.Config;
import yaml.ConfigHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by neiro on 19.05.17.
 */
public class Application {

    static final Logger logger = LoggerFactory.getLogger(Application.class);

    private static Path savePointFileNamePath;

    private static String processedLogFile;

    //ERRORS
    static final int ERROR_COMMAND_NOT_SUPPORTED = -1;
    static final int ERROR_FILE_NOT_FOUND = -2;
    static final int ERROR_CANT_READ_FILE = -3;
    static final int ERROR_CANT_SAVE_FILE = -4;
    static final int ERROR_CANT_LOAD_FILE = -5;

// TODO: 22.05.17 Move all console out to LOG except RESULT

    private static Config config;

    public static void main(String[] args) {

        // ^.*(com.apple).*(mdworker).*(pushing respawn).*$
//        String s1 = "may 23 09:09:58 nibiruqsilver com.apple.xpc.launchd[1] (com.apple.mdworker.single.05000000-0000-0000-0000-000000000000): service only ran for 6 seconds. pushing respawn out by 4 seconds.";
//
//        Pattern p = Pattern.compile("^.*(com.apple).*(mdworker).*(pushing respawn).*$");
//        System.out.println(p.matcher(s1).matches());
//
//        System.exit(0);




        long startupTime = System.currentTimeMillis();

        logger.info("Running application...");

        ConfigHandler configHandler = ConfigHandler.getInstanse();
        if ( ! configHandler.load(args)) {
            System.exit(0);
        } else {
            config = configHandler.getConfig();

            savePointFileNamePath = Paths.get(config.save_point);
            processedLogFile = config.log_file;
        }

//        if ( ! CommandChecker.isCommandSupported(config.handler.type) ) {
//            logger.error("Error. Command '{}' not supported.", config.handler.type);
//            System.out.println(ERROR_COMMAND_NOT_SUPPORTED);
//            System.exit(0);
//        }

//        logger.debug("{} array: {}", config.handler.type.toUpperCase(), Arrays.toString(config.handler.array));

        Path logFilePath = Paths.get(processedLogFile);

        //load last log
        long startLineSeq = 0;

        if ( Files.exists(savePointFileNamePath) ) {
            LogPair logPair = loadLastLogFile(savePointFileNamePath);

            if ( logPair != null ) {
                try {
                    long fileSizeLog = Files.size(logFilePath);
                    long fileSizeLogLast = logPair.getFileSize();
                    logger.info("Log file size (fileSizeLog): {}, Last file size (fileSizeLogLst): {}, Decision: {}",
                            fileSizeLog,
                            fileSizeLogLast,
                            (fileSizeLog == fileSizeLogLast) ? "No changes detected, skip" : "Has a changes, will be processed"
                    );

                    if ( fileSizeLog > fileSizeLogLast ) {
                        // log file is appended
                        logger.info("File is appended");
                        startLineSeq = logPair.getSeq();
                    } else
                    if ( fileSizeLog < fileSizeLogLast ) {
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
            logger.error("Save point file not found. Create new: " + savePointFileNamePath);
        }

        // get lines
        List<String> al = readLogFile(logFilePath, startLineSeq);

        // processing
        ParseHandler parseHandler = new ParseHandler(config, al);

//                new String[]{"com.apple","mdworker", "Pushing respawn"},
//        int result = parseHandler.handleByRegex(config.handler.regex, config.handler.case_sensitivity);
        int result = parseHandler.handleByRegex();

//        if ( result == -1 ) {
//            System.out.printf("Error. Command '%s' not supported.\n", "some_test");
//        } else {
            logger.info("Result: {} lines found", result);
            System.out.println(result);
//        }

        logger.info("Processed {} lines at {} ms", al.size(), (System.currentTimeMillis() - startupTime));
        logger.info("Complete");

    }



    public static List<String> readLogFile(Path filePath, long startSeq) {
        List<String> al = new ArrayList<>();

        logger.info("Try to loading log file: " + filePath.toAbsolutePath());

        try {
            long fileSize = Files.size(filePath);
            al = Files.readAllLines(filePath);

            int lastSeq = al.size();
            logger.info("Start line (startSeq): " + startSeq);
            logger.info("Log file lines count:  " + lastSeq);

            long idx = startSeq;
            while ( idx > 0 ) {
                al.remove(0);
                idx--;
            }

//            for (String s : al) {
//                    System.out.printf("%10d\t%s\n", al.indexOf(s) + (lastSeq - al.size()) + 1, s);
//            }

            logger.info("Processed lines count: " + al.size());
            saveLastLogFile(savePointFileNamePath, new LogPair(lastSeq, fileSize));

        } catch (FileNotFoundException e) {
            System.out.println(ERROR_FILE_NOT_FOUND);
            logger.error("File {} not found", filePath.toAbsolutePath());
            if ( logger.isDebugEnabled() ) {
                logger.debug(e.getMessage() + "\n" + e.getCause());
            }
        } catch (IOException e) {
            System.out.println(ERROR_CANT_READ_FILE);
            logger.error("Can't read file {}. I/O error", filePath.toAbsolutePath());
            if ( logger.isDebugEnabled() ) {
                logger.debug(e.getMessage() + "\n" + e.getCause());
            }
        }

        return al;

    }


    public static void saveLastLogFile(Path fileNamePath, LogPair logPair) {
        String s = String.valueOf(logPair.getSeq()) + "\n" + logPair.getFileSize();
        logger.info("Save lastLog ({})... => seq: {}, fileSize: {}",
                fileNamePath.toAbsolutePath(),
                String.valueOf(logPair.getSeq()),
                logPair.getFileSize()
        );

        try {
            Files.write(fileNamePath, s.getBytes()).toFile();
        } catch (IOException e) {
            System.out.println(ERROR_CANT_SAVE_FILE);
            logger.error("Can't save file {}. I/O error", fileNamePath.toAbsolutePath());
            if ( logger.isDebugEnabled() ) {
                logger.debug(e.getMessage() + "\n" + e.getCause());
            }
        }
    }

    public static LogPair loadLastLogFile(Path fileNamePath) {
        logger.info("Trying loading last log from: " + fileNamePath.toAbsolutePath());

        List<String> al;
        LogPair logPair = new LogPair();
        try {
            al = Files.readAllLines(fileNamePath);
            if ( al.size() == 2 ) {
                logPair.setSeq(Long.parseLong(al.get(0)));
                logPair.setFileSize(Long.parseLong(al.get(1)));
            } else
                logger.info(fileNamePath + " size = " + al.size());

            logger.info("Succesfully get data: " + logPair.toString());

        } catch (IOException e) {
            System.out.println(ERROR_CANT_LOAD_FILE);
            logger.error("Can't load file {}. I/O error", fileNamePath.toAbsolutePath());
            if ( logger.isDebugEnabled() ) {
                logger.debug(e.getMessage() + "\n" + e.getCause());
            }
        }

        return logPair;

    }

}
