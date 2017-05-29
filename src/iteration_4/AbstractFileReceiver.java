package iteration_4;
import java.net.*;
import java.io.*;
public abstract class AbstractFileReceiver {

    public abstract void sendPacket(int port, InetAddress destAddress, byte[] ack) throws FileNotFoundException, IOException;
    
    public abstract void receive(String filename, int destPort, InetAddress destAddress) throws FileNotFoundException, IOException;
}
