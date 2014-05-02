package auxiliares;
import java.util.Random;

import clasesDelModelo.Controlador;
import clasesDelModelo.Tren;

/**
 * Esta clase contiene el metodo main(), en el cual se configura y lanza la simulacion.
 */

public class Principal {

	private static final int NUM_MAX_TRENES_POR_ESTACION = 20;
	private static final int RETARDO_DE_VISUALIZACION = 1200;
	
	public static void main(String[] args) {
		int idTren = 0;
		Random aleatorios = new Random();
		new Controlador(RETARDO_DE_VISUALIZACION);
		Ventana ventana = Controlador.getVentana();		
		Thread hiloVentana = new Thread(ventana);
		
		// Creacion y colocacion aleatoria de los trenes
		for(int estacion = 0; estacion < 2; estacion++) {
			int numTrenesEstacion = aleatorios.nextInt(NUM_MAX_TRENES_POR_ESTACION);
			for(int numTren = 0; numTren < numTrenesEstacion; numTren++) {
				int numViaEstacion = aleatorios.nextInt(3);
				Tren.TipoTren tipoTren;
				if(aleatorios.nextInt(2) == 0) {
					tipoTren = Tren.TipoTren.MERCANCIAS;
				} else {
					tipoTren = Tren.TipoTren.PASAJEROS;
				}
				ventana.insertarTrenEnEstacion(new Tren(++idTren, estacion, numViaEstacion, tipoTren));
			}
		}
		
		// Iniciamos el hilo correspondiente a la interfaz grafica para que se genere la ventana
		hiloVentana.start();
		
	}
	
}
