package iteration_1;

public class Constants {
    public static final int SERVER_LISTENING_PORT = 69;
    public static final int IH_LISTENING_PORT = 23;
    public static enum runType {QUIET, VERBOSE};
    public static enum Mode { NORMAL, TEST};
    public static final byte[] DATA = {0, 3};
    public static final byte[] ACK = {0, 4};
    public static String currentIteration = "iteration_1";
    public static String clientPath = "/" + Constants.currentIteration + "/client/";
    public static String serverPath = "/" + Constants.currentIteration + "/server/";
}
