package iteration_1.intermediate_host;

import java.io.*;
import java.net.*;
import iteration_1.*;
import java.util.Scanner;

public class IntermediateHost {
	private DatagramSocket recieveSocket;
	private DatagramSocket sendSocket;
	private DatagramSocket sendrecieveSocket;
	private DatagramPacket receivePacket;
	private DatagramPacket sendPacket;
	int clientPort;
	private PrintService printer;
	private int sendPort;
	public static Constants.runType runType;

	public IntermediateHost(){
		try {
	        // Construct a receive datagram socket and bind it to port 23
			// Construct a sendrecieve datagram socket and bind it to any available
	        // port on the local host machine. This socket will be used to
	        // send and receive UDP Datagram packets.
			recieveSocket = new DatagramSocket(Constants.IH_LISTENING_PORT);			 
			sendrecieveSocket = new DatagramSocket();
            sendPort = Constants.SERVER_LISTENING_PORT;
			
			//instansiate printer service instance
			printer = new PrintService(IntermediateHost.runType);

	    } catch (SocketException se) {   // Can't create the socket.
	        se.printStackTrace();
	        System.exit(1);
	    }
	}
	
	public void sendAndReceive(){
		while(true){
			byte data[] = new byte[516];
			receivePacket = new DatagramPacket(data, data.length);

		    try {
		        printer.printMessage("Intermidiate Host: Waiting for Packet.\n");
		        // Block until a datagram is received via sendReceiveSocket.  
		    	recieveSocket.receive(receivePacket);

		    	// Process the received datagram.
		    	printer.printPacketInfo("IntermediateHost", "Recieved", receivePacket);

		   	    //save the port packet was received from
		    	clientPort = receivePacket.getPort();
		    	sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), InetAddress.getLocalHost(), sendPort);		      
                
			    //Print out new packet info
			    printer.printPacketInfo("IntermediateHost", "Sending", sendPacket);		      

		        // Block until a datagram is received via sendReceiveSocket.  
		    	sendrecieveSocket.send(sendPacket); 
		    	sendrecieveSocket.receive(receivePacket); 
                sendPort = receivePacket.getPort();

			    // Process the received datagram and send to client.
			    printer.printPacketInfo("IntermediateHost", "Recieved", receivePacket);		
			    sendSocket = new DatagramSocket();
				sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), InetAddress.getLocalHost(), clientPort);

				printer.printPacketInfo("IntermediateHost", "Sending", sendPacket);
				sendSocket.send(sendPacket);

				//close the socket
				sendSocket.close();
				System.out.println("Successfully finished a write read transaction");

		    } catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
		    }
		}
	}

   public static void main(String args[]){
        
	      Scanner scanner = new Scanner(System.in);
	      String userInputRunType, input;
	      System.out.println("Press \"q\" to quit the program");
	      System.out.println("Enter run type: (quiet/verbose)");
          while(true) {
    	      input = scanner.nextLine();
    	      
    	      if(input != null){
    	          if(input.toLowerCase().equals("q")) System.exit(0);
    	          
    	          String[] parameters = input.split(" ");
    	          if(parameters.length == 1){
        	          userInputRunType = parameters[0];        	    
        	          if (!userInputRunType.toLowerCase().equals("quiet") && !userInputRunType.toLowerCase().equals("verbose")) {
                          System.out.println("Invalid Run Type. Please follow the following format.\nFormat: (write/read) (quiet/verbose) (normal/test) \"example.txt\"");
                      } else {                          
                          System.out.println("Starting the server in " + userInputRunType + " Mode...");
            	          break;
            	      }
    	          } else {
    	              System.out.println("Unrecognized Run Type. Please Enter Run Type: (quiet/verbose)");
    	          }
    	      }
	      }
    
    	  if(userInputRunType != null){
    	      Constants.runType runType;
    	      if(userInputRunType.equals("quiet")){
    	          runType = Constants.runType.QUIET;
    	      }else{
    	          runType = Constants.runType.VERBOSE;
    	      }
    	      IntermediateHost.runType = runType;
	          IntermediateHost interMediateHost= new IntermediateHost();
	          interMediateHost.sendAndReceive();
	          
    	      boolean go = true;    	      
              while(go) {
        	      input = scanner.nextLine();
    
        	      if(input != null){
        	          if(input.toLowerCase().equals("q")) {
        	              System.out.println("Quitting...");
        	              scanner.close();
        	              System.exit(0);
    	              }    
        	      }
    	      }
          }	      
    }
}