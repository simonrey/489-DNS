package messageUDP;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.*;

import argumentProcessing.ArgumentBuffer;

public class MessageBuilderUDP {

	int messageSize;
	byte[] message = new byte[1];
	
	
	int headerSize;
	byte[] header = new byte[1];
	byte[] randomID = new byte[2];
	byte QR_OPCODE_AA_TC_RD;
	byte RA_Z_RCODE;
	byte[] QDCOUNT = new byte[2];
	byte[] ANCOUNT = new byte[2];
	byte[] NSCOUNT = new byte[2];
	byte[] ARCOUNT = new byte[2];
	
	int questionSize;
	byte[] question=new byte[1];
	byte[] QNAME = new byte[1];
	byte[] QTYPE = new byte[2];
	byte[] QCLASS = {(byte)0x00,(byte)0x01};
	
	int answerSize;
	byte[] answer=new byte[1];
	byte[] NAME=new byte[2];
	byte[] TYPE = new byte[2];
	byte[] CLASS = new byte[2];
	byte[] TTL = new byte[4];
	byte[] RDLENGTH = new byte[2];
	byte[] RDATA;
	
	
	public MessageBuilderUDP(){
		
	}
	
	public byte[] buildSendMessage(ArgumentBuffer buffer){
		
		this.buildSendMessageHeader();
		this.buildSendMessageQuestion(buffer);
		
		return this.message;
		
	}
	
	public void buildSendMessageHeader(){
		
		new Random().nextBytes(randomID);
		QR_OPCODE_AA_TC_RD=(byte) (QR_OPCODE_AA_TC_RD|1);
		QDCOUNT[1]=(byte)(QDCOUNT[0]|1);
		
		this.headerSize=12;
		this.header=Arrays.copyOf(this.header,this.headerSize);
		
		this.header[0]=this.randomID[0];
		this.header[1]=this.randomID[1];
		this.header[2]=this.QR_OPCODE_AA_TC_RD;
		this.header[3]=this.RA_Z_RCODE;
		this.header[4]=this.QDCOUNT[0];
		this.header[5]=this.QDCOUNT[1];
		this.header[6]=this.ANCOUNT[0];
		this.header[7]=this.ANCOUNT[1];
		this.header[8]=this.NSCOUNT[0];
		this.header[9]=this.NSCOUNT[1];
		this.header[10]=this.ARCOUNT[0];
		this.header[11]=this.ARCOUNT[1];
		
		int byteCounter=0;
		message=Arrays.copyOf(message, this.header.length);
		for(byte i : this.header){
			message[byteCounter]=i;
			byteCounter++;
		}
	}
	
	public void buildSendMessageQuestion(ArgumentBuffer buffer){
		int periodIndex[] = new int[1];
		
		periodIndex[0]=buffer.getName().indexOf('.');
		this.QNAME[0]=(byte) periodIndex[0];
		for(byte i:buffer.getName().substring(0,periodIndex[0]).getBytes()){
			this.QNAME=Arrays.copyOf(this.QNAME, this.QNAME.length+1);
			this.QNAME[this.QNAME.length-1]=i;
		}
		
		while(buffer.getName().indexOf('.', (periodIndex[periodIndex.length-1]+1))>0){
			periodIndex=Arrays.copyOf(periodIndex,periodIndex.length+1);
			this.QNAME=Arrays.copyOf(this.QNAME, this.QNAME.length+1);
			
			periodIndex[periodIndex.length-1]=buffer.getName().indexOf('.',(periodIndex[periodIndex.length-2]+1));
			this.QNAME[this.QNAME.length-1]=(byte)(periodIndex[periodIndex.length-1]-periodIndex[periodIndex.length-2]-1);
			
			for(byte i: buffer.getName().substring(periodIndex[periodIndex.length-2]+1,periodIndex[periodIndex.length-1]).getBytes()){
				this.QNAME=Arrays.copyOf(this.QNAME, this.QNAME.length+1);
				this.QNAME[this.QNAME.length-1]=i;
			}
		}	

		this.QNAME=Arrays.copyOf(this.QNAME, this.QNAME.length+1);
		this.QNAME[this.QNAME.length-1]=(byte) (buffer.getName().length()-periodIndex[periodIndex.length-1]-1);
		for(byte i : buffer.getName().substring(periodIndex[periodIndex.length-1]+1,buffer.getName().length()).getBytes()){
			this.QNAME=Arrays.copyOf(this.QNAME, this.QNAME.length+1);
			this.QNAME[this.QNAME.length-1]=i;
		}
		
		this.QNAME = Arrays.copyOf(this.QNAME, this.QNAME.length+1);
		
		this.QTYPE = buffer.getType();
				
		int byteCounter=this.message.length;
		this.message = Arrays.copyOf(this.message, (this.QNAME.length+this.QTYPE.length+QCLASS.length+this.message.length));
		this.question=Arrays.copyOf(this.question, this.message.length-this.header.length);
		for(byte i : this.QNAME){
			this.message[byteCounter]=i;
			this.question[byteCounter-this.header.length]=i;
			byteCounter++;
		}
		for(byte i : this.QTYPE){
			this.message[byteCounter]=i;
			this.question[byteCounter-this.header.length]=i;
			byteCounter++;
		}
		for(byte i : this.QCLASS){
			this.message[byteCounter]=i;
			this.question[byteCounter-this.header.length]=i;
			byteCounter++;
		}
		this.questionSize=this.question.length;
	}	
	
	public void decodeReceivedMesssage(DatagramPacket receivedMessage, MessageBuilderUDP sentMessage){
		
		this.message=Arrays.copyOf(receivedMessage.getData(), receivedMessage.getLength());
		this.messageSize=message.length;
		
		this.headerSize=12;
		this.questionSize=sentMessage.questionSize;

		this.header=Arrays.copyOfRange(this.message, 0, this.headerSize);
		this.question=Arrays.copyOfRange(this.message, this.headerSize, this.headerSize+this.questionSize);
		this.answer=Arrays.copyOfRange(this.message, this.header.length+this.questionSize, receivedMessage.getLength());
		this.answerSize=this.answer.length;

	
		
		if(this.decodeReceivedMessageHeader(sentMessage)>0){
			System.out.println("Received message ID does not match sent message ID\nWill now terminate");
			System.exit(0);
		}
		this.decodeReceivedMessageQuestion();
		this.decodeReceivedMessageAnswer();
	}
	
	public int decodeReceivedMessageHeader(MessageBuilderUDP sentMessage){
		
		this.randomID[0]=this.header[0];
		this.randomID[1]=this.header[1];
		this.QR_OPCODE_AA_TC_RD=this.header[2];
		this.RA_Z_RCODE=this.header[3];
		this.QDCOUNT[0]=this.header[4];
		this.QDCOUNT[1]=this.header[5];
		this.ANCOUNT[0]=this.header[6];
		this.ANCOUNT[1]=this.header[7];
		this.NSCOUNT[0]=this.header[8];
		this.NSCOUNT[1]=this.header[9];
		this.ARCOUNT[0]=this.header[10];
		this.ARCOUNT[1]=this.header[11];
		
		if(this.randomID[0]==sentMessage.randomID[0] && this.randomID[1]==sentMessage.randomID[1]) return 0;
		else return 1;
		
	}
	
	public void decodeReceivedMessageQuestion(){
		this.QNAME=Arrays.copyOfRange(this.question, 0, this.questionSize-4);
		this.QTYPE=Arrays.copyOfRange(this.question, this.QNAME.length, this.QNAME.length+2);
		this.QCLASS=Arrays.copyOfRange(this.question, this.QTYPE.length+this.QNAME.length,this.question.length);
	}
	
	public void decodeReceivedMessageAnswer(){
		
		if(this.answer[0]==-64){
			this.NAME=Arrays.copyOf(this.NAME, 2);
			this.NAME[0]=this.answer[0];
			this.NAME[1]=this.answer[1];
			this.TYPE[0]=this.answer[2];
			this.TYPE[1]=this.answer[3];
			this.CLASS[0]=this.answer[4];
			this.CLASS[1]=this.answer[5];
			this.TTL[0]=this.answer[6];
			this.TTL[1]=this.answer[7];
			this.TTL[2]=this.answer[8];
			this.TTL[3]=this.answer[9];
			this.RDLENGTH[0]=this.answer[10];
			this.RDLENGTH[1]=this.answer[11];
			this.RDATA=Arrays.copyOfRange(answer, 12, answerSize);
		}
		else{
			for(byte i : this.answer){
				this.NAME=Arrays.copyOf(this.NAME, 1);
				this.NAME[this.NAME.length-1]=i;
				if(i==0){
					break;
				}
			}
			this.TYPE[0]=this.answer[2+this.NAME.length];
			this.TYPE[1]=this.answer[3+this.NAME.length];
			this.CLASS[0]=this.answer[4+this.NAME.length];
			this.CLASS[1]=this.answer[5+this.NAME.length];
			this.TTL[0]=this.answer[6+this.NAME.length];
			this.TTL[1]=this.answer[7+this.NAME.length];
			this.TTL[2]=this.answer[8+this.NAME.length];
			this.TTL[3]=this.answer[9+this.NAME.length];
			this.RDLENGTH[0]=this.answer[10+this.NAME.length];
			this.RDLENGTH[1]=this.answer[11+this.NAME.length];
			this.RDATA=Arrays.copyOfRange(answer, 12+this.NAME.length, answerSize);
			
		}
		
		
	}
	
	public String getReplyName(){
		StringBuilder myString = new StringBuilder();
		String s=null;
		if(NAME[0]==-64){
			int nameStart=NAME[1],count=message[nameStart];
			while(count>0){
				int i;
				for(i = 1; i<=count; i++){
					myString.append( (char)message[nameStart+i]);
				}
				myString.append(".");
				count=message[nameStart+i];
				nameStart=nameStart+i;
			}
			s=myString.subSequence(0, myString.length()-1).toString();
		}
		else{
			int nameStart=0,count=this.NAME[nameStart];
			while(count>0){
				int i;
				for(i = 1; i<=count; i++){
					myString.append((char)this.NAME[nameStart+i]);
				}
				myString.append(".");
				count=this.NAME[nameStart+i];
				nameStart=nameStart+i;
			}
			s=myString.subSequence(0, myString.length()-1).toString();
		}
		return s;
	}
	
	public boolean getErrorCode(){
		int f = this.RA_Z_RCODE & 0x0f;
		String s;
		boolean err=false;
		if(f==1){
			s="Format Error: the name server was unable to interpret the query\nWill now terminate";
			err=true;
			System.out.println(s);
			return err;
		}
		if(f==2){
			s="Server Failure: the name server was unable to process this query due to a problem with the name server\nWill now terminate";
			err=true;
			System.out.println(s);
			return err;
		}
		if(f==3){
			s="Name Error: the domain name referenced in the query does not exist\nWill now terminate";
			err=true;
			System.out.println(s);
			return err;
		}
		if(f==4){
			s="Not Implemented: the name server does not support the requested kind of query\nWill now terminate";
			err=true;
			System.out.println(s);
			return err;
		}
		if(f==5){
			s="Refused: the name server has refused to perform the requested operation\nWill now terminate";
			err=true;
			System.out.println(s);
			return err;
		}
		else{
			return err;
		}
	}
	
	public int getReplyType(){
		ByteBuffer buf = ByteBuffer.wrap(this.TYPE);
		int f = buf.getShort();
		return f;
	}
	
	public boolean getReplyClass(){
		ByteBuffer buf = ByteBuffer.wrap(this.CLASS);
		int f=buf.getShort();
		if(f==1){
			return false;
		}
		return true;
	}
	
	public int getReplyTTL(){
		ByteBuffer buf = ByteBuffer.wrap(this.TTL);
		int f=buf.getInt();
		return f;
	}
	
	public int getReplyRdLength(){
		ByteBuffer buf = ByteBuffer.wrap(this.RDLENGTH);
		int f=buf.getShort();
		return f;
	}
	
	public String getReplyAuth(){
		String s="UnAuth";
		if(((this.QR_OPCODE_AA_TC_RD & 0x04)>>2) == 1){
			s="Auth";
		}
		return s;
	}
	
	public int getRDATAlength(){
		int f=this.RDATA.length;
		return f;
	}
	
	public String[] getReplyRdata(){
		
		String s[] = new String[1];
		if(this.getReplyType()==1){
						
			int iterations = this.RDATA.length/this.getReplyRdLength();
			s=Arrays.copyOf(s, iterations);
			int i=0,j=0;
			while(i<iterations){
				StringBuilder sb = new StringBuilder();
				for(j=4*i;j<4*i+4;j++){
					sb.append((char)this.RDATA[j]&0xff);
					sb.append('.');
				}
				
				s[i]="IP\t"+sb.toString().substring(0, sb.length()-1)+"\t"+this.getReplyTTL()+"\t"+this.getReplyAuth();
				i++;
			}
		}
		
		if(this.getReplyType()==2){
			StringBuilder sb = new StringBuilder();

			int nameStart=0,count=this.RDATA[nameStart];
			int lastPoint=0;
			boolean pullFromMessage=false;
			while(count>0){
				int i;
				for(i = 1; i<=count; i++){
					if(this.RDATA[nameStart+i]==-64){
						lastPoint=nameStart+i;
						nameStart=this.RDATA[nameStart+i+1];
						count=message[nameStart];
						pullFromMessage=true;
					}
					sb.append(pullFromMessage?(char)message[nameStart+i]:(char)this.RDATA[nameStart+i]);
				}
				sb.append(".");
				nameStart=pullFromMessage?nameStart+i:lastPoint;
				count=pullFromMessage?message[nameStart+i]:this.RDATA[nameStart+i];
			}
			pullFromMessage=pullFromMessage?false:false;
			s[0]=sb.subSequence(0, sb.length()-1).toString();
		}
		
		
		if(this.getReplyType()==5){
			int count=0,index=0;
			int iterations = this.getRDATAlength()/this.getReplyRdLength();
			s=Arrays.copyOf(s, iterations);
			while(count<iterations){
				s[count]=nameParser(this.RDATA,index,this.RDATA[index]);
				index=s[count].length()+1;
				count++;
			}
			return s;
		}
		return s;
	}
	
	public String nameParser(byte[] data,int start,int stop){
		StringBuilder sb=new StringBuilder();
		int i;
		
		if(data[start]==-64){
			sb.append(nameParser(message,data[start+1],message[data[start+1]]+data[start+1]));
		}
		for(i=start+1;i<=stop;i++){
			sb.append((char)data[i]);
		}
		sb.append(".");

		if(data[i]==0){
			return sb.toString();
		}
		if(data[i]>0){
			sb.append(nameParser(data,i,i+data[i]));
			
		}
		return sb.toString();
		
		
		
		
	}
	 
	
}

