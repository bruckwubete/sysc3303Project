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
    
    private PrintService printer;
    private int sendPort;
    
    public FileReceiver(Constants.runType runType){
        printer = new PrintService(runType);
        try{
            sendReceiveSocket = new DatagramSocket();
        } catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }

    }
    
    public void sendPacket(int port, InetAddress destAddress, byte[] ack) throws FileNotFoundException, IOException {
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
    
    public void receive(String filename, int destPort, InetAddress destAddress, byte[] ack) throws FileNotFoundException, IOException {

        FileOutputStream bufferedWriter = null;
        
        try{
           
            File f = new File(filename);
            if(f.exists() && !f.isDirectory()) { 
                filename = "Copy" + filename;
            }
           
            bufferedWriter = new FileOutputStream(filename);
           

            byte[] data = new byte[512];

            do{ 
                receivePacket = new DatagramPacket(data, data.length);
                System.out.println("Receiving on: " + sendReceiveSocket.getLocalPort());
                sendReceiveSocket.receive(receivePacket);
                
                sendPort = receivePacket.getPort();

                printer.printPacketInfo("FileReceiver", "Receive", receivePacket);
                
                bufferedWriter.write(receivePacket.getData(), 0, receivePacket.getLength());

                //sendPacket = new DatagramPacket(ack, ack.length, destAddress, sendPort);

                //printer.printPacketInfo("FileReceiver", "Send", sendPacket);

                //sendReceiveSocket.send(sendPacket);
                
                sendPacket(sendPort, destAddress, ack);

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
            }
            catch (IOException e){
                e.printStackTrace();
                System.exit(1);
            }
        }        
    }
}