
public class SendResponse extends Message {

	protected SendResponse(int msgld,int dest,boolean positive) {
		super(7,(short)3,msgld,dest,0,null);
		byte pay1[]= new byte[]{'s','u','c','c','e','s','s'};
		byte pay2[]= new byte[]{'f','a','i','l','u','r','e'}; 
		if(positive==true){
			super.setPayload(pay1);
		}
		else{super.setPayload(pay2);}
		
	}

}

