package clasesDelModelo;

/**
 * Esta clase modela un semaforo que puede encenderse o apagarse.
 */
public class Semaforo  {
	
	private int numVia, numEstacion;
	
	public Semaforo(int numEstacion, int numVia) {
		this.numEstacion = numEstacion;
		this.numVia = numVia;
	}
	
	public void encenderSemaforo() {
		Tren tren = null;
		
		// Hacemos casting para tener la instancia de tipo Tren
		if(Thread.currentThread() instanceof Tren) {
			tren = (Tren) Thread.currentThread();
		}
		
		// Si el hilo efectivamente es de tipo tren, procedemos con las acciones del metodo
		if(tren != null) {
			Controlador.getVentana().abrirSemaforo(tren);
			System.out.println("Se enciende el semaforo " + (numVia+1) + " de la estacion " + (numEstacion+1) + ", indicandole al tren " + tren.getIdTren() + " que puede abandonar la estacion y entrar a la via " + (numEstacion+1));
		}
	}
	
	public void apagarSemaforo() {
		Tren tren = null;
		
		// Hacemos casting para tener la instancia de tipo Tren
		if(Thread.currentThread() instanceof Tren) {
			tren = (Tren) Thread.currentThread();
		}
		
		// Si el hilo efectivamente es de tipo tren, procedemos con las acciones del metodo
		if(tren != null) {
			Controlador.getVentana().sacarTrenDeEstacion(tren);
			Controlador.getVentana().cerrarSemaforo(tren.getNumEstacion());
			System.out.println("Se apaga el semaforo " + (numVia+1) + " de la estacion " + (numEstacion+1) + " porque el tren " + tren.getIdTren() + " ya ha entrado en la via " + (numEstacion+1));
		}
	}
	
}
