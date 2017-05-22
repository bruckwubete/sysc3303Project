package iteration_2.server;
import java.net.*;
import iteration_2.*;
public class WriteThreadSpawn extends Thread{

    private ServerFileReceiver receiver;
    private DatagramPacket writeRequestPacket;
    private String filename;
    private Constants.runType runType;

    public WriteThreadSpawn(DatagramPacket writeRequestPacket, Constants.runType runType) {
        this.writeRequestPacket = writeRequestPacket;
        this.runType = runType;
    }

    public void run() {
        try {
          byte[] ack = new byte[4];
          System.arraycopy(Constants.ACK, 0, ack, 0, 2);
          ack[2] = (byte) 0;
          ack[3] = (byte) 0;
          this.receiver = new ServerFileReceiver(this.runType);
          this.receiver.sendPacket(this.writeRequestPacket.getPort(), this.writeRequestPacket.getAddress(), ack);
          this.filename = System.getProperty("user.dir").toString() + Constants.serverPath + Helper.getFilename(writeRequestPacket);
          this.receiver.receive(this.filename, this.writeRequestPacket.getPort(), this.writeRequestPacket.getAddress());
          System.out.println("Successfully finished a write transaction");
       
    	}catch(Exception e){
    		System.err.println(e.getMessage());
    		System.exit(1);
        }   
    }

}
