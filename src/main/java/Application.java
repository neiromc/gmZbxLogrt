import handler.ParseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ErrorCodes;
import yaml.Config;
import yaml.ConfigHandler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        List<String> al = readLogFile(logFilePath, startLineSeq, charset);
        if ( al.size() == 0 ) {
//            logger.error("File {} read failed. Exit", logFilePath);
            System.exit(0);
        }


        // processing
        ParseHandler parseHandler = new ParseHandler(config, al);

        int result = parseHandler.handleByRegex();
        logger.info("Result: {} lines found", result);
        System.out.println(result);

        logger.info("Processed {} lines at {} ms", al.size(), (System.currentTimeMillis() - startupTime));
        logger.info("Complete");

    }



    private static List<String> readLogFile(Path filePath, long startSeq, String charset) {
        List<String> al = new ArrayList<>();

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
                al.add(line);
            }

            br.close();

            //long lastSeq = fileSize;
            logger.info("Start line (startSeq): " + startSeq);
            logger.info("Log file size:  " + fileSize);

            logger.info("Processed lines count: " + al.size());
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

        return al;

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
