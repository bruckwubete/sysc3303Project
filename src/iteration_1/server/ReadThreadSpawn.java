package iteration_1.server;
import java.net.*;
import iteration_1.*;

public class ReadThreadSpawn extends Thread {
    
    private FileSender fileSender;
    private DatagramPacket readRequestPacket;
    private String filename;
    private Constants.runType runType;

    public ReadThreadSpawn(DatagramPacket readRequestPacket, Constants.runType runType){
        this.readRequestPacket =  readRequestPacket;
        this.runType = runType;
    }
    
    public void run() {
        try {

	        this.fileSender = new FileSender(this.runType);         
	        this.filename = System.getProperty("user.dir").toString() + Constants.serverPath + Helper.getFilename(readRequestPacket);
            this.fileSender.send(this.filename, readRequestPacket.getPort(), readRequestPacket.getAddress());

		}catch(Exception e){
		    e.printStackTrace();
		    System.exit(1);
	    }   
    }

}
