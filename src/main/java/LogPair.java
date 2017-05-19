/**
 * Created by neiro on 19.05.17.
 */
public class LogPair {

    private long seq;
    private String line;
    private long fileSize;

    public LogPair() {
    }

    public LogPair(final long seq,
                   final String line,
                   final long fileSize)
    {
        this.seq = seq;
        this.line = line;
        this.fileSize = fileSize;
    }

    public long getSeq() {
        return seq;
    }

    public String getLine() {
        return line;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {
        return "LogPair{" +
                "seq=" + seq +
                ", line='" + line + '\'' +
                '}';
    }
}
