package iteration_1;

import java.io.*;
import java.net.*;
import java.util.Arrays;

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
    
    public void receive(String filename, int destPort, InetAddress destAddress) throws FileNotFoundException, IOException {

        FileOutputStream bufferedWriter = null;
        
        byte[] ack = new byte[4];
        
        try{
           
            File f = new File(filename);
            if(f.exists() && !f.isDirectory()) {
                String[] filePathArray = filename.split("/");                
                filePathArray[filePathArray.length - 1] = "Copy" + filePathArray[filePathArray.length - 1];
                filename = String.join("/", filePathArray);                
            }
           
            bufferedWriter = new FileOutputStream(filename);
           

            byte[] data = new byte[516];

            do{ 
                receivePacket = new DatagramPacket(data, data.length);
                printer.printMessage("Receiving on: " + sendReceiveSocket.getLocalPort());
                sendReceiveSocket.receive(receivePacket);
                
                sendPort = receivePacket.getPort();

                printer.printPacketInfo("FileReceiver", "Receive", receivePacket);
                
                System.out.println("Test Length: " + receivePacket.getLength());
                //bufferedWriter.write(Helper.dataExtractor(receivePacket), 0, receivePacket.getLength()-4);
                bufferedWriter.write(Helper.dataExtractor(receivePacket));
                //bufferedWriter.write(receivePacket.getData(), 0, receivePacket.getLength());
                
                System.arraycopy(Constants.ACK, 0, ack, 0, 2);
                System.arraycopy(Arrays.copyOfRange(receivePacket.getData(), 2, 4), 0, ack, 2, 2);
                
                sendPacket(sendPort, destAddress, ack);

            }while(receivePacket.getLength() == 516);
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