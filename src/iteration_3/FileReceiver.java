package iteration_3;

import java.io.*;
import java.net.*;

/**
 * FileReceiver.java - Demonstrates how to use Java's byte stream I/O
 * classes to transmit a file. Copies the contents of in.dat to out.dat.
 *
 * @version 1.1 February 26, 2002
 */

public abstract class FileReceiver {

    public DatagramSocket sendReceiveSocket;
    public DatagramPacket sendPacket, receivePacket;
    
    public PrintService printer;
    public int sendPort;
    
    
    public void sendPacket(int port, InetAddress destAddress, byte[] ack) throws Exception {
        try{

            sendPacket = new DatagramPacket(ack, ack.length, destAddress, port);

            printer.printPacketInfo("FileReceiver", "Sending", sendPacket);
    
            sendReceiveSocket.send(sendPacket);
            sendPort = sendPacket.getPort();
        }catch (UnknownHostException e) {
    		e.printStackTrace();
    		System.exit(1);
        }
    	catch (IOException e){
    		e.printStackTrace();
    		System.exit(1);
    	}
    }
    
    abstract public void  receive(String filename, int destPort, InetAddress destAddress) throws Exception;
}