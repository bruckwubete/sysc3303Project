package iteration_3.server;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Arrays;

import iteration_3.*;

public class ServerFileReceiver extends iteration_3.FileReceiver {
    
    public ServerFileReceiver(Constants.runType runType){
        printer = new PrintService(runType);
        try{
            sendReceiveSocket = new DatagramSocket();
        } catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public void receive(String filename, int destPort, InetAddress destAddress) throws FileNotFoundException, IOException, Exception {

        FileOutputStream bufferedWriter = null;
        
        byte[] ack = new byte[4];
//        int count = 0;
        
        try{
           
            File f = new File(filename);
/*            if(f.exists() && !f.isDirectory()) {
                if(SystemUtils.IS_OS_WINDOWS) String[] filePathArray = filename.split("\\\\");   
                if(SystemUtils.IS_OS_LINUX) String[] filePathArray = filename.split("/");   
                filePathArray[filePathArray.length - 1] = "Copy" + filePathArray[filePathArray.length - 1];
                filename = String.join("/", filePathArray);                
            }
*/           

            if(f.exists() && !f.isDirectory() && !Files.isWritable(f.toPath())){
                printer.printMessage("File access violation");
                byte[] errorCode = Helper.formErrorPacket(Constants.ACCESS_VIOLATION, "File access violation");
                DatagramPacket invalidOpcode = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), destPort);                    
                DatagramSocket errorSocket = new DatagramSocket();
                errorSocket.send(invalidOpcode);
                errorSocket.close();
            }
            else{
                bufferedWriter = new FileOutputStream(filename);
               
    
                byte[] data = new byte[516];
    
                boolean errorEncountered = false;
                
                do{ 
                	errorEncountered = false;
                    receivePacket = new DatagramPacket(data, data.length);
                    printer.printMessage("Receiving on: " + sendReceiveSocket.getLocalPort());
                    sendReceiveSocket.receive(receivePacket);
                    if (!Helper.isDataOpCodeValid(receivePacket) ){
    	    		    throw new Exception("DATA BLOCK OPCode IS NOT VALID");
                    }
                    
                    
                    printer.printPacketInfo("FileReceiver", "Receive", receivePacket);
                    
                    if(destPort != receivePacket.getPort()){
                    	errorEncountered = true;
                        System.out.println("Encountered an error packet: UNKNOWN TRANSER ID");
                        byte[] errorCode = Helper.formErrorPacket(Constants.UNKNOWN_TRANSFER_ID, "Packet received from invalid port");
                        DatagramPacket invalidOpcode = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), receivePacket.getPort());
                        sendReceiveSocket.send(invalidOpcode);
                        System.out.println("Sending Error Code");
                        
                    }
                    else{
        	    		if (Helper.isErrorFourResponseValid(receivePacket)){
        	    		    System.out.println("Error Packet Received: Illegal TFTP operation");
        	    		    File file = new File(filename);
                            file.delete();
                           Thread.currentThread().interrupt();
                           return;
        	    		}    
                        else if(!Helper.isDataOpCodeValid(receivePacket)){
                            byte[] errorCode = Helper.formErrorPacket(Constants.ILLEGAL_TFTP_OPERATION, "Invalid Ack");
                            DatagramPacket invalidOpcode = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), receivePacket.getPort());
                            sendReceiveSocket.send(invalidOpcode);
                            System.out.println("Sending Error Code");
                            File file = new File(filename);
                            file.delete();
                           Thread.currentThread().interrupt();
                           return;
                        }
                        else{ 
                            
                            bufferedWriter.write(Helper.dataExtractor(receivePacket));
                            
                            System.arraycopy(Constants.ACK, 0, ack, 0, 2);
                            System.arraycopy(Arrays.copyOfRange(receivePacket.getData(), 2, 4), 0, ack, 2, 2);
    
                            sendPacket(destPort, destAddress, ack);
                        }
                    }
                }while(receivePacket.getLength() == 516 || errorEncountered);
            }

        }
        catch (UnknownHostException e) {
        	System.err.println(e.getMessage());    
        	System.exit(1);
        }
    	catch (IOException e){
            if (e.getMessage().contains("space left on device")){
                   byte[] errorCode = Helper.formErrorPacket(Constants.DISK_FULL, "Server out of space");
                   DatagramPacket errorPacket = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), destPort);
                   sendReceiveSocket.send(errorPacket);
	        }      
    		System.err.println(e.getMessage());
    		System.exit(1);
    	}
        finally{
            try{
                if(bufferedWriter!=null){
                    bufferedWriter.close();
                }            
            }
            catch (IOException e){
            	System.err.println(e.getMessage());
                System.exit(1);
            }
        }        
    }
    
    
}
