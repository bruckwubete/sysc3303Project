package iteration_1;
import java.net.*;

public class ReadThreadSpawn extends Thread {
    
    private DatagramPacket readRequestPacket;
    private String filename;

    ReadThreadSpawn(DatagramPacket readRequestPacket){
        this.readRequestPacket =  readRequestPacket;
    }
    
     public void run() {
        try {
	      
	      this.filename = Helper.getFilename(readRequestPacket);           
          FileSender.transmit(this.filename, readRequestPacket.getPort(), readRequestPacket.getAddress());
	   
		  }catch(Exception e){
		      e.printStackTrace();
		      System.exit(1);
	      }   
    }

}
