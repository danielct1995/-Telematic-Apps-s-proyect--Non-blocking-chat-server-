import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
/**Esta Clase tiene como objetivo extender la funcionalidad de Vector para adaptarla a las necesidades espec�ficas
 * de manejo de clientes
 * */
public class VClients extends LinkedList <Cliente> {

	protected VClients(){
		super();
	}
	
	/**Retorna el menor ID de cliente disponible
	 * */
	protected boolean IDExist(int id){
		boolean res= false;
		if(!super.isEmpty()){
			Cliente aux;
			Iterator<Cliente> it =super.iterator();
			while(it.hasNext()){
				aux=it.next();
				if(aux.getId()==id){res=true;break;}
			}	
		}
		
		return res;
	}
	/**Este m�todo devuelve el cliente que corresponda con el id requerido
	 * Si el id es 0 retorna nulo porque los clientes con id 0 no han completado el registro
	 * */
	protected Cliente indexId(int id){
		Cliente cl= null;
		if(!super.isEmpty()){	
			Iterator<Cliente> it =super.iterator();
			while(it.hasNext()){
				cl=it.next();
				if(cl.getId()==id){break;}
			}	
		}
		
		return cl;
	}

	
	
}
