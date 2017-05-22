package iteration_3.server;
import java.net.*;
import iteration_3.*;

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
            System.out.println("Successfully finished a read transaction");

		}catch(Exception e){
			System.err.println(e.getMessage());
    		System.exit(1);
	    }   
    }

}
