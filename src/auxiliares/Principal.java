package auxiliares;
import java.util.Random;

import clasesDelModelo.Controlador;
import clasesDelModelo.TipoTren;
import clasesDelModelo.Tren;

/**
 * Esta clase contiene el metodo main(), en el cual se configura y lanza la simulacion.
 */

public class Principal {

	private static final int NUM_MAX_TRENES_POR_ESTACION = 20;
//	
	
	public static void main(String[] args) {
		
		int idTren = 0;
		Random aleatorios = new Random();
		Ventana ventana = Controlador.getControlador().getVentana();		
		Thread hiloVentana = new Thread(ventana);
		
		// Creacion y colocacion aleatoria de los trenes
		for(int estacion = 0; estacion < Controlador.NUM_ESTACIONES; estacion++) {
			int numTrenesEstacion = aleatorios.nextInt(NUM_MAX_TRENES_POR_ESTACION);
			for(int numTren = 0; numTren < numTrenesEstacion; numTren++) {
				int numViaEstacion = aleatorios.nextInt(Controlador.NUM_VIA_CADA_ESTACION);
				Tren t = new Tren(++idTren, estacion, numViaEstacion, TipoTren.getRandom());
				t.start();
				ventana.insertarTrenEnEstacion(t);
			}
		}
		
		// Iniciamos el hilo correspondiente a la interfaz grafica para que se genere la ventana
		hiloVentana.start();
		
	}
	
}
