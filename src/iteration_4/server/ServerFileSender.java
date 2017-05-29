package iteration_4.server;

import java.io.*;
import java.net.*;

import iteration_4.*;

public class ServerFileSender extends iteration_4.FileSender {
    
    int retryCount = 0;
    
    public ServerFileSender(Constants.runType runType){
        printer = new PrintService(runType);
    }
    
    
    
    public void send(String filename, int destPort, InetAddress destAddress) throws FileNotFoundException, IOException, Exception {
        /*
         * A FileInputStream object is created to read the file
         * as a byte stream. A BufferedInputStream object is wrapped
         * around the FileInputStream, which may increase the
         * efficiency of reading from the stream.
         */
        BufferedInputStream in = 
            new BufferedInputStream(new FileInputStream(filename));
    
        /*
         * A FileOutputStream object is created to write the file
         * as a byte stream. A BufferedOutputStream object is wrapped
         * around the FileOutputStream, which may increase the
         * efficiency of writing to the stream.
         */

        byte[] data = new byte[512];
        byte[] message = new byte[516];

        int n;
        int prevn = 0;

        sendReceiveSocket = new DatagramSocket();
 
        /* Read the file in 512 byte chunks. */
       
        while ((n = in.read(data)) != -1 || prevn==512) {

            /* 
             * We just read "n" bytes into array data. 
             * Now write them to the output file. 
             */
            
            /*blockNumberByte2++;
            if(blockNumberByte2==0){
                blockNumberByte1++;
            }
            */
            
            if(prevn==512&&n==-1){
                data = new byte[0];
                n = 0;
            }

            
            prevn = n;
            blockNumber ++;
            

            
            System.arraycopy(Constants.DATA, 0, message, 0, 2);
            message[2] = (byte)(blockNumber/256); //blockNumberByte1;
            message[3] = (byte)(blockNumber%256); //blockNumberByte2;
            System.arraycopy(data, 0, message, 4, n);
            
            if(n<512){
                byte[] inputData = new byte[n+4];
                System.arraycopy(message, 0, inputData, 0, n+4);
                sendPacket = new DatagramPacket(inputData, inputData.length, destAddress, destPort);
            }
            else{
                sendPacket = new DatagramPacket(message, message.length, destAddress, destPort);   
            }
            
  
            printer.printPacketInfo("FileSender", "Sending", sendPacket);

	    	try{
	    		sendReceiveSocket.send(sendPacket);
	    	}
	    	catch (IOException e){
	    		System.err.println(e.getMessage());
	    		System.exit(1);
	    	}

	    	byte[] msg = new byte[100];

	    	receivePacket = new DatagramPacket(msg, msg.length);
	    	
	    	try{
	    	    while(true){
	    	        do{
                       try{
                           sendReceiveSocket.setSoTimeout(Constants.SENDER_TIMEOUT);
                           sendReceiveSocket.receive(receivePacket);
                           if(Helper.isAckOpCodeValid(receivePacket)){
                               int packetBlockNumber = (int)(receivePacket.getData()[2])*256 + (int)(receivePacket.getData()[3]);
                               if(packetBlockNumber == blockNumber-1){
                                  printer.printPacketInfo("ServerFileSender", "Sending", sendPacket);
                                  //sendReceiveSocket.send(sendPacket);
                                  retryCount = 0;
                               }else{break;}
                           }else{break;}
                           
                       }catch(SocketTimeoutException e){
                           printer.printMessage("Timed out while waiting for ack packet. Retrying...");
                           printer.printPacketInfo("ServerFileSender", "Sending", sendPacket);
                           sendReceiveSocket.send(sendPacket);
                           retryCount++;
                           if(retryCount > 2){
                               printer.printMessage("Failed after waiting for an ack packet 3 times. Quitting...");
                               return;
                           }
                           
                       }catch (IOException e){
        	    		System.err.println(e.getMessage());
        	    		System.exit(1);
        	    	   }
                            
                    }while(retryCount < 3);
    	    		
    	    		printer.printPacketInfo("FileSender", "Receive", receivePacket);
    	    		if(destPort != receivePacket.getPort()){
                        printer.printMessage("Encountered an error packet: UNKNOWN TRANSER ID");
                        byte[] errorCode = Helper.formErrorPacket(Constants.UNKNOWN_TRANSFER_ID, "Packet received from invalid port");
                        DatagramPacket invalidOpcode = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), receivePacket.getPort());
                        sendReceiveSocket.send(invalidOpcode);
                        printer.printMessage("Sending Error Code 5");
    	    		}
    	    		else if (Helper.isErrorThreeResponseValid(receivePacket)){
    	    			System.out.println("Error 3 Packet Received: Not Enough Space - " + new String(Helper.dataExtractor(receivePacket)));
                        Thread.currentThread().interrupt();
    	    		    return;
    	    		}
    	    		else if (Helper.isErrorFourResponseValid(receivePacket)){
    	    		   System.out.println("Error 4 Packet Received: Illegal TFTP operation - " + new String(Helper.dataExtractor(receivePacket)));
                       Thread.currentThread().interrupt();
                       return;
    	    		}
    	    		else if ( !Helper.isAckOpCodeValid(receivePacket) ){
    	    		    byte[] errorCode = Helper.formErrorPacket(Constants.ILLEGAL_TFTP_OPERATION, "Ack Op Code not valid");
    	    		    DatagramPacket invalidOpcode = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), receivePacket.getPort());
    	    		    sendReceiveSocket.send(invalidOpcode);
                        Thread.currentThread().interrupt();
                        return;
    	    		}else if ( Helper.isErrorThreeResponseValid(receivePacket) ){
    	    		    printer.printMessage("Error 3 Packet Received: Client Disk Full");    	    		  
                        Thread.currentThread().interrupt();
                        return;
    	    		}
    	    		else{
    	    		    break;
    	    		}
    		    }

	    	}
	    	catch (IOException e){
	    		System.err.println(e.getMessage());
	    		System.exit(1);
	    	}
        }
        System.out.println("Successfully finished a write transaction");
        in.close();
    }
}
