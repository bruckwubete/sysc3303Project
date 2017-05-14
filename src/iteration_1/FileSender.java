package iteration_1;

import java.io.*;
import java.net.*;

/**
 * FileTransmitter.java - Demonstrates how to use Java's byte stream I/O
 * classes to transmit a file. Copies the contents of in.dat to out.dat.
 *
 * @version 1.1 February 26, 2002
 */

public class FileSender {

    private DatagramSocket sendReceiveSocket;
    private DatagramPacket sendPacket, receivePacket;
    private PrintService printer;
    
    //private byte blockNumberByte1 = 0;
    //private byte blockNumberByte2 = 0;
    private int blockNumber = 0;
    
    public FileSender(Constants.runType runType){
        this.printer = new PrintService(runType);
    }


    public void send(String filename, int destPort, InetAddress destAddress) throws FileNotFoundException, IOException {
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
            System.out.println("Test Length: " + message.length + ", " + n);
            if(n<512){
                byte[] inputData = new byte[n+4];
                System.arraycopy(message, 0, inputData, 0, n+4);
                sendPacket = new DatagramPacket(inputData, inputData.length, destAddress, destPort);
            }
            else{
                sendPacket = new DatagramPacket(message, message.length, destAddress, destPort);   
            }
            System.out.println("Test Length 2: " + sendPacket.getLength());
  
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
	    		if ( isAcknowledgeValid(receivePacket) ){
	    		    continue;
	    		}
	    		else {
	    		    printer.printMessage("Invalid acknowledge received");
	    		    System.exit(1);
	    		}
	    	}
	    	catch (IOException e){
	    		e.printStackTrace();
	    		System.exit(1);
	    	}
        }
        
        in.close();
    }

    private static boolean isAcknowledgeValid(DatagramPacket a){
        byte[] data = a.getData();
        
        if (data[0] == 0 && data[1] == 4){
            return true;
        }
        return false;
    }

}
