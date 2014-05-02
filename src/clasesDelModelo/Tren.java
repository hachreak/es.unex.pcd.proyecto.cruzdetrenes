package clasesDelModelo;

import java.util.Random;

/**
 * Cada instancia de esta clase es un hilo que representa un tren.
 */
public class Tren extends Thread {
	
	public enum TipoTren {MERCANCIAS, PASAJEROS};
	
	private int idTren;
	private int numEstacion;
	private int numViaEstacion;
	private TipoTren tipoTren;
	
	private Thread hiloTren;
	
	public Tren(int idTren, int numEstacion, int numViaEstacion, TipoTren tipoTren) {
		this.idTren = idTren;
		this.numEstacion = numEstacion;
		this.numViaEstacion = numViaEstacion;
		this.tipoTren = tipoTren;
		this.hiloTren = this;
		this.hiloTren.start();
	}
	
	public TipoTren getTipoTren() {
		return tipoTren;
	}
	
	public int getIdTren() {
		return idTren;
	}
	
	public int getNumEstacion() {
		return numEstacion;
	}
	
	public int getNumViaEstacion() {
		return numViaEstacion;
	}
	
	public void entrarEnVia() {
		
		// Encendemos el semaforo correspondiente, dado que el tren ya puede salir
		Controlador.getSemaforos()[numEstacion][numViaEstacion].encenderSemaforo();
		
		Controlador.imprimir("El tren " + idTren + " esta entrando a la via " + (numEstacion+1) + "...");
		
		// Retardo de lo que tarda el tren en salir de la estacion a la via
		Controlador.retardoVisualizacionAmigable();
		
		// Apagamos ya el semaforo, puesto que el tren ya ha llegado a la via
		Controlador.getSemaforos()[numEstacion][numViaEstacion].apagarSemaforo();
		
		// Retardo de seguridad (no es realmente necesario, pero facilita y mejora la visualizacion)
		Controlador.retardoVisualizacionAmigable();	
		
		System.out.println("El tren " + idTren + " ha terminado de entrar a la via " + (numEstacion+1));
		
	}

	public void cruzarVia() throws InterruptedException {
		
		System.out.println("Ahora el tren " + getIdTren() + " circula por la via " + (getNumEstacion()+1));
		
		try {
			
			// El tren ya esta en la via, y tarda aproximadamente 3600 ms en atravesarla (200 bucles con 18ms de espera en cada uno)
			for(int i=0; i<200; i++) {
				Thread.sleep(18);
				
				// Movemos el tren 5 pixeles a cada 100ms para la simulacion grafica
				if(numEstacion == 0) {
					Controlador.getVentana().moverTren(this, 1, 0);
				} else {
					Controlador.getVentana().moverTren(this, 0, -1);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// El tren ha terminado de cruzar la via y activa el sensor...
		Controlador.getSensoresVia()[numEstacion].activarSensor();
	}
	
	@Override
	public void run() {
		if(hiloTren == currentThread()) {
			
			// El tren, tras un tiempo a priori desconocido (cuasialeatorio -entre 2000 y 7000 ms-), pide acceso a la via
			try {
				int retardo = 2000 + (new Random()).nextInt(5001);
				sleep(retardo);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				Controlador.getControlador().accederALaVia();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String toString() {
		return "Tren " + idTren + " " + ((tipoTren == TipoTren.MERCANCIAS) ? "(M)" : "(P)");
	}
	
}
