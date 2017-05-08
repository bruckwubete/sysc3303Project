package iteration_1.intermediate_host;

import java.io.*;
import java.net.*;
import iteration_1.*;

public class IntermediateHost {
	private DatagramSocket recieveSocket;
	private DatagramSocket sendSocket;
	private DatagramSocket sendrecieveSocket;
	private DatagramPacket receivePacket;
	private DatagramPacket sendPacket;
	int clientPort;
	private PrintService printer;

	public IntermediateHost(){
		try {
	        // Construct a receive datagram socket and bind it to port 23
			// Construct a sendrecieve datagram socket and bind it to any available
	        // port on the local host machine. This socket will be used to
	        // send and receive UDP Datagram packets.
			recieveSocket = new DatagramSocket(23);			 
			sendrecieveSocket = new DatagramSocket();

			//instansiate printer service instance
			//TODO: make the argument come in from user input
			printer = new PrintService(Constants.runType.VERBOSE);

	    } catch (SocketException se) {   // Can't create the socket.
	        se.printStackTrace();
	        System.exit(1);
	    }
	}
	
	public void sendAndReceive(){
		while(true){
			byte data[] = new byte[100];
			receivePacket = new DatagramPacket(data, data.length);

		    try {
		        System.out.println("Intermidiate Host: Waiting for Packet.\n");
		        // Block until a datagram is received via sendReceiveSocket.  
		    	recieveSocket.receive(receivePacket);

		    	// Process the received datagram.
		    	printer.printPacketInfo("IntermediateHost", "Recieved", receivePacket);

		   	    //save the port packet was received from
		    	clientPort = receivePacket.getPort();
		    	sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), InetAddress.getLocalHost(), 69);		      

			    //Print out new packet info
			    printer.printPacketInfo("IntermediateHost", "Sending", sendPacket);		      

		        // Block until a datagram is received via sendReceiveSocket.  
		    	sendrecieveSocket.send(sendPacket);  

		    	sendrecieveSocket.receive(receivePacket);

			    // Process the received datagram and send to client.
			    printer.printPacketInfo("IntermediateHost", "Recieved", receivePacket);		
			    sendSocket = new DatagramSocket();
				sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), InetAddress.getLocalHost(), clientPort);

				printer.printPacketInfo("IntermediateHost", "Sending", sendPacket);
				sendSocket.send(sendPacket);

				//close the socket
				sendSocket.close();

		    } catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
		    }
		}
	}

	public static void main(String args[]){
		IntermediateHost i = new IntermediateHost();
	    i.sendAndReceive();
	}
}
