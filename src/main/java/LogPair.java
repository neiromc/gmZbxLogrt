/**
 * Created by neiro on 19.05.17.
 */
public class LogPair {

    private long seq;
    private long fileSize;

    public LogPair() {
    }

    public LogPair(final long seq,
                   final long fileSize)
    {
        this.seq = seq;
        this.fileSize = fileSize;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
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
                ", fileSize=" + fileSize +
                '}';
    }
}
