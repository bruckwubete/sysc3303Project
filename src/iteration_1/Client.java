package iteration_1;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
	private DatagramPacket sendReceivePacket;
	private DatagramSocket sendReceiveSocket;
	private PrintService printer;

	   public Client(String requestType, String runType, String mode, String filename){
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
//	      Scanner s = new Scanner(System.in);
//	      System.out.println("Press \"q\" to quit the program");
//	      System.out.println("Enter a request: (write/read)");
//	      String inputRequest, inputRunType, inputMode, inputFilename;
//	      while(true) {
//    	      inputRequest = s.nextLine();
//                
//              if(inputRequest.toLowerCase().equals("q"))  {
//                  System.exit(1);
//              }
//    	      if (!inputRequest.toLowerCase().equals("write") && !inputRequest.toLowerCase().equals("read")) {
//    	          System.out.println("Invalid Request. Please use the following format: (write/read)");
//    	      } else {
//    	          break;
//    	      }
//	      }
//	      
//          System.out.println("Enter a run type: (quite/verbose)");
//          while(true){
//              inputRunType = s.nextLine();
//                
//              if(inputRunType.toLowerCase().equals("q"))  {
//                  System.exit(1);
//              }
//              if (!inputRunType.toLowerCase().equals("quite") && !inputRunType.toLowerCase().equals("verbose")) {
//                  System.out.println("Invalid Request. Please use the following format: (quite/verbose)");
//              } else {
//                  break;
//              }
//          }
//
//          System.out.println("Enter a run mode: (normal/test)");
//          while(true){
//              inputMode =s.nextLine();
//                
//              if(inputMode.toLowerCase().equals("q"))  {
//                  System.exit(1);
//              }
//              if (!inputMode.toLowerCase().equals("normal") && !inputMode.toLowerCase().equals("test")) {
//                  System.out.println("Invalid Request. Please use the following format: (normal/test)");
//              } else {
//                  break;
//              }
//          }
//
//          System.out.println("Enter the file name:");
//          while(true){
//              inputFilename = s.nextLine();
//              
//              if(inputFilename.toLowerCase().equals("q")) {
//                  System.exit(1);
//              }
//              File f = new File(inputFilename);
//              if(f.exists() && f.isFile()){
//                  System.out.println("Hassaan is currently sending the request. Please hold...");
//                  break;
//              } else {
//                  System.out.println("Invalid Request: Please enter a file name");
//              }
//          }
//	      s.close();

/* QUICK test for server
   try {
    byte[] test= {};
     DatagramPacket sendReceivePacket1 = new DatagramPacket(test, test.length,
		 	                                         InetAddress.getLocalHost(), 69);	 	     
		 	      
	         // Construct a datagram socket and bind it to any available 
	         // port on the local host machine. This socket will be used to
	         // send and receive UDP Datagram packets.
	     DatagramSocket    sendReceiveSocket1 = new DatagramSocket();
	     sendReceiveSocket1.send(sendReceivePacket1);
	     
	      } catch (Exception se) {   // Can't create the socket.
	         se.printStackTrace();
	         System.exit(1);
	      }
*/
	      /************************************************************/
	      Scanner scanner = new Scanner(System.in);
	      String input, requestType, runType, mode, filename;
	      System.out.println("Press \"q\" to quit the program");
	      System.out.println("Enter a request: (write/read) (quite/verbose) (normal/test) \"example.txt\"");
          while(true) {
    	      input = scanner.nextLine();
    	      
    	      if(input != null){
    	          if(input.toLowerCase().equals("q")) System.exit(0);
    	          
    	          String[] parameters = input.split(" ");
    	          if(parameters.length == 4){
        	          requestType = parameters[0];
        	          runType = parameters[1];
        	          mode = parameters[2];
        	          filename = parameters[3];
        	          File f = new File(filename);
        	          if (!requestType.toLowerCase().equals("write") && !requestType.toLowerCase().equals("read")) {
            	          System.out.println("Invalid Request Type. Please follow the following format.\nFormat: (write/read) (quite/verbose) (normal/test) \"example.txt\"");
            	      } else if (!runType.toLowerCase().equals("quite") && !runType.toLowerCase().equals("verbose")) {
                          System.out.println("Invalid Run Type. Please follow the following format.\nFormat: (write/read) (quite/verbose) (normal/test) \"example.txt\"");
                      } else if (!mode.toLowerCase().equals("normal") && !mode.toLowerCase().equals("test")) {
                          System.out.println("Invalid Mode Specification. Please follow the following format.\nFormat: (write/read) (quite/verbose) (normal/test) \"example.txt\"");
                      } else if (!f.exists() || !f.isFile()) {
                          System.out.println("Invalid file name: Please use a valid file");
                      } else {
                          System.out.println("Hassaan is currently sending the request. Please hold...");
            	          break;
            	      }
    	          } else {
    	              System.out.println("Please follow the format: (write/read) (quite/verbose) (normal/test) \"example.txt\"");
    	          }
    	      }
	      }
    	  scanner.close();
    	  if(requestType != null && runType != null && mode != null && filename != null){
	          Client c = new Client(requestType, runType, mode, filename);
	          //c.sendAndReceive();
          }
	   }
}
