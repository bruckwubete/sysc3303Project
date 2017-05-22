package iteration_2;

public class Constants {
    public static final int SERVER_LISTENING_PORT = 69;
    public static final int IH_LISTENING_PORT = 23;
    public static enum requestType {READ, WRITE};
    public static enum runType {QUIET, VERBOSE};
    public static final byte[][] FILE_MODES = {"ocTEt".toLowerCase().getBytes(), "netASCII".toLowerCase().getBytes()};
    public static enum Mode { NORMAL, TEST};
    public static final byte[] READ = {0, 1};
    public static final byte[] WRITE = {0, 2};
    public static final byte[] DATA = {0, 3};
    public static final byte[] ACK = {0, 4};
    public static final byte[] ERROR = {0, 5};
    public static final byte[] ILLEGAL_TFTP_OPERATION = {0, 4};
    public static final byte[] UNKNOWN_TRANSFER_ID = {0, 5};
    public static String currentIteration = "iteration_2";
    public static String clientPath = "\\src\\" + Constants.currentIteration + "\\client\\";
    public static String serverPath = "\\src\\" + Constants.currentIteration + "\\server\\";
}
