package iteration_2.client;

import java.io.*;
import java.net.*;
import java.util.Arrays;

import iteration_2.client.Client;
import iteration_2.*;


public class ClientFileReceiver extends iteration_2.FileReceiver{
    
    int count = 0;
    int expectedReceivePort;
    

    public ClientFileReceiver(Constants.runType runType){
            printer = new PrintService(runType);
            try{
                sendReceiveSocket = new DatagramSocket();
            } catch(Exception e){
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
                
                count++;
                
                printer.printPacketInfo("FileReceiver", "Receive", receivePacket);
                    
                if(count  == 1 && Client.mode != Constants.Mode.TEST){
                    sendPort = receivePacket.getPort();
                } 
                if(count  == 1){
                    expectedReceivePort = receivePacket.getPort();
                } 
                
                if (expectedReceivePort != receivePacket.getPort()){
                    System.out.println("Encountered an error packet: UNKNOWN TRANSER ID");
                    byte[] errorCode = Helper.formErrorPacket(Constants.UNKNOWN_TRANSFER_ID, "Packet received from invalid port");
                    DatagramPacket invalidOpcode = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), receivePacket.getPort());
                    sendReceiveSocket.send(invalidOpcode);
                    System.out.println("Sending Error Code");
                    
                }else{
    	    		if (Helper.isErrorFourResponseValid(receivePacket)){
    	    		    System.out.println("Error Packet Received: Illegal TFTP operation");
    	    		    File file = new File(filename);
                        file.delete();
                        System.exit(1);
    	    		}
                    else if(!Helper.isDataOpCodeValid(receivePacket)){
                        byte[] errorCode = Helper.formErrorPacket(Constants.ILLEGAL_TFTP_OPERATION, "Invalid Ack");
                        DatagramPacket invalidOpcode = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), receivePacket.getPort());
                        sendReceiveSocket.send(invalidOpcode);
                        System.out.println("Sending Error Code");
                        File file = new File(filename);
                        file.delete();
                        System.exit(1);
                    }
                    else{
                        bufferedWriter.write(Helper.dataExtractor(receivePacket));         
                        System.arraycopy(Constants.ACK, 0, ack, 0, 2);
                        System.arraycopy(Arrays.copyOfRange(receivePacket.getData(), 2, 4), 0, ack, 2, 2);
                        sendPacket(sendPort, destAddress, ack);
                        
                    }
                    
                    printer.printPacketInfo("FileReceiver", "Sending", receivePacket);
                }

            }while(receivePacket.getLength() == 516);
            
        }catch (UnknownHostException e) {
    		e.printStackTrace();
    		System.exit(1);
        }
    	catch (IOException e){
    		e.printStackTrace();
    		System.exit(1);
    	}finally{
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
