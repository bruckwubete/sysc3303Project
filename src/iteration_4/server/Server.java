package iteration_4.server;
import java.util.Scanner;
import java.net.*;
import iteration_4.*;

public class Server {
    private Thread serverLogicThread;
    private DatagramSocket receiveSocket;
    public static Constants.runType runType;

    public Server() {
        try{
            
            this.receiveSocket = new DatagramSocket(Constants.SERVER_LISTENING_PORT);
            this.serverLogicThread = new ServerLogicThreadSpawn(this.receiveSocket);       
            this.serverLogicThread.start();
            
        } catch (SocketException se) {
	         se.printStackTrace();
	         System.exit(1);
	      }
    }
    
    public void terminate(){
        try{
            this.receiveSocket.close();
            this.serverLogicThread.interrupt();
        }catch(Exception e){
            System.out.println("Failed to close the ServerLogicThread");
            e.printStackTrace();
			System.exit(0);
        }        
    }

    public static void main(String args[]){
        
        Scanner scanner = new Scanner(System.in);
        String userInputRunType, input;
        String directoryInput = "";
        System.out.println("Enter directory to read/write to, must include full path...(leave blank for default)");
        while(true) {
    	      input = scanner.nextLine();
    	      
    	      if(input != null){
    	          if(input.toLowerCase().equals("q")) System.exit(0);
    	          
    	          if(input.toLowerCase().equals("")) {
  	                  Constants.serverReadWriteLocation = System.getProperty("user.dir").toString() + Constants.getServerPath();
  	                  System.out.println("Default directory chosen: " + Constants.serverReadWriteLocation);
          	          break;
      	          }
    	          String[] parameters = input.split(" ");
    	          if(parameters.length == 1){
        	          directoryInput = parameters[0];        	    
        	          if ( Helper.isValidDirectory(directoryInput) ) {
        	                if (directoryInput.charAt(directoryInput.length() - 1) != Constants.getSlash().charAt(0) ){
        	                    Constants.serverReadWriteLocation = directoryInput + Constants.getSlash() ;
        	                }else {Constants.serverReadWriteLocation = directoryInput;}
                            System.out.println("Your chosen directory: " + Constants.serverReadWriteLocation);
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
    	      Server.runType = runType;
	          Server server = new Server();
    	      boolean go = true;
    	      
              while(go) {
        	      input = scanner.nextLine();
    
        	      if(input != null){
        	          if(input.toLowerCase().equals("q")) {
        	              server.terminate();
        	              scanner.close();
        	              go = false;
    	              }
    
        	      }
    	      }
          }	      
	      
    }
}