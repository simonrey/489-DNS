package main;
import java.io.IOException;
import java.net.*;

import argumentProcessing.*;
import messageUDP.*;

public class DNS_Client{

	public static void main(String[] args) throws IOException{
	
		String[] args1=new String[4];
		args1[0]="-t";
		args1[1]="10";
		args1[2]="@8.8.8.8";
		args1[3]="www.cnn.com";
		
		if(args1.length>7||args1.length<2){
			System.out.println("Error, too many or too few arguments\nProgram will terminate.");
			System.exit(0);
		}
				
		
		ArgumentBuffer buffer = new ArgumentBuffer(args1);
		buffer.fillArgumentBuffer();
		

		
		
		MessageBuilderUDP myMessageBuilder = new MessageBuilderUDP();
		byte[] message = myMessageBuilder.buildSendMessage(buffer);
		byte[] receiveData=new byte[1024];
		
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress ipAddress = InetAddress.getByAddress(buffer.getByteIP());
				
		DatagramPacket sendPacket = new DatagramPacket(message,message.length, ipAddress, buffer.getPort());
		clientSocket.setSoTimeout(buffer.getTimeout()*1000);
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		
		System.out.println("DNS Client sending request for: "+buffer.getName());
		System.out.println("Server: "+buffer.getStringIP());
		System.out.println("Request type: "+buffer.getTypeString());
		
		long tic=System.currentTimeMillis();
		
		int i=0;
		boolean received=false;
		while(i<buffer.getRetry()){
			clientSocket.send(sendPacket);
			try{
				clientSocket.receive(receivePacket);
				long toc=System.currentTimeMillis();
				System.out.println("Response received after: "+((toc-tic)/1000)+" seconds");
				System.out.println("Number of retries: "+i);
				i=buffer.getRetry();
				received=true;
			}
			catch (SocketTimeoutException ste){
				System.out.println("Timeout, will retry if available: " + i);
				i++;
				continue;
			}
		}
		
		if(!received){
			System.out.println("Unable to receive message\nExhausted all retries\nWill now terminate");
			System.exit(0);
		}
	
		
		MessageBuilderUDP myReceivedMessage = new MessageBuilderUDP();
		myReceivedMessage.decodeReceivedMesssage(receivePacket, myMessageBuilder);
		
		
		
		System.out.println("***Answer Section***");
		
		if(myReceivedMessage.getReplyClass()){
			System.out.println("Class is not 0x0001\n Will now terminate");
			System.exit(0);
		}
		
		if(myReceivedMessage.getErrorCode()) System.exit(0);
		
		for(String s: myReceivedMessage.getReplyRdata()){
			System.out.println(s);
		}
		clientSocket.close();

	}	


}
