import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Vector;


public class Server {
	private Selector select;
	private ServerSocketChannel pSock;
	private IOThread io;
	
	
	private Server() {
		// Hilo principal inicializa todo
		/*
		 * Crea un socket pasivo y los hilos de recepci�n y escritura lectura
		 */
		
		try {
			this.select = SelectorProvider.provider().openSelector();
			// Establecemos el socket pasivo
			pSock = ServerSocketChannel.open();
			SocketAddress psA = new InetSocketAddress(8888);
			pSock.bind(psA, 10);
			
			System.out.println("Escuchando puerto 8888: ");
			//Abrimos el hilo de entrada salida
			io = new IOThread(this.select);
			this.io.start();
			// Creamos el bucle de conexiones
			SocketChannel aSock;
			
			while (true) {
				
				aSock = pSock.accept();
			
				aSock.configureBlocking(false);
				InetSocketAddress aSockAddr = (InetSocketAddress) aSock.getRemoteAddress();				
				System.out.println("Cliente de: " + aSockAddr.getAddress().getHostAddress() + " : "
						+ aSockAddr.getPort() + " - conectando");
				
				Cliente cl = new Cliente(aSock);

							
				aSock.register(select, SelectionKey.OP_READ |SelectionKey.OP_WRITE, cl);
				
				if(io.isInterrupted()){
					io.notify();
				}
			
				
				
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void main(String args[]){
			Server a=new Server();
		}
	
	
	
	
}