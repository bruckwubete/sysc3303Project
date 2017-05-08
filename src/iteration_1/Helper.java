package iteration_1;
import java.net.*;
import java.util.Arrays;

public class Helper {

    public static String getFilename(DatagramPacket writeRequestPacket){

	    byte[] packetData = writeRequestPacket.getData();

	    //find location of first 0
	    int index = 2;

	    for (int i = 2; i < packetData.length; i++ ) {
	        if (packetData[i] == 0) {
	            break;
	        }
	        index++;
	    }

        return new String(Arrays.copyOfRange(packetData, 2, index));

     }
}
