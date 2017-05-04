package iteration_1;

import java.io.*;
import java.net.*;

public class Client {
	private DatagramPacket sendReceivePacket;
	private DatagramSocket sendReceiveSocket;
	private PrintService printer;

	   public Client(){
	      try {
	         // Construct a datagram socket and bind it to any available 
	         // port on the local host machine. This socket will be used to
	         // send and receive UDP Datagram packets.
	         sendReceiveSocket = new DatagramSocket();
	         printer = new PrintService();
	      } catch (SocketException se) {   // Can't create the socket.
	         se.printStackTrace();
	         System.exit(1);
	      }
	   }

	   public void sendAndReceive(){
          //Required Function's declerations
		  ByteArrayOutputStream stream = new ByteArrayOutputStream();
	      byte[] read_sequence = {0, 1};
	      byte[] write_sequence = {0, 2};
	      byte[] invalid_sequence = {1,4};
	      String ex_filename = "test.txt";
	      String mode = "ocTEt";
	      byte msg[] = ex_filename.getBytes();	      
	      int count = 0;
	      byte data[] = new byte[100]; //buffer for packet receiving 
	      
	      while(count!=11){
	    	  try{
	              //Construct message
	    		  if(count == 10){
	    			  System.out.println("Sending corrupted packet to server");
	    			  stream.write(invalid_sequence);  
	    		  }else if(count%2 == 0){//alternate between read and write
		    		  stream.write(read_sequence);			 
		    	  }else{
		    		  stream.write(write_sequence);
		    	  }
		    	  stream.write(msg);
		    	  stream.write(0);
		    	  stream.write(mode.getBytes());
		    	  stream.write(0);
		    	  
		    	  //Create Packet
		    	  sendReceivePacket = new DatagramPacket(stream.toByteArray(), stream.toByteArray().length,
		 	                                         InetAddress.getLocalHost(), 23);	 	     
		 	      
		    	  //Print Packet
		    	  printer.printPacketInfo("Client", "Sending", sendReceivePacket);
			      
	
			      // Send the datagram packet to the server via the send/receive socket. 
	               sendReceiveSocket.send(sendReceivePacket);
	               System.out.println("Client: Packet sent.\n");
	               
	               
	               System.out.println("\nWaiting for packet \n");	               
	               sendReceivePacket = new DatagramPacket(data, data.length);           	
	     	      // Block until a datagram is received via sendReceiveSocket.  
	     	      sendReceiveSocket.receive(sendReceivePacket);     	    
	     	      // Process the received datagram.
	     	      printer.printPacketInfo("Client", "Recieved", sendReceivePacket);
			      
	     	      //Prepare for next loop
		       	  count++;
		       	  stream = new ByteArrayOutputStream();
	    	  }catch(Exception e){
	    		  e.printStackTrace();
	    		  System.exit(1);
	    	  }
	      }

	      // We're finished, so close the socket.
	      sendReceiveSocket.close();
	   }

	   public static void main(String args[]){
	      Client c = new Client();
	      c.sendAndReceive();
	   }	   
	
}
