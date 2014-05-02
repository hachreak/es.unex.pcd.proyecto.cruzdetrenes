package clasesDelModelo;

/**
 * Esta clase modela un sensor que es activado por el paso de un tren.
 */
public class Sensor  {
	
	private int numVia;
	
	public Sensor(int numVia) {
		this.numVia = numVia;
	}
	
	public void activarSensor() throws InterruptedException {
		// Si el hilo efectivamente es de tipo tren, procedemos con las acciones del metodo
		Tren tren = Controlador.hiloTren();
		if(tren != null) {
				
			// Usamos el semaforo para sincronizar el acceso a la variable compartida "numTrenesPasando"...
			Controlador.getMutexVariablesCompartidas().acquire();
			
			Controlador.getNumTrenesPasando()[numVia]--;	
			
			// Soltamos la exclusion mutua del semaforo porque ya hemos accedido a la variable compartida...
			Controlador.getMutexVariablesCompartidas().release();
			
			// El sensor se marca como activo
			Controlador.getVentana().activarSensor(tren);
			Controlador.imprimir("El tren " + tren.getIdTren() + " esta abandonando la via " + (numVia+1) + "... (detectado por sensor " + (numVia+1) + ")");
			
			// Tras un tiempo dado, el sensor se desactiva
			Controlador.retardoVisualizacionAmigable();
			Controlador.getVentana().desactivarSensor(tren);
			System.out.println("El tren " + tren.getIdTren() + " ha abandonado la via " + (numVia+1));
			
			// Dado que un tren acaba de dejar su via, tenemos que despertar a todos los trenes bloqueados (pues las condiciones han cambiado).
			synchronized(Controlador.getControlador()) {
				Controlador.getControlador().notifyAll();
			}
		}
	}
}
