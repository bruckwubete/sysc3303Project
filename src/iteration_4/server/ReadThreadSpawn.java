package iteration_4.server;
import java.net.*;
import iteration_4.*;

public class ReadThreadSpawn extends Thread {
    
    private ServerFileSender fileSender;
    private DatagramPacket readRequestPacket;
    private String filename;
    private Constants.runType runType;

    public ReadThreadSpawn(DatagramPacket readRequestPacket, Constants.runType runType){
        this.readRequestPacket =  readRequestPacket;
        this.runType = runType;
    }
    
    public void run() {
        try {

	        this.fileSender = new ServerFileSender(this.runType);         
	        this.filename = System.getProperty("user.dir").toString() + Constants.getServerPath() + Helper.getFilename(readRequestPacket);
            this.fileSender.send(this.filename, readRequestPacket.getPort(), readRequestPacket.getAddress());
            

		}catch(Exception e){
			System.err.println(e.getMessage());
    		System.exit(1);
	    }   
    }

}
