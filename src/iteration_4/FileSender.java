package iteration_4;

import java.net.*;
import java.io.*;

/**
 * FileTransmitter.java - Demonstrates how to use Java's byte stream I/O
 * classes to transmit a file. Copies the contents of in.dat to out.dat.
 *
 * @version 1.1 February 26, 2002
 */

public abstract class FileSender {

    public DatagramSocket sendReceiveSocket;
    public DatagramPacket sendPacket, receivePacket;
    public PrintService printer;
    public int blockNumber = 0;


    abstract public void send(String filename, int destPort, InetAddress destAddress) throws FileNotFoundException, IOException, Exception;

}
