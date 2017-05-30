package iteration_4.server;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Arrays;

import iteration_4.*;

public class ServerFileReceiver extends iteration_4.FileReceiver {
    
    int nextDataPacketNumber = 1;
    
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
        
        try{
           
            File f = new File(filename);           

            if(f.exists() && !f.isDirectory() && !Files.isWritable(f.toPath())){
                printer.printMessage("File access violation");
                byte[] errorCode = Helper.formErrorPacket(Constants.ACCESS_VIOLATION, "File access violation");
                DatagramPacket invalidOpcode = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), destPort);                    
                DatagramSocket errorSocket = new DatagramSocket();
                errorSocket.send(invalidOpcode);
                printer.printMessage("Sending Error Code 2");
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
//                    if (!Helper.isDataOpCodeValid(receivePacket) ){
//    	    		    throw new Exception("DATA BLOCK OPCode IS NOT VALID");
//                    }
                    
                    
                    printer.printPacketInfo("FileReceiver", "Receive", receivePacket);
                    
                    if(destPort != receivePacket.getPort()){
                    	errorEncountered = true;
                        System.out.println("Encountered an error packet: UNKNOWN TRANSER ID");
                        byte[] errorCode = Helper.formErrorPacket(Constants.UNKNOWN_TRANSFER_ID, "Packet received from invalid port");
                        DatagramPacket invalidOpcode = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), receivePacket.getPort());
                        sendReceiveSocket.send(invalidOpcode);
                        System.out.println("Sending Error Code 5");
                        
                    }
                    else{
        	    		if (Helper.isErrorFourResponseValid(receivePacket)){
        	    		    System.out.println("Error 4 Packet Received: Illegal TFTP operation - " + new String(Helper.dataExtractor(receivePacket)));
        	    		    File file = new File(filename);
                            file.delete();
                           Thread.currentThread().interrupt();
                           return;
        	    		}    
                        else if(!Helper.isDataOpCodeValid(receivePacket)){
                    	   System.out.println("Error Data Packet Received");
                            byte[] errorCode = Helper.formErrorPacket(Constants.ILLEGAL_TFTP_OPERATION, "Invalid Data Opcode");
                            DatagramPacket invalidOpcode = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), receivePacket.getPort());
                            sendReceiveSocket.send(invalidOpcode);
                            System.out.println("Sending Error Code 4");
                            File file = new File(filename);
                            file.delete();
                           Thread.currentThread().interrupt();
                           return;
                        }
                        else if(!Helper.isDataLengthValid(receivePacket)){
                        	System.out.println("Error Data Packet Receiver: Data packet exceeds maximum length");
                            byte[] errorCode = Helper.formErrorPacket(Constants.ILLEGAL_TFTP_OPERATION, "Invalid Data Length");
                            DatagramPacket invalidOpcode = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), receivePacket.getPort());
                            sendReceiveSocket.send(invalidOpcode);
                            printer.printMessage("Sending Error Code 4");
                            printer.printPacketInfo("FileReceiver", "Sending", invalidOpcode);
                            bufferedWriter.close();
                            f = new File(filename);
                            //Files.delete(Paths.get(f.getAbsolutePath()));
        	    		    f.delete();
        	    		    return;
                        }
                        else{
                            
                            int packetBlockNumber = (int)(data[2])*256 + (int)(data[3]);                      
                            if(packetBlockNumber > nextDataPacketNumber){
                                byte[] errorCode = Helper.formErrorPacket(Constants.ILLEGAL_TFTP_OPERATION, "Invalid Request Format");
                                sendPacket(sendPort, destAddress, errorCode);
                                printer.printMessage("Error: Wrong packet number. Received: " + packetBlockNumber + " Expected: " + nextDataPacketNumber);
                                printer.printMessage("Sending Error code 4");
                                return;
                            }
                            if((data[2] << 8) + data[3] == nextDataPacketNumber){
                                bufferedWriter.write(Helper.dataExtractor(receivePacket));
                                nextDataPacketNumber++;
                                if(nextDataPacketNumber == 65535){
                                    nextDataPacketNumber = 0;
                                }
                            }
                            
                            
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
                   printer.printMessage("Sending Error Code 3");
	        }      
    		System.err.println(e.getMessage());
    		System.exit(1);
    	}
        finally{
            try{
                if(bufferedWriter!=null){
                    bufferedWriter.close();
                    System.out.println("Successfully finished a read transaction");
                }            
            }
            catch (IOException e){
            	System.err.println(e.getMessage());
                System.exit(1);
            }
        }        
    }
    
    
}
