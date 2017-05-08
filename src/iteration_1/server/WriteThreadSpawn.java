package iteration_1.server;
import java.net.*;
import iteration_1.*;
public class WriteThreadSpawn extends Thread{

    private FileReceiver receiver;
    private DatagramPacket writeRequestPacket;
    private String filename;
    private Constants.runType runType;

    public WriteThreadSpawn(DatagramPacket writeRequestPacket, Constants.runType runType) {
        this.writeRequestPacket = writeRequestPacket;
        this.runType = runType;
    }

    public void run() {
        try {
            
          this.receiver = new FileReceiver(this.runType);
          this.filename = Helper.getFilename(writeRequestPacket);
          this.receiver.sendPacket(this.writeRequestPacket.getPort(), this.writeRequestPacket.getAddress(), Constants.ACK);
          this.receiver.receive(this.filename, this.writeRequestPacket.getPort(), this.writeRequestPacket.getAddress(), Constants.ACK);
       
    	}catch(Exception e){
    	    e.printStackTrace();
    	    System.exit(1);
        }   
    }
     
}
