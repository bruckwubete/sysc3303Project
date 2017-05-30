package iteration_4.client;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Scanner;
import iteration_4.*;


public class Client {
	private DatagramPacket sendReceivePacket;
	private DatagramSocket sendReceiveSocket;
	private PrintService printer;
	private String requestType, filename;
	private Constants.runType runType;
	private int sendPort;
	private int retryCount = 0;
	public static Constants.Mode mode;

    public Client(String requestType, String runType, String mode, String filename){
	    try {
	        // Construct a datagram socket and bind it to any available 
	        // port on the local host machine. This socket will be used to
	        // send and receive UDP Datagram packets.
	        this.requestType = requestType;
	        this.filename = filename;
	        sendReceiveSocket = new DatagramSocket();
            sendPort = mode.equals("normal") ? Constants.SERVER_LISTENING_PORT: Constants.IH_LISTENING_PORT;
            Client.mode  = mode.equals("normal") ? Constants.Mode.NORMAL: Constants.Mode.TEST;

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
		
	    byte data[] = new byte[100]; //buffer for packet receiving 

	    try{
	        byte requestData[] = Helper.getRequestData(filename, requestType);

              
            if(requestType.equals("read")){
	            ClientFileReceiver receiver = new ClientFileReceiver(runType);
	            receiver.sendPacket(sendPort, InetAddress.getLocalHost(), requestData);
                printer.printMessage("Client: Packet sent.\n");	                                   
	            receiver.receive(filename, sendPort,  InetAddress.getLocalHost());
	    	}else{
	    	    sendReceivePacket = new DatagramPacket(requestData, requestData.length, InetAddress.getLocalHost(), sendPort);
	    	      	    	  
      		    //Print Packet
	    	    printer.printPacketInfo("Client", "Sending", sendReceivePacket);

    		    // Send the datagram packet to the server via the send/receive socket. 
    		    sendReceiveSocket.send(sendReceivePacket);
                printer.printMessage("Client: Packet sent.\n");	    	      

	    	    printer.printMessage("\nWaiting for packet \n");	               
                sendReceivePacket = new DatagramPacket(data, data.length);

     	        // Block until a datagram is received via sendReceiveSocket.  
     	       do{
                       try{
                           
                           sendReceiveSocket.setSoTimeout(Constants.SENDER_TIMEOUT);
                           sendReceiveSocket.receive(sendReceivePacket);
                           retryCount = 0;
                           break;
                       }catch(SocketTimeoutException e){
                        
                               printer.printMessage("Timed out while waiting for ack packet block 0. Retrying...");   
                               printer.printPacketInfo("Client", "Sending", sendReceivePacket);
                               sendReceiveSocket.send(sendReceivePacket);
                               retryCount++;
                               if(retryCount > 2){
                                   printer.printMessage("Failed after waiting for a ack packet block 0 for 3 times. Quitting...");
                                   return;
                               }                           
                           
                          
                           
                       }catch (IOException e){
        	    		System.err.println(e.getMessage());
        	    		System.exit(1);
        	    	   }
                            
                    }while(retryCount < 3);  	    
     	        // Process the received datagram.
     	        printer.printPacketInfo("Client", "Recieved", sendReceivePacket);

     	        if (Helper.isErrorFourResponseValid(sendReceivePacket)){
	    		    System.out.println("Error code 4 Packet Received: Illegal TFTP operation");
                   //System.exit(1);
	    		} else if (!Helper.isAckOpCodeValid(sendReceivePacket)){
	    		    byte[] errorCode = Helper.formErrorPacket(Constants.ILLEGAL_TFTP_OPERATION, "Ack Op Code not valid");
	    		    DatagramPacket invalidOpcode = new DatagramPacket(errorCode, errorCode.length, sendReceivePacket.getAddress(), sendReceivePacket.getPort());
	    		    sendReceiveSocket.send(invalidOpcode);
	    		    printer.printMessage("Sending Error Code 4");
	    		} else {
	                int expectedPort = sendReceivePacket.getPort();
	     	        if(Client.mode != Constants.Mode.TEST){
	                	sendPort = sendReceivePacket.getPort();
	                }
	
	     	        ClientFileSender sender = new ClientFileSender(runType, expectedPort, sendReceiveSocket);
	     	        sender.send(filename, sendPort, sendReceivePacket.getAddress());
	    		}
    		}

        }catch(Exception e){
	        e.printStackTrace();
	        System.out.print("ERROR: " + e.getMessage());
    		return;
	    }

	    // We're finished, so close the socket.
	    sendReceiveSocket.close();
	}

	public static void main(String args[]){
		boolean loop = true; 
		Scanner scanner = new Scanner(System.in);
		String input, requestType, runType, mode, filename;
		
		scanner = new Scanner(System.in); 
        String directoryInput = "";
        System.out.println("Enter directory to read/write to, must include full path...(leave blank for default)");
        while(true) {
    	      input = scanner.nextLine();
    	      
    	      if(input != null){
    	          if(input.toLowerCase().equals("q")) System.exit(0);
    	          
      	          if(input.toLowerCase().equals("")) {
  	                  Constants.clientReadWriteLocation = System.getProperty("user.dir").toString() + Constants.getClientPath();
      	              System.out.println("Default directory chosen: " + Constants.clientReadWriteLocation );
      	              break;
      	          }
    	          String[] parameters = input.split(" ");
    	          if(parameters.length == 1){
        	          directoryInput = parameters[0];        	
        	          if ( Helper.isValidDirectory(directoryInput) ) {
        	                if (directoryInput.charAt(directoryInput.length() - 1) != Constants.getSlash().charAt(0) ){
        	                    Constants.clientReadWriteLocation = directoryInput + Constants.getSlash() ;
        	                }else {Constants.clientReadWriteLocation = directoryInput;}
                            System.out.println("Your chosen directory: " + Constants.clientReadWriteLocation);
                            break;
                      } else {                          
                          System.out.println("The path provided is not a valid directory, please provide a valid directory...");
            	          continue;
            	      }
    	          } else {
    	              System.out.println("Invalid input format!");
    	          }
    	      }
	      }
        while(loop){
        	input = null; requestType = null; runType = null; mode = null; filename = null;
    	    System.out.println("Press \"q\" to quit the program");
    	    System.out.println("Enter a request: (write/read) (quiet/verbose) (normal/test) \"filename\"");
    	    System.out.println("OR: (write/read) \"filename\" [default: quiet normal]");
            while(true) {
            	if(scanner.hasNextLine()){
            		input = scanner.nextLine();
            	}else{
            		continue;
            	}
        	    

        	    if(input != null){
        	        if(input.toLowerCase().equals("q")){
        	        	 System.out.println("Quitting...");
        	        	 loop = false;
        	        	 break;
        	        }

        	        String[] parameters = input.split(" ");
        	        if(parameters.length == 4){
            	        requestType = parameters[0];
            	        runType = parameters[1];
            	        mode = parameters[2];
            	        filename = parameters[3];

            	        if(requestType.equals("write")){
            	            filename = Constants.clientReadWriteLocation + parameters[3];
            	            System.out.println("This is the chosen path to your file: " + filename);
            	        }

            	        File f = new File(filename);
            	        if (!requestType.toLowerCase().equals("write") && !requestType.toLowerCase().equals("read")) {
                	        System.out.println("Invalid Request Type. Please use one of the following formats:");
                	    } else if (!runType.toLowerCase().equals("quiet") && !runType.toLowerCase().equals("verbose")) {
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
            	            filename = Constants.clientReadWriteLocation + parameters[1];
            	            System.out.println("This is the chosen path to your file: " + filename);
            	        }

        	            File f = new File(filename);

        	            if (!requestType.toLowerCase().equals("write") && !requestType.toLowerCase().equals("read")) {
        	                System.out.println("Invalid Request Type. Please follow one of the following formats:");
        	            } else if (requestType.equals("write") && !f.getAbsoluteFile().exists()) {
                            System.out.println("ERROR 1: File " + parameters[1] +  " does not exist");
                        } else if (requestType.equals("write") && !f.isFile()) {
                            System.out.println("ERROR 2: Input is not a file");
                        } else if ((requestType.equals("write") && !Files.isReadable(f.toPath())) || (requestType.equals("read") && f.exists() && !f.isDirectory() && !Files.isWritable(f.toPath()))) {
                            System.out.println("ERROR 3: File access violation");
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
        	if(requestType != null && runType != null && mode != null && filename != null){
    	        Client c = new Client(requestType, runType, mode, filename);
    	        c.sendAndReceive();
            }        	
        }
        scanner.close();
	}
}