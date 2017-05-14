package iteration_2.server;


import java.net.*;
import iteration_2.*;


public class ServerLogicThreadSpawn extends Thread{

    private DatagramPacket receivePacket;
    private DatagramSocket receiveSocket;
    private PrintService printer;
    private Thread response;
       
	public ServerLogicThreadSpawn(DatagramSocket receiveSocket){
	    // Assign a datagram socket and bind it to port 69 
        // on the local host machine. This socket will be used to
	    // receive UDP Datagram packets.
	    this.receiveSocket = receiveSocket;
	    printer = new PrintService(Server.runType);
    }
	   
	public void close(){
	    receiveSocket.close();
    }

    public void run(){
        while(!Thread.interrupted()){
		    try{
			
                // Construct a DatagramPacket for receiving packets up 
			    // to 100 bytes long (the length of the byte array).

			    byte data[] = new byte[100];
			      
			    receivePacket = new DatagramPacket(data, data.length);
			    printer.printMessage("Server: Waiting for Packet.\n");
	
			    // Block until a datagram packet is received from receiveSocket.
			    printer.printMessage("Waiting..."); // so we know we're waiting
			    receiveSocket.receive(receivePacket);
			     
			    //log the packet
	            printer.printPacketInfo("Server", "Received", receivePacket);
	            
	            
	            if(!Helper.validReadWriteRequest(receivePacket)){
	                System.out.println("Invalid Read/Write Request Received");
	                byte[] errorCode = Helper.formErrorPacket(Constants.ILLEGAL_TFTP_OPERATION, "Invalid Request Format");
	                DatagramPacket invalidOpcode = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), receivePacket.getPort());                    
                    DatagramSocket errorSocket = new DatagramSocket();
                    errorSocket.send(invalidOpcode);
                    errorSocket.close();
	            }
	            
	            else if(!Helper.isModeValid(receivePacket)){
	                byte[] errorCode = Helper.formErrorPacket(Constants.ILLEGAL_TFTP_OPERATION, "Invalid Mode in Request");
	                DatagramPacket invalidOpcode = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), receivePacket.getPort());                    
                    DatagramSocket errorSocket = new DatagramSocket();
                    errorSocket.send(invalidOpcode);
                    errorSocket.close();	                
	            }
	            
	            
	            else if(Helper.isReadOpCodeValid(receivePacket)){	                
                    response =  new ReadThreadSpawn(receivePacket, Server.runType);
                    response.start(); 
                }
                else if(Helper.isWriteOpCodeValid(receivePacket)) {
                    response =  new WriteThreadSpawn(receivePacket, Server.runType);
                    response.start(); 
                }
                else{
                    byte[] errorCode = Helper.formErrorPacket(Constants.ILLEGAL_TFTP_OPERATION, "Invalid Request");
                    DatagramPacket invalidOpcode = new DatagramPacket(errorCode, errorCode.length, receivePacket.getAddress(), receivePacket.getPort());
                    
                    DatagramSocket errorSocket = new DatagramSocket();
                    errorSocket.send(invalidOpcode);
                    errorSocket.close();
                }
	   
            }catch(SocketException e){
			    System.out.println("Quiting...");
			}catch(Exception e){			      
			    e.printStackTrace();
			    System.exit(1);
		    }
	    }	       
    }

	

	public static void main( String args[] ){
	    try{
	        ServerLogicThreadSpawn c = new ServerLogicThreadSpawn(new DatagramSocket(69));
	        c.start();
	    }catch(Exception e){
	        e.printStackTrace();
	        System.exit(1);
	    }	      
	}   

}