package iteration_4;

public class Constants {
    public static final int SERVER_LISTENING_PORT = 69;
    public static final int IH_LISTENING_PORT = 23;
    public static enum requestType {READ, WRITE};
    public static enum runType {QUIET, VERBOSE};
    public static final byte[][] FILE_MODES = {"ocTEt".toLowerCase().getBytes(), "netASCII".toLowerCase().getBytes()};
    public static enum Mode {NORMAL, TEST};
    public static final byte[] READ = {0, 1};
    public static final byte[] WRITE = {0, 2};
    public static final byte[] DATA = {0, 3};
    public static final byte[] ACK = {0, 4};
    public static final byte[] ERROR = {0, 5};
    public static final byte[] FILE_NOT_FOUND = {0, 1};
    public static final byte[] ACCESS_VIOLATION = {0, 2};
    public static final byte[] DISK_FULL = {0, 3};
    public static final byte[] ILLEGAL_TFTP_OPERATION = {0, 4};
    public static final byte[] UNKNOWN_TRANSFER_ID = {0, 5};
    public static enum packetType {REQUEST, DATA, ACK};
    public static String currentIteration = "iteration_4";
    public static String fileSystemDrive = "D:";
    public static int SENDER_TIMEOUT = 3000;
    public static String serverReadWriteLocation = "";
    public static String clientReadWriteLocation = "";


    public static String getServerPath(){

        if(Helper.getOS().contains("win")){
            return "\\src\\" + Constants.currentIteration + "\\server\\";
        } else if (Helper.getOS().contains("linux")){
            return "/" + Constants.currentIteration + "/server/";
        }
        return "";
    }
    
    public static String getClientPath(){

        if(Helper.getOS().contains("win")){
            return "\\src\\" + Constants.currentIteration + "\\client\\";
        } else if (Helper.getOS().contains("linux")){
            return "/" + Constants.currentIteration + "/client/";
        }
        return "";
    }
    
    public static String getSlash(){
        if(Helper.getOS().contains("win")){
            return "\\";
        } else if (Helper.getOS().contains("linux")){
            return "/";
        }
        return "";
    }
}
