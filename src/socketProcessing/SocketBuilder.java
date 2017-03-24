package socketProcessing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SocketBuilder {
		
		DatagramSocket clientSocket;
		DatagramPacket receivePacket;
		DatagramPacket sendPacket;
		int messageSize;
		byte [] receiveData = new byte[1024];
		byte [] sendData = new byte[messageSize];
		
		public SocketBuilder() throws Exception{
			//Create a socket object
			this.clientSocket = new DatagramSocket();

		}
		
		public void DatagramBuilder(InetAddress ipAddress, int port,byte [] dataToSend){
			this.messageSize=dataToSend.length;
			this.sendData=dataToSend;
			this.sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
			this.receivePacket = new DatagramPacket(receiveData, receiveData.length);
		}
		
		public void closeSocket(){
			// Close the socket
			this.clientSocket.close();
		}
		
		public void receivePacket(){
			try {
				this.clientSocket.receive(this.receivePacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Failed at receive");
			}
		}
		
		public void sendPacket() {
			try {
				this.clientSocket.send(sendPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Failed at send");
			}
		}
		
		public DatagramPacket getReceivePacket(){
			return receivePacket;
		}
		
		public DatagramPacket getSendPacket(){
			return sendPacket;
		}

}
