package iteration_1;

import java.net.DatagramPacket;

public class PrintService {
    private boolean isQuite;
    public static enum Mode { QUITE, VERBOSE};
	
	public PrintService(Constants.runType runType){
          this.isQuite = (runType == Constants.runType.QUITE) ? true: false;
	}
	
	 public void printPacketInfo(String who, String sendRecieve, DatagramPacket sendReceivePacket){
	      if(!isQuite){
    		  int len = sendReceivePacket.getLength();
    		  byte[] data = sendReceivePacket.getData();
    		  if(sendRecieve.equals("Sending")){
    			  System.out.println( who + " Sending packet:");
    			  System.out.println("To host: " + sendReceivePacket.getAddress());
    			  System.out.println("Destination host port: " + sendReceivePacket.getPort());			  			      
    		  }else{
    			  System.out.println(who + " Recieved packet:");
    			  System.out.println("From host: " + sendReceivePacket.getAddress());
    			  System.out.println("Sender/Host port: " + sendReceivePacket.getPort());
    		  }
    		  System.out.println("Length: " + len);
    		  System.out.print("Containing: \n");
    		  System.out.print("String: " + new String(data,0,len)); // or could print "s"
    		  System.out.println("\nBytes ");
    		  for(int index=0; index < sendReceivePacket.getLength(); index++){
    			  System.out.println("Byte "+ index+ ":  " + data[index]);
    		  }
    		  System.out.println("\n");		
	      }

	   }

}
