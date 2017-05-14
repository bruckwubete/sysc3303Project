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
	   private boolean testMode;
	
	   public Client(String requestType, String runType, String mode, String filename){
	      try {
	         // Construct a datagram socket and bind it to any available 
	         // port on the local host machine. This socket will be used to
	         // send and receive UDP Datagram packets.
	         this.requestType = requestType;
	         this.filename = filename;
	         sendReceiveSocket = new DatagramSocket();
             sendPort = mode.equals("normal") ? Constants.SERVER_LISTENING_PORT: Constants.IH_LISTENING_PORT;
             testMode = mode.equals("normal") ? false: true;
             
             if(runType.equals("quiet")){
    	          this.runType = Constants.runType.QUIET;
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
	              receiver.receive(filename, sendPort,  InetAddress.getLocalHost());
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
     	          
     	          if(!testMode){
     	              sendPort = sendReceivePacket.getPort();
 	              }

     	          
     	          FileSender sender = new FileSender(runType);
     	          sender.send(filename, sendPort, sendReceivePacket.getAddress());
    		  }

          }catch(Exception e){
	          e.printStackTrace();
    		  System.exit(1);
	      }

	      // We're finished, so close the socket.
	      sendReceiveSocket.close();
	   }

	   public static void main(String args[]){

	      Scanner scanner = new Scanner(System.in);
	      String input, requestType, runType, mode, filename;
	      System.out.println("Press \"q\" to quit the program");
	      System.out.println("Enter a request: (write/read) (quiet/verbose) (normal/test) \"filename\"");
	      System.out.println("OR: (write/read) \"filename\" [default: quiet normal]");
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
            	          System.out.println("Invalid Request Type. Please use one of the following formats:");
            	      } else if (!runType.toLowerCase().equals("quite") && !runType.toLowerCase().equals("verbose")) {
                          System.out.println("Invalid Run Type. Please use one of the following formats:");
                      } else if (!mode.toLowerCase().equals("normal") && !mode.toLowerCase().equals("test")) {
                          System.out.println("Invalid Mode Specification. Please use one of the following formats:");
                      } else if (requestType.equals("write") && !f.getAbsoluteFile().exists()) {
                          System.out.println("ERROR 1: File does not exist");
                      } else if (requestType.equals("write") && !f.isFile()) {
                          System.out.println("ERROR 2: Input is not a file");
                      } else {
                          System.out.println("Processsing the request. Please hold...");
            	          break;
            	      }
            	      System.out.println("(write/read) (quite/verbose) (normal/test) \"example.txt\"");
            	      System.out.println("(write/read) \"filename\" [default: quiet normal]");
    	          } else if (parameters.length == 2){
    	              requestType = parameters[0];
    	              runType = "quiet";
    	              mode = "normal";
    	              filename = parameters[1];
    	              
    	              if(requestType.equals("write")){
        	              filename = System.getProperty("user.dir").toString() + Constants.clientPath + parameters[1];
        	          }
    	              
    	              File f = new File(filename);
    	              
    	              if (!requestType.toLowerCase().equals("write") && !requestType.toLowerCase().equals("read")) {
    	                  System.out.println("Invalid Request Type. Please follow one of the following formats:");
    	              } else if (requestType.equals("write") && !f.getAbsoluteFile().exists()) {
                          System.out.println("ERROR 1: File does not exist");
                      } else if (requestType.equals("write") && !f.isFile()) {
                          System.out.println("ERROR 2: Input is not a file");
                      } else {
                          System.out.println("Processing the request. Please hold...");
                          break;
                      }
                      System.out.println("(write/read) (quite/verbose) (normal/test) \"example.txt\"");
            	      System.out.println("(write/read) \"filename\" [default: quiet normal]");
    	          } else {
    	              System.out.println("Invalid. Please use one of the specified formats.");
    	          }
    	      }
	      }
    	  scanner.close();
    	  if(requestType != null && runType != null && mode != null && filename != null){
	          Client c = new Client(requestType, runType, mode, filename);
	          c.sendAndReceive();
	          System.out.println("Successfully finished a " + requestType + " transaction!");
          }
	   }
}
