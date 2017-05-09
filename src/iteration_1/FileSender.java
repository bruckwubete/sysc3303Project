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
        BufferedOutputStream out =
            new BufferedOutputStream(new FileOutputStream("out.dat"));

        byte[] data = new byte[512];

        int n;

        sendReceiveSocket = new DatagramSocket();
        
        /* Read the file in 512 byte chunks. */
        while ((n = in.read(data)) != -1) {
            /* 
             * We just read "n" bytes into array data. 
             * Now write them to the output file. 
             */
            if(n<512){
                byte[] inputData = new byte[n];
                System.arraycopy(data, 0, inputData, 0, n);
                sendPacket = new DatagramPacket(inputData, inputData.length, destAddress, destPort);
            }
            else{
                sendPacket = new DatagramPacket(data, data.length, destAddress, destPort);   
            }

            out.write(data, 0, n);
  
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
        out.close();
    }

    private static boolean isAcknowledgeValid(DatagramPacket a){
        byte[] data = a.getData();
        
        if (data[0] == 0 && data[1] == 4){
            return true;
        }
        return false;
    }

}
