package argumentProcessing;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class ArgumentBuffer {
	int argumentSize;
	HashMap <String,String> argumentsIn = new HashMap<String,String>(argumentSize);
	String arguments[] = new String[argumentSize];
	
	public ArgumentBuffer(String[] args){
		this.argumentSize=args.length;
		this.arguments=args;
	}

	public void fillArgumentBuffer(){
		for(int i = 0 ; i < argumentSize ; i++){
			String key=arguments[i];
			String value=arguments[i+1];
					
			if(key.contentEquals("-mx")||key.contentEquals("-ns")){
				argumentsIn.put("mx_ns",key);
				argumentsIn.put("IP", value);
				argumentsIn.put("name", arguments[i+2]);
				break;
			}
			if(key.contains("@")){
				argumentsIn.put("IP",key.substring(1,key.length()));
				argumentsIn.put("name",value);
				break;
			}
			if(key.contentEquals("-t") || key.contentEquals("-r")){
				argumentsIn.put(key, value);
			}
		}
	}
	
	public int getTimeout(){
		if(argumentsIn.get("-t")!=null) return Integer.parseInt(argumentsIn.get("-t"));
		else return 5;
	}
	public int getRetry(){
		if(argumentsIn.get("-r")!=null)	return Integer.parseInt(argumentsIn.get("-r"));
		else return 3;
	}
	public int getPort(){
		if(argumentsIn.get("-p")!=null) return Integer.parseInt(argumentsIn.get("-p"));
		else return 53;
	}
	public byte[] getType(){
		byte[] type = {(byte)0x00,(byte)0x01};
		if(argumentsIn.containsKey("mx_ns")){
			if(argumentsIn.get("mx_ns").contains("-mx")){
				type[0] = (byte)0x00;
				type[1]=(byte)0x0f;
			}
			else if(argumentsIn.get("mx_ns").contains("-ns")){
				type[0] = (byte)0x00;
				type[1] = (byte)0x02;
			}
		}
		
		return type;
	}
	public String getTypeString(){
		String s = "A";
		if(argumentsIn.get("mx_ns")==null){
			return s;
		}
		if(argumentsIn.get("mx_ns").contains("-mx")){
			s="MX";
		}
		if(argumentsIn.get("mx_ns").contains("-ns")){
			s="NS";
		}
		return s;
	}
	public byte[] getByteIP() throws UnsupportedEncodingException{
		char temp[]=argumentsIn.get("IP").toCharArray();
		int temp2[]=new int[4];
		byte temp3[]=new byte[4];
		for(int i=0;i<temp.length;i+=2){
			temp2[i/2]=Character.getNumericValue(temp[i]);
			temp3[i/2]=(byte)temp2[i/2];
		}
		return temp3;
	}
	public String getStringIP(){
		return argumentsIn.get("IP");
	}
	public String getName(){
		return argumentsIn.get("name");
	}

}
