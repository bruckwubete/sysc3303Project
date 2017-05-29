package iteration_4;
import java.net.*;
import java.io.*;
import java.util.Arrays;

public class Helper {

    public static String getFilename(DatagramPacket writeRequestPacket){

	    byte[] packetData = writeRequestPacket.getData();

	    //find location of first 0
	    int index = 2;

	    for (int i = 2; i < packetData.length; i++ ) {
	        if (packetData[i] == 0) {
	            break;
	        }
	        index++;
	    }  

        return new String(Arrays.copyOfRange(packetData, 2, index));

     }
     
     public static byte[] dataExtractor(DatagramPacket dataPacket){
         return Arrays.copyOfRange(dataPacket.getData(), 4, dataPacket.getLength());
     }
     
    public static boolean isReadOpCodeValid(DatagramPacket a){
        byte[] data = a.getData();
        
        return (data[0]==0 && data[1]==1);
    }

        
    public static boolean isWriteOpCodeValid(DatagramPacket a){
        byte[] data = a.getData();

        return (data[0]==0 && data[1]==2);
    }
        
    public static boolean isDataOpCodeValid(DatagramPacket a){
        byte[] data = a.getData();
        
        return (data[0]==0 && data[1]==3);
    }
        
    public static boolean isAckOpCodeValid(DatagramPacket a){
        byte[] data = a.getData();
        
        return (data[0]==0 && data[1]==4);
    }
    
    public static boolean isDataLengthValid(DatagramPacket a){
        byte[] data = a.getData();
        
        return (data.length <= 516);
    }
    
    public static boolean isErrorOneResponseValid(DatagramPacket a){
        byte[] data = a.getData();

        return (data[0]==0 && data[1]==5 && data[2]==0 && data[3]==1);
    }
    
    public static boolean isErrorTwoResponseValid(DatagramPacket a){
        byte[] data = a.getData();

        return (data[0]==0 && data[1]==5 && data[2]==0 && data[3]==2);
    }
    
    public static boolean isErrorThreeResponseValid(DatagramPacket a){
        byte[] data = a.getData();

        return (data[0]==0 && data[1]==5 && data[2]==0 && data[3]==3);
    }
    
    public static boolean isErrorFourResponseValid(DatagramPacket a){
        byte[] data = a.getData();

        return (data[0]==0 && data[1]==5 && data[2]==0 && data[3]==4);
    }
    
    public static boolean isErrorFiveResponseValid(DatagramPacket a){
        byte[] data = a.getData();

        return (data[0]==0 && data[1]==5 && data[2]==0 && data[3]==5);
    }
    
    public static byte[] formReadPacket(String filename, String mode){
        byte[] packet = new byte[filename.length() + mode.length() + 4];
        System.arraycopy(Constants.READ, 0, packet, 0, 2);
        System.arraycopy(filename.getBytes(), 0, packet, 2, filename.getBytes().length);
        packet[filename.getBytes().length + 2] = (byte)0;
        System.arraycopy(mode.getBytes(), 0, packet, filename.getBytes().length + 3, mode.getBytes().length);
        packet[filename.getBytes().length + mode.getBytes().length + 3] = (byte)0;
        
        return packet;
    }
    
    public static byte[] formWritePacket(String filename, String mode){
        byte[] packet = new byte[filename.length() + mode.length() + 4];
        System.arraycopy(Constants.WRITE, 0, packet, 0, 2);
        System.arraycopy(filename.getBytes(), 0, packet, 2, filename.getBytes().length);
        packet[filename.getBytes().length + 2] = (byte)0;
        System.arraycopy(mode.getBytes(), 0, packet, filename.getBytes().length + 3, mode.getBytes().length);
        packet[filename.getBytes().length + mode.getBytes().length + 3] = (byte)0;
        
        return packet;
    }
    
    public static byte[] formDataPacket(byte[] blockNumber, byte[] data){
        byte[] packet = new byte[blockNumber. length + data.length + 2];
        System.arraycopy(Constants.DATA, 0, packet, 0, 2);
        System.arraycopy(blockNumber, 0, packet, 2, 2);
        System.arraycopy(data, 0, packet, 4, data.length);
        
        return packet;
    }
    
    public static byte[] formAckPacket(byte[] blockNumber){
        byte[] packet = new byte[blockNumber.length + 2];
        System.arraycopy(Constants.ACK, 0, packet, 0, 2);
        System.arraycopy(blockNumber, 0, packet, 2, 2);
        
        return packet;
    }

    public static byte[] formErrorPacket(byte[] errorCode, String errorMessage){
        byte[] packet = new byte[errorCode.length + errorMessage.getBytes().length + 3];
        System.arraycopy(Constants.ERROR, 0, packet, 0, 2);
        System.arraycopy(errorCode, 0, packet, 2, 2);
        System.arraycopy(errorMessage.getBytes(), 0, packet, 4, errorMessage.getBytes().length);
        packet[errorMessage.getBytes().length + 4] = (byte)0;
        
        return packet;
    }
    
    public static byte[] getMode (DatagramPacket packet){
        byte[] packetData = packet.getData();
        int startOfMode = 0;
        int endOfMode = 0;
        for (int i = 2; i < packet.getLength(); i++ ){
            if (packetData[i] == 0){
                startOfMode = i + 1;
                break;
            }
        }
        endOfMode = packet.getLength() - 1;
        byte [] mode = Arrays.copyOfRange(packetData, startOfMode, endOfMode);
        return mode;
    }
    
    public static byte[] makeModeInvalid(byte[] packetData){
        
        int startOfMode = 0;
        int endOfMode = 0;
        for (int i = 2; i < packetData.length; i++ ){
            if (packetData[i] == 0){
                startOfMode = i + 1;
                break;
            }
        }
        endOfMode = packetData.length - 1;
        
        for (int i = startOfMode; i < endOfMode; i++){
            packetData[i] = 5;      
        }
        return packetData;
    }
    
    public static boolean isModeValid(DatagramPacket packet){
        byte[] mode = Helper.getMode(packet);        
        return (Arrays.equals(mode, Constants.FILE_MODES[0]) || Arrays.equals(mode, Constants.FILE_MODES[1]));    
    }
    
    public static boolean validReadWriteRequest(DatagramPacket packet){
        if(Helper.isReadOpCodeValid(packet) || Helper.isWriteOpCodeValid(packet)){
            if(packet.getData()[3]!= 0 && packet.getData()[packet.getLength()-1] == 0 && packet.getData()[packet.getLength()-2] != 0){
                return true;
            }
        }
        return false;
    }
    
    public static boolean doesFileAlreadyExistOnServer(String fileName){
        if (new File(System.getProperty("user.dir").toString() + Constants.getServerPath() + fileName).isFile()){           
            return true;
        }
        return false;    
    }
    
    public static long isThereEnoughSpaceOnServer(){        
        File drive = new File (Constants.getServerPath() );
        long freeSpace = drive.getFreeSpace();
        return freeSpace;
    }
    
    public static long isThereEnoughSpaceOnClient(){
        
        File drive = new File (Constants.getClientPath());
        long freeSpace = drive.getFreeSpace();
        return freeSpace;
    }
    
    public static String getOS(){
        return System.getProperty("os.name").toLowerCase();
    }
    
    public static byte[] getRequestData(String filename, String requestType) throws IOException{
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

            if(requestType.equals("read")){
	            stream.write(Constants.READ);			 
	    	}else{
		    	stream.write(Constants.WRITE);
    		}
            
    		if(Helper.getOS().contains("win")){
    		    String[] filePathArray = filename.split("\\\\");
    		    filename = filePathArray[filePathArray.length - 1];    
    		} 
    		else{
    		   String[] filePathArray = filename.split("/");
    		   filename = filePathArray[filePathArray.length - 1];    
    		}
    		
    		


        	stream.write(filename.getBytes());
	    	stream.write(0);
	    	stream.write(Constants.FILE_MODES[0]);
	    	stream.write(0);
	    	return stream.toByteArray();
    }
    
}
