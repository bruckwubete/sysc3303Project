package iteration_2.client;

import java.io.*;
import java.net.*;

import iteration_2.*;
public class ClientFileSender extends iteration_2.FileSender {
    
        private int expectedPort;
    
        public ClientFileSender(Constants.runType runType, int expectedPort, DatagramSocket sendReceiveSocket){
            printer = new PrintService(runType);
            this.expectedPort = expectedPort;
            this.sendReceiveSocket = sendReceiveSocket;
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
	    		e.printStackTrace();
	    		System.exit(1);
	    	}

	    	byte[] msg = new byte[100];

	    	receivePacket = new DatagramPacket(msg, msg.length);
	    	
	    	try{
	    		sendReceiveSocket.receive(receivePacket);
	    		printer.printPacketInfo("FileSender", "Receive", receivePacket);
	    		
	    		
	    		while(true){
	    		    if(expectedPort != receivePacket.getPort()){
                        System.out.println("Encountered an error packet: UNKNOWN TRANSER ID");
                        byte[] errorCode = Helper.formErrorPacket(Constants.UNKNOWN_TRANSFER_ID, "Packet received from invalid port");
                        DatagramPacket invalidOpcode = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), receivePacket.getPort());
                        sendReceiveSocket.send(invalidOpcode);
                        System.out.println("Sending Error Code");
    	    		}
    	    		else if (Helper.isErrorFourResponseValid(receivePacket)){
    	    		    System.out.println("Error Packet Received: Illegal TFTP operation");
                        System.exit(1);
    	    		}
    	    		else if (!Helper.isAckOpCodeValid(receivePacket)){
    	    		    byte[] errorCode = Helper.formErrorPacket(Constants.ILLEGAL_TFTP_OPERATION, "Ack Op Code not valid");
    	    		    DatagramPacket invalidOpcode = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), receivePacket.getPort());
    	    		    sendReceiveSocket.send(invalidOpcode);
    	    		    throw new Exception("READ ACKNOWLEDGE OPCode IS NOT VALID");
    	    		}
    	    		else{
    	    		    break;
    	    		}
    	    		/*else if (Helper.isAckOpCodeValid(receivePacket) ){
    	    		    continue;
    	    		}
    	    		else {
    	    		    printer.printMessage("Invalid acknowledge received");
    	    		    System.exit(1);
    	    		}*/
    		    }

	    	}
	    	catch (IOException e){
	    		e.printStackTrace();
	    		System.exit(1);
	    	}
        }
        
        in.close();
    }
}
