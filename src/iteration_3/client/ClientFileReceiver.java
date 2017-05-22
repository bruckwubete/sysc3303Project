package iteration_3.client;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import iteration_3.client.Client;
import iteration_3.*;


public class ClientFileReceiver extends iteration_3.FileReceiver{
    
    int count = 0;
    int expectedReceivePort;
    

    public ClientFileReceiver(Constants.runType runType){
            printer = new PrintService(runType);
            try{
                sendReceiveSocket = new DatagramSocket();
            } catch(Exception e){
                e.printStackTrace();
                return;
            }
    }

    public void receive(String filename, int destPort, InetAddress destAddress) throws Exception {

        FileOutputStream bufferedWriter = null;
        
        byte[] ack = new byte[4];
        
        try{
           
            File f = new File(filename);
/*            if(f.exists() && !f.isDirectory()) {
                String[] filePathArray ="";
                if(SystemUtils.IS_OS_WINDOWS) filePathArray = filename.split("\\\\");  
                if(SystemUtils.IS_OS_LINUX) filePathArray = filename.split("/");
                filePathArray[filePathArray.length - 1] = "Copy" + filePathArray[filePathArray.length - 1];
                filename = String.join("/", filePathArray);                
            }
*/          
            bufferedWriter = new FileOutputStream(filename);

            byte[] data = new byte[516];

            boolean errorEncountered = false;
            
            do{ 
            	errorEncountered = false;
                receivePacket = new DatagramPacket(data, data.length);
                printer.printMessage("Receiving on: " + sendReceiveSocket.getLocalPort());
                sendReceiveSocket.receive(receivePacket);
                
                count++;
                
                printer.printPacketInfo("FileReceiver", "Receive", receivePacket);
                    
                if(count  == 1 && Client.mode != Constants.Mode.TEST){
                    sendPort = receivePacket.getPort();
                } 
                if(count  == 1){
                    expectedReceivePort = receivePacket.getPort();
                } 
                
                if (expectedReceivePort != receivePacket.getPort()){
                	errorEncountered = true;
                    printer.printMessage("Encountered an error packet: UNKNOWN TRANSER ID");
                    byte[] errorCode = Helper.formErrorPacket(Constants.UNKNOWN_TRANSFER_ID, "Packet received from invalid port");
                    DatagramPacket invalidOpcode = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), receivePacket.getPort());
                    printer.printPacketInfo("FileReceiver", "Sending", invalidOpcode);
                    sendReceiveSocket.send(invalidOpcode);
                    printer.printMessage("Sending Error Code \n");
                    
                }else{
    	    		if (Helper.isErrorFourResponseValid(receivePacket)){
    	    			System.out.println("Error Packet Received: Illegal TFTP operation");
    	    		    bufferedWriter.close();
    	    		    f = new File(filename);
    	    		    Files.delete(Paths.get(f.getAbsolutePath()));
    	    		    return;
    	    		}
                    else if(!Helper.isDataOpCodeValid(receivePacket)){
                    	System.out.println("Error Ack Packet Received");
                        byte[] errorCode = Helper.formErrorPacket(Constants.ILLEGAL_TFTP_OPERATION, "Invalid Ack");
                        DatagramPacket invalidOpcode = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), receivePacket.getPort());
                        sendReceiveSocket.send(invalidOpcode);
                        printer.printMessage("Sending Error Code");
                        printer.printPacketInfo("FileReceiver", "Sending", receivePacket);
                        bufferedWriter.close();
                        f = new File(filename);
                        Files.delete(Paths.get(f.getAbsolutePath()));
    	    		    return;
                    }
                    else{
                        bufferedWriter.write(Helper.dataExtractor(receivePacket));         
                        System.arraycopy(Constants.ACK, 0, ack, 0, 2);
                        System.arraycopy(Arrays.copyOfRange(receivePacket.getData(), 2, 4), 0, ack, 2, 2);
                        sendPacket(sendPort, destAddress, ack);
                        
                    }
                    
                   
                }

            }while(receivePacket.getLength() == 516 || errorEncountered);
            
            printer.printMessage("Successfully finished a read transaction! \n\n");
            
        }catch (UnknownHostException e) {
        	throw new Exception(e.getMessage());
        }
    	catch (IOException e){
            if (e.getMessage().contains("space left on device")){
                   byte[] errorCode = Helper.formErrorPacket(Constants.DISK_FULL, "Client out of space");
                   DatagramPacket errorPacket = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), sendPort);
                   sendReceiveSocket.send(errorPacket);
	           }      
	           
    		throw new Exception(e.getMessage());
    	}finally{
            try{
                if(bufferedWriter!=null){
                    bufferedWriter.close();
                }            
            }
            catch (IOException e){
            	throw new Exception(e.getMessage());
            }
        }        
    }
}
