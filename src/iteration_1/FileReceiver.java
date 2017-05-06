package iteration_1;

import java.io.*;
import java.net.*;

/**
 * FileReceiver.java - Demonstrates how to use Java's byte stream I/O
 * classes to transmit a file. Copies the contents of in.dat to out.dat.
 *
 * @version 1.1 February 26, 2002
 */

public class FileReceiver {
    
    private DatagramSocket sendReceiveSocket;
    private DatagramPacket sendPacket, receivePacket;
    
    public void receive(String filename, int destPort, InetAddress destAddress, byte[] ack) throws FileNotFoundException, IOException {

        PrintService printer = new PrintService();

        BufferedWriter bufferedWriter = null;
        FileWriter fileWriter = null;
        
        try{
            fileWriter = new FileWriter(filename);
            bufferedWriter = new BufferedWriter(fileWriter);

            byte[] data = new byte[512];
        
            sendReceiveSocket = new DatagramSocket();
        
            do{ 
                receivePacket = new DatagramPacket(data, data.length);

                sendReceiveSocket.receive(receivePacket);
                
                printer.printPacketInfo("FileReceiver", "Receive", receivePacket);

                bufferedWriter.write(receivePacket.getData() + "");

                sendPacket = new DatagramPacket(ack, ack.length, destAddress, destPort);
                   
                printer.printPacketInfo("FileReceiver", "Send", sendPacket);

                sendReceiveSocket.send(sendPacket);
            
            }while(receivePacket.getLength() == 512);
        }
        catch (UnknownHostException e) {
    		e.printStackTrace();
    		System.exit(1);
        }
    	catch (IOException e){
    		e.printStackTrace();
    		System.exit(1);
    	}
        finally{
            try{
                if(bufferedWriter!=null){
                    bufferedWriter.close();
                }
                if(fileWriter!=null){
                    bufferedWriter.close();
                }
            }
            catch (IOException e){
                e.printStackTrace();
                System.exit(1);
            }
        }        
    }
}