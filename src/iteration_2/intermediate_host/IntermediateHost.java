package iteration_2.intermediate_host;

import java.io.*;
import java.net.*;
import iteration_2.*;

import java.util.Arrays;
import java.util.Scanner;

public class IntermediateHost {
	private DatagramSocket recieveSocket, sendSocket, sendrecieveSocket;
	private DatagramPacket receivePacket, sendPacket;
	int clientPort;
	private PrintService printer;
	private int sendPort;
	private int simulationOption;
	public static Constants.runType runType;

    public static int OPTION_NORMAL_OPERATION = 0;
    public static int OPTION_INVALID_OP_CODE = 1;
    public static int OPTION_INVALID_MODE = 2;
    public static int OPTION_INVALID_PACKET_FORMATTING = 3;
    public static int OPTION_INVALID_TRANSFER_ID = 4;

	public IntermediateHost(int optionSelected){
		try {
	        // Construct a receive datagram socket and bind it to port 23
			// Construct a sendrecieve datagram socket and bind it to any available
	        // port on the local host machine. This socket will be used to
	        // send and receive UDP Datagram packets.
			recieveSocket = new DatagramSocket(Constants.IH_LISTENING_PORT);			 
			sendrecieveSocket = new DatagramSocket();
            sendPort = Constants.SERVER_LISTENING_PORT;
            sendSocket = new DatagramSocket();
            simulationOption = optionSelected;
			//instantiate printer service instance
			printer = new PrintService(IntermediateHost.runType);

	    } catch (SocketException se) {   // Can't create the socket.
	        se.printStackTrace();
	        System.exit(1);
	    }
	}

	public void sendAndReceive(){
	    int ackCounter = 0;
		while(true){
			byte data[] = new byte[516];
			receivePacket = new DatagramPacket(data, data.length);

		    try {
		        printer.printMessage("Intermidiate Host: Waiting for Packet.\n");
		        // Block until a datagram is received via sendReceiveSocket. 
		    	recieveSocket.receive(receivePacket);

		    	// Process the received datagram.
		    	printer.printPacketInfo("IntermediateHost", "Recieved", receivePacket);

		    	if(Helper.validReadWriteRequest(receivePacket)){
		    		sendPort = Constants.SERVER_LISTENING_PORT;
		    	}
		    	
		   	    //save the port packet was received from
		    	clientPort = receivePacket.getPort();
		    	sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), InetAddress.getLocalHost(), sendPort);		      
      

                /******************************************************************/
		    	if((Helper.isDataOpCodeValid(sendPacket) && sendPacket.getLength() < 516)){
				    System.out.println("Successfully finished a read/write transaction");
	            }
		    	
		    	
		    	
		    	if(Helper.isAckOpCodeValid(sendPacket)){
				    ackCounter++;
				}

				/*
				if(simulationOption == OPTION_INVALID_TRANSFER_ID && ((Helper.isDataOpCodeValid(sendPacket) && sendPacket.getLength() < 516) || (ackCounter == 2 && Helper.isAckOpCodeValid(sendPacket)))){
				    simulateError5(sendPacket);
	            } else if (simulationOption != OPTION_NORMAL_OPERATION && simulationOption != OPTION_INVALID_TRANSFER_ID) {
	               if(Helper.validReadWriteRequest(sendPacket)){
	                    simulateError4(sendPacket, simulationOption, clientPort);                    
	                }else if(ackCounter == 2 && Helper.isAckOpCodeValid(sendPacket) || (Helper.isDataOpCodeValid(sendPacket) && sendPacket.getLength() < 516)){
	                    simulateError4(sendPacket, simulationOption, clientPort);   	                    
	                }
	            }
	            */
		    	
	            if (simulationOption != OPTION_NORMAL_OPERATION && simulationOption != OPTION_INVALID_TRANSFER_ID) {
	               if(Helper.validReadWriteRequest(sendPacket)){
	                    simulateError4(sendPacket, simulationOption, clientPort);                    
	                }else if(ackCounter == 2 && Helper.isAckOpCodeValid(sendPacket) || (Helper.isDataOpCodeValid(sendPacket) && sendPacket.getLength() < 516)){
	                    simulateError4(sendPacket, simulationOption, clientPort);   	                    
	                }
		    	} else {
		    		if(simulationOption == OPTION_INVALID_TRANSFER_ID && ((Helper.isDataOpCodeValid(sendPacket) && sendPacket.getLength() < 516) || (ackCounter == 2 && Helper.isAckOpCodeValid(sendPacket)))){
		    			simulateError5(sendPacket);
		    		}
			    	
		            /*******************************************************************/
	
					
			    	
					//Print out new packet info
				    printer.printPacketInfo("IntermediateHost", "Sending", sendPacket);	
					
			        // Block until a datagram is received via sendReceiveSocket.  
			    	sendrecieveSocket.send(sendPacket);
			    	sendrecieveSocket.setSoTimeout(5000);
			    	sendrecieveSocket.receive(receivePacket); 
	                sendPort = receivePacket.getPort();
	
				    // Process the received datagram and send to client.
				    printer.printPacketInfo("IntermediateHost", "Recieved", receivePacket);		
				    
					sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), InetAddress.getLocalHost(), clientPort);
	
					
	
					/******************************************************************/
	                if(Helper.isAckOpCodeValid(sendPacket)){
					    ackCounter++;
					}
					
	                /*
					if(simulationOption == OPTION_INVALID_TRANSFER_ID && ((Helper.isDataOpCodeValid(sendPacket) && sendPacket.getLength() < 516) || (ackCounter == 2 && Helper.isAckOpCodeValid(sendPacket)))){
					    simulateError5(sendPacket);
		            } else if (simulationOption != OPTION_NORMAL_OPERATION && simulationOption != OPTION_INVALID_TRANSFER_ID){
		                if(Helper.validReadWriteRequest(sendPacket)){
		                    simulateError4(sendPacket, simulationOption, sendPort);                    
		                }else if(ackCounter == 2 && Helper.isAckOpCodeValid(sendPacket) || (Helper.isDataOpCodeValid(sendPacket) && sendPacket.getLength() < 516)){
		                    simulateError4(sendPacket, simulationOption, sendPort);   	                    
		                }
		                simulationOption = OPTION_NORMAL_OPERATION;
		            }
		            */
	                
					
		            if (simulationOption != OPTION_NORMAL_OPERATION && simulationOption != OPTION_INVALID_TRANSFER_ID){
		                if(Helper.validReadWriteRequest(sendPacket)){
		                    simulateError4(sendPacket, simulationOption, sendPort);                    
		                }else if(ackCounter == 2 && Helper.isAckOpCodeValid(sendPacket) || (Helper.isDataOpCodeValid(sendPacket) && sendPacket.getLength() < 516)){
		                    simulateError4(sendPacket, simulationOption, sendPort);   	                    
		                }
		                simulationOption = OPTION_NORMAL_OPERATION;}
		            else{
		            	if(simulationOption == OPTION_INVALID_TRANSFER_ID && ((Helper.isDataOpCodeValid(sendPacket) && sendPacket.getLength() < 516) || (ackCounter == 2 && Helper.isAckOpCodeValid(sendPacket)))){
		            		simulateError5(sendPacket);
		            	}
					
					/*******************************************************************/
					
			    	if((Helper.isDataOpCodeValid(sendPacket) && sendPacket.getLength() < 516)){
					    System.out.println("Successfully finished a read/write transaction");
		            }
		    	
		            }
		    	}					
				printer.printPacketInfo("IntermediateHost", "Sending", sendPacket);
				sendSocket.send(sendPacket);
			    	



		    } catch (SocketTimeoutException e) {
		    	continue;
		    } catch (IOException e) {
		    	
				e.printStackTrace();
				System.exit(1);
		    }
		}
	}

    public void simulateError5(DatagramPacket sendPacket) {
        System.out.println("Simulating ERROR 5 option: "+  simulationOption + " ...");

        try {
            DatagramSocket newSocket = new DatagramSocket();

            newSocket.send(sendPacket);
            byte[] data = new byte[100];
            DatagramPacket receivePacket = new DatagramPacket(data, data.length);
            newSocket.receive(receivePacket);
            printer.printMessage("Received Error Response");
		    printer.printPacketInfo("IntermediateHost", "Receiving Error", receivePacket);

           
            if(Helper.isErrorFiveResponseValid(receivePacket)){
                printer.printMessage("Received Error Packet: Invalid Transfer ID");
                printer.printMessage("");
            }

            newSocket.close();
        } catch (SocketException se) {
            se.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("ERROR 5 simulation ran successfully.");
    }

    public void simulateError4(DatagramPacket sendPacket, int errorOption, int destPort) {
    	System.out.println("Simulating ERROR 4 option: " +  simulationOption + " ...");
      
        if (errorOption == OPTION_INVALID_OP_CODE){    
            byte[] dataBytes = sendPacket.getData();
            dataBytes[0] = 8;
            dataBytes[1] = 9;
            sendPacket.setData(dataBytes);
        } else if (errorOption == OPTION_INVALID_MODE && Helper.validReadWriteRequest(sendPacket)){
            byte[] dataBytes = Helper.makeModeInvalid(Arrays.copyOf(sendPacket.getData(), sendPacket.getLength()));
            sendPacket.setData(dataBytes);
        } else if (errorOption == OPTION_INVALID_PACKET_FORMATTING && Helper.validReadWriteRequest(sendPacket)){
            byte[] dataBytes = sendPacket.getData();
            dataBytes[sendPacket.getLength() - 1] = 3;
            sendPacket.setData(Arrays.copyOf(dataBytes, sendPacket.getLength()));
        }
        try {
            printer.printMessage("Sending invalid packet to server...");
            printer.printPacketInfo("Intermediate Host", "Sending", sendPacket);
            sendrecieveSocket.send(sendPacket);
            
            byte errorData[] = new byte[516];
            DatagramPacket errorPacket = new DatagramPacket(errorData, errorData.length);
            sendrecieveSocket.receive(errorPacket);   
            
            if(Helper.isErrorFourResponseValid(errorPacket)){
            	sendPacket.setData(Arrays.copyOf(errorPacket.getData(), errorPacket.getLength()));
            	sendPacket.setPort(destPort);
                printer.printMessage("Received Error Packet: Error Code 4");
                printer.printPacketInfo("Intermediate Host", "Received", errorPacket);
            }else{
            	System.out.println("ERROR: simulate Error 4 not handled properly");
            	System.exit(1);
            }

        } catch (SocketException se) {
            se.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("ERROR 4 simulation ran successfully.");
    }

    private static void printMenu() {
	    System.out.println("Press \"q\" to quit the program");
	    System.out.println("Error Simulation Menu\n" +
	                       "0: Normal Operation\n" +
	                       "1: Invalid OP Code (Error code 4)\n" + 
	                       "2: Invalid Mode (Error code 4)\n" + 
	                       "3: Invalid RRQ/WRQ Packet Formatting (Error code 4)\n" +
	                       "4: Invalid Transfer ID/Socket (Error code 5)");
	    System.out.println("Command Format: (quiet/verbose) (0..4)[optional; defaulted to option 0]");
    }

    public static void main(String args[]){

	    Scanner scanner = new Scanner(System.in);
	    String userInputRunType, input;
	    int optionSelected = 0; //default operation (normal)

        IntermediateHost.printMenu();
        while(true) {
    	    input = scanner.nextLine();

    	    if(input != null){
    	        if(input.toLowerCase().equals("q")) System.exit(0);

    	        String[] parameters = input.split(" ");
    	        if(parameters.length == 1){
        	        userInputRunType = parameters[0];        	    
        	        if (!userInputRunType.toLowerCase().equals("quiet") && !userInputRunType.toLowerCase().equals("verbose")) {
                        System.out.println("Invalid Run Type. Please follow the following format.\nFormat: (quiet/verbose) (4/5)[optional]");
                    } else {                          
                        System.out.println("Starting the intermediate host in " + userInputRunType + " Mode with no Error Simulation");
            	        break;
            	    }
    	        } else if (parameters.length == 2){
                    userInputRunType = parameters[0];
                    optionSelected = Integer.parseInt(parameters[1]);
          	        if (!userInputRunType.toLowerCase().equals("quiet") && !userInputRunType.toLowerCase().equals("verbose")) {
   	                    System.out.println("Invalid Run Type. Please follow the following format.\nFormat: (quiet/verbose) (0..4)[optional; defaulted to option 0]");
      	            } else if (optionSelected < 0 || optionSelected > 4) {
      	                System.out.println("Error " + parameters[1] + " is not a valid error type. Please enter a number between 0..4, or nothing for no error simulation");
      	            } else {
      	                System.out.println("Starting the intermediate host in " + userInputRunType + " Mode with option " + optionSelected);
      	                break;
      	            }
    	        } else {
    	            System.out.println("Invalid command. Please follow the format: (quiet/verbose) (0..4)[optional; defaulted to option 0]");
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
	        IntermediateHost intermediateHost= new IntermediateHost(optionSelected);
	        intermediateHost.sendAndReceive();

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
