package iteration_1;
import java.util.Scanner;
import java.net.*;
public class Server {
    private Thread serverLogicThread;
    private DatagramSocket receiveSocket;
    public static int CLIENT_SIDE_PORT = 69;

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
	      String input;
	      System.out.println("Press \"q\" to quit the program");
	      
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