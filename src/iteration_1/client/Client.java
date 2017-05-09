package iteration_1.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import iteration_1.*;

public class Client {
	private DatagramPacket sendReceivePacket;
	private DatagramSocket sendReceiveSocket;
	private PrintService printer;
	private String requestType, filename;
	private Constants.runType runType;
	private int sendPort;
	private static final int INTERMEDIATE_HOST = 23;
	private static final int SERVER = 69;

	   public Client(String requestType, String runType, String mode, String filename){
	      try {
	         // Construct a datagram socket and bind it to any available 
	         // port on the local host machine. This socket will be used to
	         // send and receive UDP Datagram packets.
	         this.requestType = requestType;
	         this.filename = filename;
	         sendReceiveSocket = new DatagramSocket();
             sendPort = mode.equals("normal") ? SERVER: INTERMEDIATE_HOST;
             
             if(runType.equals("quite")){
    	          this.runType = Constants.runType.QUITE;
    	      }else{
    	          this.runType = Constants.runType.VERBOSE;
    	      }
             
	         this.printer = new PrintService(this.runType);
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
	      String mode = "ocTEt";
	      byte data[] = new byte[100]; //buffer for packet receiving 
	      
	      try{
	          
	          if(requestType.equals("read")){
	              stream.write(read_sequence);			 
	    	  }else{
		    	  stream.write(write_sequence);
    		  }
    		  
    		  String[] filePathArray = filename.split("/");
    		  filename = filePathArray[filePathArray.length - 1];    
    		 

        	  stream.write(filename.getBytes());
	    	  stream.write(0);
	    	  stream.write(mode.getBytes());
	    	  stream.write(0);
	    	  
	    	  
	    	  filename = System.getProperty("user.dir").toString() + Constants.clientPath + filename;
              
              if(requestType.equals("read")){
	              FileReceiver receiver = new FileReceiver(runType);
	              receiver.sendPacket(sendPort, InetAddress.getLocalHost(), stream.toByteArray());
                  printer.printMessage("Client: Packet sent.\n");	                                   
	              receiver.receive(filename, sendPort,  InetAddress.getLocalHost(), Constants.ACK);
	    	  }else{
	    	      sendReceivePacket = new DatagramPacket(stream.toByteArray(), stream.toByteArray().length, InetAddress.getLocalHost(), sendPort);
	    	      	    	  
      		      //Print Packet
	    	      printer.printPacketInfo("Client", "Sending", sendReceivePacket);
    		
    		      // Send the datagram packet to the server via the send/receive socket. 
    		      sendReceiveSocket.send(sendReceivePacket);
                  printer.printMessage("Client: Packet sent.\n");	    	      
	    	      
		    	  printer.printMessage("\nWaiting for packet \n");	               
                  sendReceivePacket = new DatagramPacket(data, data.length);
                  
     	          // Block until a datagram is received via sendReceiveSocket.  
     	          sendReceiveSocket.receive(sendReceivePacket);     	    
     	          // Process the received datagram.
     	          printer.printPacketInfo("Client", "Recieved", sendReceivePacket);
     	          sendPort = sendReceivePacket.getPort();
     	          
     	          FileSender sender = new FileSender(runType);
     	          sender.send(filename, sendPort, sendReceivePacket.getAddress());
    		  }

          }catch(Exception e){
	          e.printStackTrace();
    		  System.exit(1);
	      }
//
//	      
//	      while(count!=11){
//	    	  try{
//	              //Construct message
//	    		  if(count == 10){
//	    			  System.out.println("Sending corrupted packet to server");
//	    			  stream.write(invalid_sequence);  
//	    		  }else if(count%2 == 0){//alternate between read and write
//		    		  stream.write(read_sequence);			 
//		    	  }else{
//		    		  stream.write(write_sequence);
//		    	  }
//		    	  stream.write(msg);
//		    	  stream.write(0);
//		    	  stream.write(mode.getBytes());
//		    	  stream.write(0);
//		    	  
//		    	  //Create Packet
//		    	  if(mode.equals("test")){
//		    	      sendReceivePacket = new DatagramPacket(stream.toByteArray(), stream.toByteArray().length,
//		 	                                         InetAddress.getLocalHost(), INTERMEDIATE_HOST);
//		    	  } else {
//		    	      sendReceivePacket = new DatagramPacket(stream.toByteArray(), stream.toByteArray().length,
//		 	                                         InetAddress.getLocalHost(), SERVER);
//	 	          }
//
//		 	      
//		    	  //Print Packet
//		    	  printer.printPacketInfo("Client", "Sending", sendReceivePacket);
//			      
//	
//			      // Send the datagram packet to the server via the send/receive socket. 
//	               sendReceiveSocket.send(sendReceivePacket);
//	               System.out.println("Client: Packet sent.\n");
//	               
//	               
//	               System.out.println("\nWaiting for packet \n");	               
//	               sendReceivePacket = new DatagramPacket(data, data.length);           	
//	     	      // Block until a datagram is received via sendReceiveSocket.  
//	     	      sendReceiveSocket.receive(sendReceivePacket);     	    
//	     	      // Process the received datagram.
//	     	      printer.printPacketInfo("Client", "Recieved", sendReceivePacket);
//			      
//	     	      //Prepare for next loop
//		       	  count++;
//		       	  stream = new ByteArrayOutputStream();
//	    	  }catch(Exception e){
//	    		  e.printStackTrace();
//	    		  System.exit(1);
//	    	  }
//	      }

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
        	          
        	          if(requestType.equals("write")){
        	              filename = System.getProperty("user.dir").toString() + Constants.clientPath + parameters[3];
        	          }
        	          
        	          File f = new File(filename);
        	          if (!requestType.toLowerCase().equals("write") && !requestType.toLowerCase().equals("read")) {
            	          System.out.println("Invalid Request Type. Please follow the following format.\nFormat: (write/read) (quite/verbose) (normal/test) \"example.txt\"");
            	      } else if (!runType.toLowerCase().equals("quite") && !runType.toLowerCase().equals("verbose")) {
                          System.out.println("Invalid Run Type. Please follow the following format.\nFormat: (write/read) (quite/verbose) (normal/test) \"example.txt\"");
                      } else if (!mode.toLowerCase().equals("normal") && !mode.toLowerCase().equals("test")) {
                          System.out.println("Invalid Mode Specification. Please follow the following format.\nFormat: (write/read) (quite/verbose) (normal/test) \"example.txt\"");
                      } else if (requestType.equals("write") && !f.getAbsoluteFile().exists()) {
                          System.out.println("ERROR 1: File does not exist");
                      } else if (requestType.equals("write") && !f.isFile()) {
                          System.out.println("ERROR 2: Input is not a file");
                      } else {
                          System.out.println("Processsing the request. Please hold...");
            	          break;
            	      }
            	      System.out.println("Format: (write/read) (quite/verbose) (normal/test) \"example.txt\"");
    	          } else {
    	              System.out.println("Please follow the format: (write/read) (quite/verbose) (normal/test) \"example.txt\"");
    	          }
    	      }
	      }
    	  scanner.close();
    	  if(requestType != null && runType != null && mode != null && filename != null){
	          Client c = new Client(requestType, runType, mode, filename);
	          c.sendAndReceive();
          }
	   }
}
