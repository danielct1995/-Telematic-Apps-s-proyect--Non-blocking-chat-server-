import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Vector;

public class Message {
	public static final int PROTO = 0x11223344;

	private int proto;
	private int length;
	private short cmd;
	private int msgld;
	private int dest_id;
	private int org_id;
	private byte[] payload;

	protected Message(int proto, int length, short cmd, int msgld, int dest, int org, byte[] payload) {
		this.proto = proto;
		this.length = length;
		this.cmd = cmd;
		this.msgld = msgld;
		this.dest_id = dest;
		this.org_id = org;
		this.payload = payload;

	}

	protected Message(int length, short cmd, int msgld, int dest, int org, byte[] payload) {
		this.proto = this.PROTO;
		this.length = length;
		this.cmd = cmd;
		this.msgld = msgld;
		this.dest_id = dest;
		this.org_id = org;
		this.payload = payload;

	}

	
	protected static LinkedList <Message>  ExtractMessages(ByteBuffer a)
	{	ByteBuffer aux =a.duplicate();
		boolean ctr=false;
		aux.flip();
		int leido = 0;
		int pr;
		int orrem=aux.remaining();
		Message m=null;
		 LinkedList <Message> msgs=new <Message>LinkedList();
		while (aux.remaining() >=8) {
			pr = aux.getInt();
			leido = leido + 4;
			if (pr == Message.PROTO) {
				aux.position(leido - 4);
				leido=leido-4;
					m = Message.parseMessage(aux);
				if (m != null) {
					msgs.add(m);
					leido=leido+22+m.getLength();
				}
				else{
					break;
				}
				
			}
		}
		if(orrem<8){
			m=null;
			ctr=true;
		}
		if (m != null) {
			a.clear();
			leido=0;
		}
		else{
			if(ctr==false){
				aux.position(leido-8);
				leido= leido-8;
				ctr=false;
			}
			aux = aux.compact();
			a=aux;
			
			}
		
		return msgs;
	}
		
	

	
	protected static Message parseMessage(ByteBuffer buf) {
		
		;

		try {
			int proto = buf.getInt();// Desechamos el protocolo pero igual es
										// necesario
			int l = buf.getInt();
			if (buf.remaining() >= (l + 14)) {
				short c = buf.getShort();
				int m = buf.getInt();
				int d = buf.getInt();
				int o = buf.getInt();
				byte p[] = new byte[l];
				buf.get(p);
				
				return new Message(proto, l, c, m, d, o, p);
				
			}
			else{
				return null;
			}
			
			
			
			
			
		} catch (Exception ex) {

			ex.printStackTrace();
			return null;
		}
	}

	protected byte[] ToByteArray() {
		ByteBuffer res = ByteBuffer.allocate(279);
		byte result[] = new byte[this.length + 22];
		res.putInt(this.proto);
		res.putInt(this.length);
		res.putShort(this.cmd);
		res.putInt(this.msgld);
		res.putInt(this.dest_id);
		res.putInt(this.org_id);
		res.put(this.payload);
		res.flip();
		res.get(result);

		return result;
	}

	protected int getLength() {
		return length;
	}

	protected void setLength(int length) {
		this.length = length;
	}

	protected short getCmd() {
		return cmd;
	}

	protected void setCmd(short cmd) {
		this.cmd = cmd;
	}

	protected int getMsgld() {
		return msgld;
	}

	protected void setMsgld(int msgld) {
		this.msgld = msgld;
	}

	protected int getDest_id() {
		return dest_id;
	}

	protected void setDest_id(int dest_id) {
		this.dest_id = dest_id;
	}

	protected int getOrg_id() {
		return org_id;
	}

	protected void setOrg_id(int org_id) {
		this.org_id = org_id;
	}

	protected byte[] getPayload() {
		return payload;
	}

	protected void setPayload(byte[] payload) {
		this.payload = payload;
	}

	/**
	 * Este m�todo comprueba que se cumple el formato del payload para los casos
	 * de log in/ Logout
	 */
	protected boolean testLogInPayload() {
		boolean res = true;
		StringBuffer cbuf=new StringBuffer();
		cbuf=cbuf.append("pass");
		ByteBuffer aux = ByteBuffer.allocate(276);
		int i=3;
		int div=this.org_id;
		while(div!=0){
			i--;
			div=div/100;
		}
		for(int b=0;b<=i;b++){
			cbuf=cbuf.append('0');
			
		}
		cbuf.append(String.valueOf(this.org_id));

		aux.putInt(this.org_id);
		aux.put(cbuf.toString().getBytes());
		aux.flip();
		byte aux3[] = new byte[this.length];
		// Convertimos el n�mero en ASCII
		
		// Comparamos ambos resultados
		for (int c = 0; c < this.length; c++) {
			if (this.payload[i] != aux3[i]) {
				res = false;
				break;
			}
		}

		return res;
	}

	protected boolean testLogOutPayload() {

		return (this.length == 0) && (this.payload.length == 0);
	}

	
	@Override
	public String toString() {
		String cadena = new String(this.payload, StandardCharsets.US_ASCII);
		if (this.cmd == 0) {
			cadena = cadena.substring(4);
		}

		StringBuffer buf = new StringBuffer();
		buf.append("PROTO " + Integer.toString(this.PROTO) + ", length=" + length + ", cmd=" + cmd + ", msgld="
				+ Integer.toHexString(msgld) + ", dest_id=" + dest_id + ", org_id=" + org_id + "\n Payload: " + cadena
				+ "]");
		return buf.toString();

	}

}
