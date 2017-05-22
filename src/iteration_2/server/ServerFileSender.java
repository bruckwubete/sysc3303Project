package iteration_2.server;

import java.io.*;
import java.net.*;

import iteration_2.*;

public class ServerFileSender extends iteration_2.FileSender {
    
    
    
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
    	    		sendReceiveSocket.receive(receivePacket);
    	    		printer.printPacketInfo("FileSender", "Receive", receivePacket);
    	    		if(destPort != receivePacket.getPort()){
                        printer.printMessage("Encountered an error packet: UNKNOWN TRANSER ID");
                        byte[] errorCode = Helper.formErrorPacket(Constants.UNKNOWN_TRANSFER_ID, "Packet received from invalid port");
                        DatagramPacket invalidOpcode = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), receivePacket.getPort());
                        sendReceiveSocket.send(invalidOpcode);
                        printer.printMessage("Sending Error Code");
    	    		}
    	    		else if (Helper.isErrorFourResponseValid(receivePacket)){
    	    		   printer.printMessage("Error Packet Received: Illegal TFTP operation");
                       Thread.currentThread().interrupt();
                       return;
    	    		}
    	    		else if ( !Helper.isAckOpCodeValid(receivePacket) ){
    	    		    //byte[] errorCode = {0,4};
    	    		    byte[] errorCode = Helper.formErrorPacket(Constants.ILLEGAL_TFTP_OPERATION, "Ack Op Code not valid");
    	    		    DatagramPacket invalidOpcode = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), receivePacket.getPort());
    	    		    sendReceiveSocket.send(invalidOpcode);
    	    		    printer.printMessage("Closing server");
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
        
        in.close();
    }
}
