import java.io.IOException;
import java.nio.*;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

public class IOThread extends Thread {
		Selector select;
		private VClients conectados;
		private ByteBuffer BRead;
		private ByteBuffer BWrite;
		private int reg;
	
	

	protected IOThread(Selector sel){
		super();
		this.select=sel;
		this.BRead=ByteBuffer.allocate(1024);
		this.BWrite=ByteBuffer.allocate(1024);
		this.conectados= new VClients();
		this.reg=0;
	
	}

	public void run() {
		System.out.println("Hilo I/O de sockets en funcionamiento ...\n");

		while (true) {
			
			try {
				//Select no bloqueante pero con sistema que evita hacer intentos de escritura o lectura
				//synchronized (Server.class) {
				this.reg=this.select.selectNow();
				//}
			} catch (IOException excpt) {
				break;
			}

			// Usamos un iterador para cambiar el selector
			if(this.reg!=0){
			Iterator<SelectionKey> it = select.selectedKeys().iterator();
			SelectionKey sk;
			while (it.hasNext()) {
				sk = (SelectionKey) it.next();
				it.remove();
				select.selectedKeys().remove(sk);
				SocketChannel sChannel = (SocketChannel) sk.channel();
				Cliente conn = (Cliente) sk.attachment();
				
				if (sk.isValid() && sk.isReadable()) {
					/* Read operation */
					

				//	sk.interestOps(SelectionKey.OP_READ);
					

						try {
							int numBytesRead = sChannel.read(BRead);
							if (numBytesRead == -1) {
								System.out.println("Conexi�n cerrada por el cliente: " + sChannel.getRemoteAddress()
										+ " ID -> " + conn.getId());
								sk.cancel();
								conn.getSc();
								sChannel.close();
								conectados.remove(conn);

							} else {

								LinkedList<Message> aux = Message.ExtractMessages(this.BRead);
								while(!aux.isEmpty()){
									this.sendAction(conn, aux.poll());}
								
								aux = null;
							}

						} catch (IOException e) {
							try {

								System.out.println("Closing...\n");
								sk.cancel();
								sChannel.close();
								conectados.remove(conn);

							} catch (IOException e1) {

								System.err.println(e1.getMessage());
							}
						}

					}
								
					/*Comprobamos si el cliente tiene algo que escribir y en ese caso escribe */
						BWrite.clear();
							while (!conn.isPendingEmpty()) {					
								// write NUMS, OK, KO
								BWrite.put(conn.getPendingMsg());
								BWrite.flip();
								try {
									sChannel.write(BWrite);
									
									
								} catch (IOException e) {

									try {
										System.out.println(
												"Unable to send message due to " + e.getMessage() + ".Closing...\n");
										sk.cancel();
										sChannel.close();
										conectados.remove(conn);
									} catch (IOException e1) {

										System.out.println(e1.getMessage());
									}
								
								}
								BWrite.clear();
							}
							// Borramos todos los pendientes ya
							// que se han enviado
						
					}
				

			
			it = null; /* force gc */
		
			}
			else{
				try {
					IOThread.sleep(5);//Reducimos la continuidad del bucle
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		}
}
	
	private void sendAction(Cliente cl, Message input) {

		Message output = null;


		switch (input.getCmd()) {
		case 0: {
			if (this.LogInConditions(input)) {
				this.conectados.add(cl);
				output = new LogMessage((short) 1, input.getMsgld(), input.getOrg_id(), true);
				cl.addPending(output.ToByteArray());
				cl.setId(input.getOrg_id());
				
				System.out.println("Recibida peticion de LogIn, respuesta exitosa -> ID: "+cl.getId());

			} else {
				//System.out.println(input.toString());
				output = new LogMessage((short) 1, input.getMsgld(), input.getOrg_id(), false);
				cl.addPending(output.ToByteArray());
				//System.out.println("Enviado a: " + cl.getId());
				//System.out.println(output.toString());
				System.out.println("Recibida peticion de LogIn, respuesta fallida -> ID: "+cl.getId());
			}
			//sk.interestOps(SelectionKey.OP_READ|SelectionKey.OP_WRITE);
			break;
		}
		case 2: {
			if (this.sendConditions(input, cl)) {
				// Encolamos el mensaje en el cliente de destino
				Cliente recep=this.conectados.indexId(input.getDest_id());
				recep.addPending(input.ToByteArray());
		
				System.out.println("Enviado mensaje cruzado: "+input.getOrg_id()+" -> "+input.getDest_id());
				output = new SendResponse(input.getMsgld(), input.getOrg_id(), true);
				// Enviamos el mensaje de exito
				cl.addPending(output.ToByteArray());
				System.out.println("Recibido mensaje, respuesta exitosa -> ID: "+cl.getId());
			} else {
				output = new SendResponse(input.getMsgld(), input.getOrg_id(), false);
				// Enviamos el mensaje de fracaso
				cl.addPending(output.ToByteArray());
				System.out.println("Recibido mensaje, respuesta fallida -> ID: "+output.getDest_id());
			}
			//sk.interestOps(SelectionKey.OP_READ|SelectionKey.OP_WRITE);
			break;
		}

		case 4: {
			if (this.LogOutConditions(input, cl)) {
				this.conectados.remove(cl);//Eliminamos del vector el conectado
				output = new LogMessage((short) 5, input.getMsgld(), input.getOrg_id(), true);
				cl.addPending(output.ToByteArray());
				//System.out.println("Enviando a: " + cl.getId());
				//System.out.println(output.toString());
				System.out.println("Recibido mensaje de LogOut, respuesta exitosa -> ID: "+cl.getId());

			} else {
				output = new LogMessage((short) 5, input.getMsgld(), input.getOrg_id(), false);
				cl.addPending(output.ToByteArray());
				//System.out.println("Enviado a: " + cl.getId());
				//System.out.println(output.toString());
				System.out.println("Recibido mensaje de LogOut, respuesta fallida -> ID: "+cl.getId());

			}
			//sk.interestOps(SelectionKey.OP_READ|SelectionKey.OP_WRITE);
			break;
		}
		default: System.out.println("------------------------FALLO DE CLASIFICACIÓN--------------------------------------");break;
		}
		

	}

	private boolean LogInConditions(Message m) {
		// boolean res = true;
		/*
		 * if (this.conectados.IDExist(m.getOrg_id())) { res = false; }
		 * if(!this.testInputId(m, 0)){ res=false; }
		 * if(!m.testPayloadFormat()){res=false;}
		 */
		/*return !(this.conectados.IDExist(m.getOrg_id())) && (this.testDestId(m, 0)) && (m.testLogInPayload())
				&& m.testProtocol();*/
		return (!this.conectados.IDExist(m.getOrg_id())) &&m.testLogInPayload() &&(this.testDestId(m, 0))&&this.testOrgId(m);
	}

	private boolean LogOutConditions(Message m, Cliente cl) {
		/*
		 * Para que el logout de Succes tiene que darse: 1-Que el Cliente se
		 * encuentre logeado 2-Que id de destino sea 0 3-Que el formato del
		 * payload sea correcto
		 */
		return this.testDestId(m, 0) && this.testSocketIDRelation(m, cl)&&this.testOrgId(m);

	}

	private boolean testDestId(Message m, int ctr) {
		boolean res = true;
		int id = 0;
		if (ctr == 0) {// Caso Log in/Log Out
			if (m.getDest_id() != 0) {
				res = false;
			}
		}
		if (ctr == 2) {
			id = m.getDest_id();
			if (id == 0||id>1000) {
				res = false;
			} else {
				res = this.conectados.IDExist(id);
			}

		}
		return res;
	}
	private boolean testOrgId(Message m){
		boolean res=true;
		int id=m.getOrg_id();
		if(id<=0||id>1000){
			res =false;
		}
		return res;
	}

	/**
	 * Comprueba que el id del cliente y el que anuncia en el mensaje
	 */
	private boolean testSocketIDRelation(Message m, Cliente cl) {
		boolean res = true;
		int id = m.getOrg_id();
		if ((id != cl.getId()) || (cl.getId() == 0)||(id==0)) {
			res = false;
		}
		// Comprobamos que el mensaje no tiene id origen 0 o no corresponde con
		// el guardado o o esta registrado el cliente

		return res;

	}

	private boolean sendConditions(Message m, Cliente cl) {

		return this.testSocketIDRelation(m, cl) && this.testDestId(m, 2)&&this.testOrgId(m);
	}
		
}
