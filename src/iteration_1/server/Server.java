package iteration_1.server;
import java.util.Scanner;
import java.net.*;
import iteration_1.*;
public class Server {
    private Thread serverLogicThread;
    private DatagramSocket receiveSocket;
    public static int CLIENT_SIDE_PORT = 69;
    public static Constants.runType runType;

    public Server() {
        try{
            
            this.receiveSocket = new DatagramSocket(Server.CLIENT_SIDE_PORT);
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
	      System.out.println("Press \"q\" to quit the program");
	      System.out.println("Enter run type: (quite/verbose)");
          while(true) {
    	      input = scanner.nextLine();
    	      
    	      if(input != null){
    	          if(input.toLowerCase().equals("q")) System.exit(0);
    	          
    	          String[] parameters = input.split(" ");
    	          if(parameters.length == 1){
        	          userInputRunType = parameters[0];        	    
        	          if (!userInputRunType.toLowerCase().equals("quite") && !userInputRunType.toLowerCase().equals("verbose")) {
                          System.out.println("Invalid Run Type. Please follow the following format.\nFormat: (write/read) (quite/verbose) (normal/test) \"example.txt\"");
                      } else {                          
                          System.out.println("Starting the server in " + userInputRunType + " Mode...");
            	          break;
            	      }
    	          } else {
    	              System.out.println("Unrecognized Run Type. Please Enter Run Type: (quite/verbose)");
    	          }
    	      }
	      }
    
    	  if(userInputRunType != null){
    	      Constants.runType runType;
    	      if(userInputRunType.equals("quite")){
    	          runType = Constants.runType.QUITE;
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