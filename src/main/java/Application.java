import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by neiro on 19.05.17.
 */
public class Application {

//    public static String lastLogFileNamePath = "LastLogFileOptions.dat";
    public static Path lastLogFileNamePath = Paths.get("LastLogFileOptions.dat");

    public static void main(String[] args) {

        Path logFilePath = Paths.get("/var/log/system.log");

        //load last log
        long startLineSeq = 0;

        if ( Files.exists(lastLogFileNamePath) ) {
            LogPair logPair = loadLastLogFile(lastLogFileNamePath);

            if ( logPair != null ) {
                try {
                    long fileSizeLog = Files.size(logFilePath);
                    long fileSizeLogLast = logPair.getFileSize();
                    System.out.println("Log file size (fileSizeLog): " + fileSizeLog +
                            ", Last file size (fileSizeLogLst): " + fileSizeLogLast +
                    ", isEquals=" + (fileSizeLog == fileSizeLogLast));

                    if ( fileSizeLog > fileSizeLogLast ) {
                        // log file is appended
                        System.out.println("File is appended");
                        startLineSeq = logPair.getSeq();
                    } else
                    if ( fileSizeLog < fileSizeLogLast ) {
                        // log file is rotated
                        System.out.println("File is rotated");
                        startLineSeq = 0;
                    } else {
                        // log file does not have changes
                        System.out.println("No changes in file. Skip");
                        System.exit(0);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        } else {
            System.out.println("File not found: " + lastLogFileNamePath);
        }

        List<String> al = readLogFile(logFilePath, startLineSeq);
        // TODO: 19.05.17 write method to grep and solve count of actual lines from $al

    }

    public static List<String> readLogFile(Path filePath, long startSeq) {
        List<String> al = new ArrayList<>();

        System.out.println("Try to loading log file: " + filePath.toAbsolutePath());

        try {
            long fileSize = Files.size(filePath);
            al = Files.readAllLines(filePath);

            int lastSeq = al.size();
            System.out.println("Start line (startSeq): " + startSeq);
            System.out.println("Log file lines count:  " + lastSeq);

            long idx = startSeq;
            while ( idx > 0 ) {
                al.remove(0);
                idx--;
            }

            for (String s : al) {
                    System.out.printf("%10d\t%s\n", al.indexOf(s) + (lastSeq - al.size()) + 1, s);
            }

            System.out.println("Processed line count: " + al.size());
            saveLastLogFile(lastLogFileNamePath, new LogPair(lastSeq,al.get(al.size() - 1), fileSize));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return al;

    }


    public static void saveLastLogFile(Path fileNamePath, LogPair logPair) {
        String s = String.valueOf(logPair.getSeq()) + "\n" + logPair.getLine() + "\n" + logPair.getFileSize();
        try {
            Files.write(fileNamePath, s.getBytes()).toFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static LogPair loadLastLogFile(Path fileNamePath) {
        List<String> al;
        LogPair logPair = new LogPair();
        try {
            al = Files.readAllLines(fileNamePath);
            if ( al.size() == 3 ) {
                logPair.setSeq(Long.parseLong(al.get(0)));
                logPair.setLine(al.get(1));
                logPair.setFileSize(Long.parseLong(al.get(2)));
            } else
                System.err.println(fileNamePath + " size = " + al.size());

            System.out.println("Loading from " + fileNamePath.toAbsolutePath());
            System.out.println(logPair.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return logPair;

    }

}
