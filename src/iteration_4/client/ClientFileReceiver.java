package iteration_4.client;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import iteration_4.client.Client;
import iteration_4.*;


public class ClientFileReceiver extends iteration_4.FileReceiver{
    
    int count = 0;
    int expectedReceivePort;
    int retryCount = 0;
    int nextDataPacketNumber = 1;
    byte data[];
    

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
            filename = Constants.clientReadWriteLocation + filename;
            File f = new File(filename);
          
            bufferedWriter = new FileOutputStream(filename);

            byte[] data = new byte[516];

            boolean errorEncountered = false;
            
            do{ 
            	errorEncountered = false;
                receivePacket = new DatagramPacket(data, data.length);
                printer.printMessage("Receiving on: " + sendReceiveSocket.getLocalPort());
                
                //sendReceiveSocket.receive(receivePacket);
                
                 do{
                       try{
                           
                           if(count ==0) sendReceiveSocket.setSoTimeout(Constants.SENDER_TIMEOUT);
                           sendReceiveSocket.receive(receivePacket);
                           retryCount = 0;
                           break;
                       }catch(SocketTimeoutException e){
                           if(count == 0){
                               printer.printMessage("Timed out while waiting for data packet block 1. Retrying...");
                               byte requestData[] = Helper.getRequestData(filename, "read");
                               sendPacket(sendPort, InetAddress.getLocalHost(), requestData);
                               retryCount++;
                               if(retryCount > 2){
                                   printer.printMessage("Failed after waiting for a data packet block 1 3 times. Quitting...");
                                   return;
                               }                               
                           }
                          
                           
                       }catch (IOException e){
        	    		System.err.println(e.getMessage());
        	    		System.exit(1);
        	    	   }
                            
                    }while(retryCount < 3 && count==0);
              
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
                    printer.printMessage("Sending Error Code 5\n");
                    
                }else{
    	    		if (Helper.isErrorOneResponseValid(receivePacket)){
    	    			System.out.println("Error 1 Packet Received: File Not Found - " + new String(Helper.dataExtractor(receivePacket)));
    	    		    bufferedWriter.close();
    	    		    f = new File(filename);
    	    		    Files.delete(Paths.get(f.getAbsolutePath()));
    	    		    return;
	    		    } else if (Helper.isErrorTwoResponseValid(receivePacket)){
    	    			System.out.println("Error 2 Packet Received: Access Violation - " + new String(Helper.dataExtractor(receivePacket)));
    	    		    bufferedWriter.close();
    	    		    f = new File(filename);
    	    		    Files.delete(Paths.get(f.getAbsolutePath()));
    	    		    return;
    	    		} else if (Helper.isErrorFourResponseValid(receivePacket)){
    	    			System.out.println("Error 4 Packet Received: Illegal TFTP operation - " + new String(Helper.dataExtractor(receivePacket)));
    	    		    bufferedWriter.close();
    	    		    f = new File(filename);
    	    		    Files.delete(Paths.get(f.getAbsolutePath()));
    	    		    return;
    	    		}
                    else if(!Helper.isDataOpCodeValid(receivePacket)){
                    	System.out.println("Error Ack Packet Received");
                        byte[] errorCode = Helper.formErrorPacket(Constants.ILLEGAL_TFTP_OPERATION, "Invalid Data Opcode");
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
                        //data = Helper.dataExtractor(receivePacket);                       
                        //data = receivePacket.getData();
                        int packetBlockNumber = (int)(data[2])*256 + (int)(data[3]);
                        //if((data[2] << 8) + data[3] > nextDataPacketNumber){
                        
                        if(packetBlockNumber > nextDataPacketNumber){
                            byte[] errorCode = Helper.formErrorPacket(Constants.ILLEGAL_TFTP_OPERATION, "Invalid Request Format");
                            sendPacket(sendPort, destAddress, errorCode);
                            printer.printMessage("Error: Wrong packet number. Received: " + packetBlockNumber + " Expected: " + nextDataPacketNumber);
                            printer.printMessage("Sending Error code 4");
                            return;
                        }
                        //if((data[2] << 8) + data[3] == nextDataPacketNumber)bufferedWriter.write(data);
                        if(packetBlockNumber == nextDataPacketNumber){
                            nextDataPacketNumber++;
                            bufferedWriter.write(Helper.dataExtractor(receivePacket));
                            if(nextDataPacketNumber == 65535){
                                nextDataPacketNumber = 0;
                            }
                        }
                                      
                        System.arraycopy(Constants.ACK, 0, ack, 0, 2);
                        System.arraycopy(Arrays.copyOfRange(receivePacket.getData(), 2, 4), 0, ack, 2, 2);
                        sendPacket(sendPort, destAddress, ack);
                        
                        
                    }
                    
                   
                }

            }while(receivePacket.getLength() == 516 || errorEncountered);
            
            System.out.println("Successfully finished a read transaction! \n\n");
            
        }catch (UnknownHostException e) {
        	throw new Exception(e.getMessage());
        }
    	catch (IOException e){
            if (e.getMessage().contains("space left on device")){
                   byte[] errorCode = Helper.formErrorPacket(Constants.DISK_FULL, "Client out of space");
                   DatagramPacket errorPacket = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), sendPort);
                   sendReceiveSocket.send(errorPacket);
                   printer.printMessage("Sending Error Code 5");
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
