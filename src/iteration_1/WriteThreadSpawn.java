package iteration_1;
import java.net.*;

public class WriteThreadSpawn extends Thread{

    private FileReceiver receiver;
    private DatagramPacket writeRequestPacket;
    private String filename;
    public static byte[] ACK = {'3', '3'};
    
    

    WriteThreadSpawn(DatagramPacket writeRequestPacket) {
        this.writeRequestPacket = writeRequestPacket;
    }
    
    public void run() {
        try {
            
          this.receiver = new FileReceiver();
          this.filename = Helper.getFilename(writeRequestPacket);
          this.receiver.receive(this.filename, this.writeRequestPacket.getPort(), this.writeRequestPacket.getAddress(), WriteThreadSpawn.ACK);
       
    	  }
    	  catch(Exception e){
    	      e.printStackTrace();
    	      System.exit(1);
          }   
     }    
     
}
