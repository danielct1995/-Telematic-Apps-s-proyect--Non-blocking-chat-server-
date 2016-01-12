

import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Vector;

public class Cliente {
	private int id;
	private SocketChannel sc;
	
	private  LinkedList <byte[]> pending;
	
	
	public SocketChannel getSc() {
		return sc;
	}

	public void setSc(SocketChannel sc) {
		this.sc = sc;
	}

	protected Cliente(SocketChannel sc){
		this.sc = sc; 
		this.pending= new LinkedList();
	}
	
	protected void setSC(SocketChannel sc) {
		this.sc = sc;
		
	}
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	protected int getFirstMsgSize(){
			return this.pending.getFirst().length;
	}
	protected byte[] getPendingMsg() {
		return this.pending.poll();
	}

	protected void addPending(byte pending[]){			
		this.pending.add(pending);	
	}
	/**<Name>delPending():</Name>
	 * Elimina del vector pendiente un elemento en la posici�n determinada;
	 * */
	protected void delAllPending(){
		this.pending.clear();
	}
	
	protected boolean isPendingEmpty(){
		return this.pending.isEmpty();		
	}
	
}
