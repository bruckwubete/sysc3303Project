package iteration_4.intermediate_host;

import java.io.*;
import java.net.*;
import iteration_4.*;

import java.util.Arrays;
import java.util.Scanner;

public class IntermediateHost {
	private DatagramSocket recieveSocket, sendSocket, sendrecieveSocket;
	private DatagramPacket receivePacket, sendPacket;
	int clientPort;
	private PrintService printer;
	private int sendPort;
	private int simulationOption;
	private Constants.packetType packetType;
	private int packetNumber;
	private int delay;
	public static Constants.runType runType;

    //dataSender is only used when simulating a lost packet
    //dataSender = 1 -> client is the data sender
    //dataSender = 2 -> server is the data sender
    //dataSender = 0 -> data sender not set yet or different error simulation chosen
    private int dataSender = 0;

    public static int OPTION_NORMAL_OPERATION = 0;
    public static int OPTION_INVALID_OP_CODE = 1;
    public static int OPTION_INVALID_MODE = 2;
    public static int OPTION_INVALID_PACKET_FORMATTING = 3;
    public static int OPTION_INVALID_TRANSFER_ID = 4;
    public static int OPTION_DUPLICATE_PACKET = 5;
    public static int OPTION_LOSE_PACKET = 6;
    public static int OPTION_DELAY_PACKET = 7;

	public IntermediateHost(int optionSelected, String packetType, int numberOfPacket, int delay){
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
            if(packetType.toLowerCase().equals("data")){
                this.packetType = Constants.packetType.DATA;
            } else if (packetType.toLowerCase().equals("ack")){
                this.packetType = Constants.packetType.ACK;
            } else if (packetType.toLowerCase().equals("rq")){
                this.packetType = Constants.packetType.REQUEST;
            }
            this.delay = delay;
            packetNumber = numberOfPacket;
			//instantiate printer service instance
			printer = new PrintService(IntermediateHost.runType);

	    } catch (SocketException se) {   // Can't create the socket.
	        se.printStackTrace();
	        System.exit(1);
	    }
	}

	public void sendAndReceive(){
	    int ackCounter = 0;
	    int dataCounter = 0;
	    int requestCounter = 0;
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
      
                if(Helper.isDataOpCodeValid(sendPacket)){
                    dataSender = 1;
                }

		    	if((Helper.isDataOpCodeValid(sendPacket) && sendPacket.getLength() < 516)){
				    System.out.println("Successfully finished a read/write transaction");
	            }

		    	if(Helper.isAckOpCodeValid(sendPacket)){
				    ackCounter++;
				} else if (Helper.isDataOpCodeValid(sendPacket)){
				    dataCounter++;
				} else if (Helper.validReadWriteRequest(sendPacket)){
				    requestCounter++;
				}

				if (dataSender == 1 && (simulationOption == OPTION_LOSE_PACKET && Helper.isAckOpCodeValid(sendPacket) && packetType == Constants.packetType.ACK && ackCounter == packetNumber) || (simulationOption == OPTION_LOSE_PACKET && Helper.isDataOpCodeValid(sendPacket) && packetType == Constants.packetType.DATA && dataCounter == packetNumber) || (simulationOption == OPTION_LOSE_PACKET && Helper.validReadWriteRequest(sendPacket) && packetType == Constants.packetType.REQUEST && requestCounter == packetNumber)){
					continue;
				}

	            if (simulationOption != OPTION_NORMAL_OPERATION && simulationOption != OPTION_INVALID_TRANSFER_ID && simulationOption != OPTION_DELAY_PACKET && simulationOption != OPTION_DUPLICATE_PACKET && simulationOption != OPTION_LOSE_PACKET && ((packetType == Constants.packetType.ACK && ackCounter == packetNumber) || (packetType == Constants.packetType.DATA && dataCounter == packetNumber) || packetType == Constants.packetType.REQUEST)) {
	                if(packetType == Constants.packetType.REQUEST && Helper.validReadWriteRequest(sendPacket)){
	                    simulateError4(sendPacket, simulationOption, clientPort);
	                } else if(packetType == Constants.packetType.ACK && (ackCounter == packetNumber && Helper.isAckOpCodeValid(sendPacket))){
	                    simulateError4(sendPacket, simulationOption, clientPort);
	                } else if(packetType == Constants.packetType.DATA && (dataCounter == packetNumber && Helper.isDataOpCodeValid(sendPacket))){
	                    simulateError4(sendPacket, simulationOption, clientPort);
	                }
		    	} else {
		    		if(simulationOption == OPTION_INVALID_TRANSFER_ID && ((Helper.isDataOpCodeValid(sendPacket) && dataCounter == packetNumber  && packetType == Constants.packetType.DATA) || (Helper.isAckOpCodeValid(sendPacket) && dataCounter == packetNumber && packetType == Constants.packetType.ACK))){
		    			simulateError5(sendPacket);
		    		}

					//Print out new packet info
				    printer.printPacketInfo("IntermediateHost", "Sending", sendPacket);	
					
		    		if(simulationOption == OPTION_DELAY_PACKET && ((Helper.isDataOpCodeValid(sendPacket) && dataCounter == packetNumber  && packetType == Constants.packetType.DATA) || (Helper.isAckOpCodeValid(sendPacket) && dataCounter == packetNumber && packetType == Constants.packetType.ACK))){
	    		        try {
	    		            Thread.sleep( (long)delay);
    		            } catch (InterruptedException e) {
    		                e.printStackTrace();
    		            }
	    		    }
			        // Block until a datagram is received via sendReceiveSocket.
			        if(!(simulationOption == OPTION_LOSE_PACKET && (Helper.isAckOpCodeValid(sendPacket) && packetType == Constants.packetType.ACK && ackCounter == packetNumber) || (Helper.isDataOpCodeValid(sendPacket) && packetType == Constants.packetType.DATA && dataCounter == packetNumber))){
			    	    sendrecieveSocket.send(sendPacket);
		    	    }

					if (simulationOption == OPTION_DUPLICATE_PACKET && Helper.isDataOpCodeValid(sendPacket) && packetType == Constants.packetType.DATA && dataCounter == packetNumber){
	    			    	try{Thread.sleep( (long) delay);}
	    			    	catch(InterruptedException e){e.printStackTrace();}
	    			    	printer.printMessage("Sending Duplicate DATA");
	    			    	printer.printPacketInfo("IntermediateHost", "Sending", sendPacket);
	    			    	sendrecieveSocket.send(sendPacket);
		    	     }
					if (simulationOption == OPTION_DUPLICATE_PACKET && Helper.isAckOpCodeValid(sendPacket) && packetType == Constants.packetType.ACK && ackCounter == packetNumber){
   			    		    try{Thread.sleep( (long) delay);}
	    			    	catch(InterruptedException e){e.printStackTrace();}
	    			    	printer.printMessage("Sending Duplicate ACK");
	    			    	printer.printPacketInfo("IntermediateHost", "Sending", sendPacket);
		   			    	sendrecieveSocket.send(sendPacket);
				    }
			    	sendrecieveSocket.setSoTimeout(5000);
			    	sendrecieveSocket.receive(receivePacket); 
	                sendPort = receivePacket.getPort();
	                
                    if(Helper.isDataOpCodeValid(sendPacket)){
                        dataSender = 2;
                    }
	
				    // Process the received datagram and send to client.
				    printer.printPacketInfo("IntermediateHost", "Recieved", receivePacket);		
				    
					sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), InetAddress.getLocalHost(), clientPort);
	
	                if(Helper.isAckOpCodeValid(sendPacket)){
					    ackCounter++;
					} else if (Helper.isDataOpCodeValid(sendPacket)){
					    dataCounter++;
					}
					
					if (dataSender == 2 && (simulationOption == OPTION_LOSE_PACKET && Helper.isAckOpCodeValid(sendPacket) && packetType == Constants.packetType.ACK && ackCounter == packetNumber) || (simulationOption == OPTION_LOSE_PACKET && Helper.isDataOpCodeValid(sendPacket) && packetType == Constants.packetType.DATA && dataCounter == packetNumber)){
    					sendrecieveSocket.setSoTimeout(5000);
    			    	sendrecieveSocket.receive(receivePacket); 
    	                sendPort = receivePacket.getPort();
    	
    				    // Process the received datagram and send to client.
    				    printer.printPacketInfo("IntermediateHost", "Recieved", receivePacket);		
    				    
    					sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), InetAddress.getLocalHost(), clientPort);
				    }
				    else if(dataSender == 1 && (simulationOption == OPTION_LOSE_PACKET && Helper.isAckOpCodeValid(sendPacket) && packetType == Constants.packetType.ACK && ackCounter == packetNumber) || (simulationOption == OPTION_LOSE_PACKET && Helper.isDataOpCodeValid(sendPacket) && packetType == Constants.packetType.DATA && dataCounter == packetNumber)){
				        continue;
				    }

		            if (simulationOption != OPTION_NORMAL_OPERATION && simulationOption != OPTION_INVALID_TRANSFER_ID && simulationOption != OPTION_DELAY_PACKET && simulationOption != OPTION_DUPLICATE_PACKET && simulationOption != OPTION_LOSE_PACKET && ((packetType == Constants.packetType.ACK && ackCounter == packetNumber) || (packetType == Constants.packetType.DATA && dataCounter == packetNumber))){
//		                if(Helper.validReadWriteRequest(sendPacket)){
//		                    simulateError4(sendPacket, simulationOption, sendPort);                    
//		                } else if(ackCounter == 2 && Helper.isAckOpCodeValid(sendPacket) || (Helper.isDataOpCodeValid(sendPacket) && sendPacket.getLength() < 516)){
//		                    simulateError4(sendPacket, simulationOption, sendPort);   	                    
//		                }
                        if(packetType == Constants.packetType.REQUEST && Helper.validReadWriteRequest(sendPacket)){
	                        simulateError4(sendPacket, simulationOption, sendPort);
	                    } else if(packetType == Constants.packetType.ACK && (ackCounter == packetNumber && Helper.isAckOpCodeValid(sendPacket))){
	                        simulateError4(sendPacket, simulationOption, sendPort);
	                    } else if(packetType == Constants.packetType.DATA && (dataCounter == packetNumber && Helper.isDataOpCodeValid(sendPacket))){
	                        simulateError4(sendPacket, simulationOption, sendPort);
	                    }
		                simulationOption = OPTION_NORMAL_OPERATION;
		            }else{
		            	if(simulationOption == OPTION_INVALID_TRANSFER_ID && ((Helper.isDataOpCodeValid(sendPacket) && dataCounter == packetNumber) || (Helper.isAckOpCodeValid(sendPacket) && ackCounter == packetNumber))){
		            		simulateError5(sendPacket);
		            	}

			    	    if((Helper.isDataOpCodeValid(sendPacket) && sendPacket.getLength() < 516)){
					        System.out.println("Successfully finished a read/write transaction");
		                }

		            }
		    	}
				printer.printPacketInfo("IntermediateHost", "Sending", sendPacket);
				if(simulationOption == OPTION_DELAY_PACKET && ((Helper.isDataOpCodeValid(sendPacket) && dataCounter == packetNumber  && packetType == Constants.packetType.DATA) || (Helper.isAckOpCodeValid(sendPacket) && dataCounter == packetNumber && packetType == Constants.packetType.ACK))){
	    		    try {
	    		        Thread.sleep((long) delay);
    		        } catch (InterruptedException e) {
    		            e.printStackTrace();
    		        }
	    		}
				sendSocket.send(sendPacket);
                if (simulationOption == OPTION_DUPLICATE_PACKET && Helper.isDataOpCodeValid(sendPacket) && packetType == Constants.packetType.DATA && dataCounter == packetNumber){
	    	        try {
	    	            Thread.sleep((long) delay);
	    	        } catch(InterruptedException e){
	    	            e.printStackTrace();
	    	        }
	    	        printer.printMessage("Sending Duplicate DATA");
	    			printer.printPacketInfo("IntermediateHost", "Sending", sendPacket);
	    			sendSocket.send(sendPacket);
		    	}
				if (simulationOption == OPTION_DUPLICATE_PACKET && Helper.isAckOpCodeValid(sendPacket) && packetType == Constants.packetType.ACK && ackCounter == packetNumber){
   			    	try {
   			    	    Thread.sleep((long) delay);
   			    	} catch(InterruptedException e){
   			    	    e.printStackTrace();
   			    	}
   			    	printer.printMessage("Sending Duplicate ACK");
	    			printer.printPacketInfo("IntermediateHost", "Sending", sendPacket);
		   		    sendSocket.send(sendPacket);
				}

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
            sendSocket.send(sendPacket);

            byte errorData[] = new byte[516];
            DatagramPacket errorPacket = new DatagramPacket(errorData, errorData.length);
            sendSocket.receive(errorPacket);   
            
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
	                       "4: Invalid Transfer ID/Socket (Error code 5)\n" +
	                       "5: Duplicate Packet\n" +
	                       "6: Lose Packet\n" +
	                       "7: Delay Packet");
	    System.out.println("Command Format: (quiet/verbose) (0..7)[default to 0] (rq/data/ack) (packetNumber)");
    }

    public static void main(String args[]){

	    Scanner scanner = new Scanner(System.in);
	    String userInputRunType, input;
	    int optionSelected = 0; //default operation (normal)
	    String packetType = "";
	    int numberOfPacket = 0;
	    int delay = 0;

        IntermediateHost.printMenu();
        while(true) {
    	    input = scanner.nextLine();

    	    if(input != null){
    	        if(input.toLowerCase().equals("q")) System.exit(0);

    	        String[] parameters = input.split(" ");
    	        if(parameters.length == 1){
        	        userInputRunType = parameters[0];        	    
        	        if (!userInputRunType.toLowerCase().equals("quiet") && !userInputRunType.toLowerCase().equals("verbose")) {
                        System.out.println("Invalid Run Type. Please follow the following format.\nFormat: (quiet/verbose) (0..7)[default to 0] (rq/data/ack) (packetNumber)");
                    } else {                          
                        System.out.println("Starting the intermediate host in " + userInputRunType + " Mode with no Error Simulation");
            	        break;
            	    }
    	        } else if (parameters.length == 2){
    	            userInputRunType = parameters[0];
    	            if(parameters[1] != null && parameters[1].matches("[-+]?\\d*\\.?\\d+")){
    	                optionSelected = Integer.parseInt(parameters[1]);
    	            }

    	            if (!userInputRunType.toLowerCase().equals("quiet") && !userInputRunType.toLowerCase().equals("verbose")) {
   	                    System.out.println("Invalid Run Type. Please follow the following format.\nFormat: (quiet/verbose) (0..4)[default to 0] (rq/data/ack) (packetNumber)");
      	            } else if (optionSelected != 0) {
      	                System.out.println("For options 1..7, please specify packet type and packet number");
      	            } else if (optionSelected == 0) {
      	                System.out.println("Starting the intermediate host in " + userInputRunType + " Mode with option " + optionSelected);
      	                break;
      	            }
    	        }/*else if (parameters.length == 2){
                    userInputRunType = parameters[0];
                    optionSelected = Integer.parseInt(parameters[1]);
          	        if (!userInputRunType.toLowerCase().equals("quiet") && !userInputRunType.toLowerCase().equals("verbose")) {
   	                    System.out.println("Invalid Run Type. Please follow the following format.\nFormat: (quiet/verbose) (0..4)[default to 0] (rq/data/ack) (packetNumber)");
      	            } else if (optionSelected < 0 || optionSelected > 4) {
      	                System.out.println("Error " + parameters[1] + " is not a valid error type. Please enter a number between 0..4, or nothing for no error simulation");
      	            } else {
      	                System.out.println("Starting the intermediate host in " + userInputRunType + " Mode with option " + optionSelected);
      	                break;
      	            }
    	        }*/ else if (parameters.length == 4){
    	            userInputRunType = parameters[0];
    	            if(parameters[1] != null && parameters[1].matches("[-+]?\\d*\\.?\\d+")){
    	                optionSelected = Integer.parseInt(parameters[1]);
    	            }
    	            packetType = parameters[2];
    	            if(parameters[3] != null && parameters[3].matches("[-+]?\\d*\\.?\\d+")){
    	                numberOfPacket = Integer.parseInt(parameters[3]);
    	            }

    	            if (!userInputRunType.toLowerCase().equals("quiet") && !userInputRunType.toLowerCase().equals("verbose")) {
   	                    System.out.println("Invalid Run Type. Please follow the following format.\nFormat: (quiet/verbose) (0..7)[default to 0] (rq/data/ack) (packetNumber)");
      	            } else if (optionSelected < 0 || optionSelected > 7) {
      	                System.out.println("Option " + parameters[1] + " is not a valid error type. Please enter a number between 0..7, or nothing for no error simulation");
      	            } else if (!packetType.toLowerCase().equals("rq") && !packetType.toLowerCase().equals("data") && !packetType.toLowerCase().equals("ack")) {
      	                System.out.println("Invalid Packet Type. Please follow the following format.\nFormat: (quiet/verbose) (0..7)[default to 0] (rq/data/ack) (packetNumber)");
      	            } else if (numberOfPacket <= 0) {
      	                System.out.println("Invalid Packet Number. Please follow the following format.\nFormat: (quiet/verbose) (0..7)[default to 0] (rq/data/ack) (packetNumber)");
      	            } else if (packetType.toLowerCase().equals("rq") && numberOfPacket != 1) {
      	                System.out.println("Only packet number 1 can be used for a request packet");
      	            } else if (optionSelected == 4 && (packetType.toLowerCase().equals("data") || packetType.toLowerCase().equals("ack")) && numberOfPacket == 1) {
      	                System.out.println("Cannot simulate invalid transfer id error on first data/ack packet");
      	            } else if (optionSelected == 4 && packetType.toLowerCase().equals("rq")) {
      	                System.out.println("Cannot simulate invalid transfer id on a request packet");
      	            } else if ((optionSelected == 2 || optionSelected == 3) && !packetType.toLowerCase().equals("rq")) {
      	                System.out.println("Cannot simulate option 2 or 3 with data or ack packets");
      	            } else {
      	                System.out.println("Starting the intermediate host in " + userInputRunType + " Mode with the following options: " + optionSelected + ", " + packetType + ", " + numberOfPacket);
      	                break;
      	            }
    	        } else {
    	            System.out.println("Invalid command. Please follow the format: (quiet/verbose) (0..7)[default to 0] (rq/data/ack) (packetNumber)");
    	        }
    	    }
	    }

	    if(optionSelected == 5 || optionSelected == 7)
	        System.out.println("Please specify delay length in milliseconds");
	    while(optionSelected == 5 || optionSelected == 7){
	        input = scanner.nextLine();

    	    if(input != null){
    	        if(input.toLowerCase().equals("q")) System.exit(0);
    	        
    	        String[] parameters = input.split(" ");
    	        if(parameters.length == 1){
	                if(parameters[0] != null && parameters[0].matches("[-+]?\\d*\\.?\\d+")){
    	                delay = Integer.parseInt(parameters[0]);
    	                break;
    	            } else {
    	                System.out.println("Please enter a valid number for the delay");
    	            }
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
	        IntermediateHost intermediateHost= new IntermediateHost(optionSelected, packetType, numberOfPacket, delay);
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
