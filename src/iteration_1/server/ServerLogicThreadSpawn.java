package iteration_1.server;


import java.net.*;
import iteration_1.*;


public class ServerLogicThreadSpawn extends Thread{

    private DatagramPacket receivePacket;
    private DatagramSocket receiveSocket;
    private int writeRead;
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
			    System.out.println("Server: Waiting for Packet.\n");
	
			    // Block until a datagram packet is received from receiveSocket.
			    System.out.println("Waiting..."); // so we know we're waiting
			    receiveSocket.receive(receivePacket);
			     
			    //log the packet
	            printer.printPacketInfo("Server", "Received", receivePacket);
	              
	            //validation
	            byte[] dataByte = receivePacket.getData();          
	            validate(dataByte);               

	            //build response message 

	            if(writeRead == 0){
	                response =  new ReadThreadSpawn(receivePacket, Server.runType);
	            }else{
	                
	                response =  new WriteThreadSpawn(receivePacket, Server.runType);
	            }
	                 
	            response.start();   

            }catch(SocketException e){
			    System.out.println("Quiting...");
			}catch(Exception e){			      
			    e.printStackTrace();
			    System.exit(1);
		    }
	    }	       
    }

	private void validate(byte[] dataByte) throws Exception {
	    System.out.println("\nServer: Parsing received packet. \n");
		if (dataByte[0] != (byte) 0) { 
			// check first byte - should equal 0 
			System.out.println("ERROR: First byte should be 0. \nExiting...");
			System.exit(1);
		}

		// check second byte should equal 1 or 2
		if ((dataByte[1] == (byte) 1) || (dataByte[1] == (byte) 2)) { 
			writeRead = dataByte[1] == (byte) 1 ? 0:1;
		} else {
			System.out.println("ERROR: Second byte incorrect. \nExiting...");
			System.exit(1);
		}
		
		// check last byte should equla 0
		if (dataByte[receivePacket.getLength() - 1] != (byte) 0) {
			System.out.println("ERROR: Last byte incorrect.");
			System.exit(1);
		}

		// check for a single "0"
		int count = 0;
		for (int i = 2; i < receivePacket.getLength() - 1; i++) { 
			// 3rd element of array to second last element
			if (dataByte[i] == (byte) 0) {
				count++;
			}	
		}
		if (count != 1) {
			System.out.println("ERROR: No 0 byte or to many 0 bytes between strings. \nExiting...");
			throw new Exception();
		}

		System.out.println("Server: Parsing finished. \n\n");	

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
