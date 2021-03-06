package clasesDelModelo;
import java.util.concurrent.Semaphore;

import auxiliares.Ventana;

/**
 * Una instancia de esta clase gestiona el trafico de trenes de las dos estaciones.
 */
public class Controlador {
	
	/*
	 * De ahora en adelante, en el c�digo, la estacion de la izquierda (asi como su via asociada) 
	 * se identificara con el 0, y la estacion de la derecha, con el 1.
	 */
	
	private static final int NUM_TRENES_SEGUIDOS_MISMO_SENTIDO = 5;
		
	private static int[] numTrenesPasando;
	private static int[] numTrenesEsperando;
	private static int[] numTrenesDePasajerosEsperando;
	private static int numTrenesSeguidosMismoSentido;
	private static int sentido; // 0 para la via horizontal, 1 para la vertical, -1 cuando aun no ha sido definido
	
	private static Sensor[] sensoresVia;
	private static Semaforo[][] semaforos;
	
	private static Controlador controlador = null;
	private static Ventana ventana;
	
	/* Este objeto auxiliar lo usaremos para la sincronizacion de las variables compartidas en todo el programa */
	private static Semaphore mutexVariablesCompartidas;
	
	private static int retardoVisualizacion;
	
	public Controlador(int retardo) {
		if(controlador == null) {
			controlador = this;
			numTrenesPasando = new int[2];
			numTrenesEsperando = new int[2];
			numTrenesDePasajerosEsperando = new int[2];
			for(int i=0; i<2; i++) {
				numTrenesPasando[i] = 0;
				numTrenesEsperando[i] = 0;
				numTrenesDePasajerosEsperando[i] = 0;
			}
			numTrenesSeguidosMismoSentido = 0;
			sentido = -1;
			sensoresVia = new Sensor[2];
			sensoresVia[0] = new Sensor(0);
			sensoresVia[1] = new Sensor(1);
			semaforos = new Semaforo[2][3];
			for(int i=0; i<2; i++)
				for(int j=0; j<3; j++)
					semaforos[i][j] = new Semaforo(i, j);
			ventana = new Ventana();
			mutexVariablesCompartidas = new Semaphore(1);
			retardoVisualizacion = retardo;
		}
	}

	public void accederALaVia() throws InterruptedException {
		
		// Si el hilo efectivamente es de tipo tren, procedemos con las acciones del metodo
		Tren tren = hiloTren();
		if(tren != null) {
			
			// Usamos el semaforo para sincronizar el acceso a las variables compartidas...
			mutexVariablesCompartidas.acquire();
			
			System.out.println("[ El tren " + tren.getIdTren() + ", que esta ubicado en la estacion " + (tren.getNumEstacion()+1) + " y es de " + ((tren.getTipoTren() == Tren.TipoTren.MERCANCIAS) ? "mercancias" : "pasajeros") + ", quiere acceder a la via " + (tren.getNumEstacion()+1) + " ]");			
			ventana.addTrenSolicitante(tren);
			
			// Este tren esta esperando a salir de su correspondiente estacion...
			numTrenesEsperando[tren.getNumEstacion()]++;
			
			// Si el tren que quiere pasar es de tipo pasajeros, se indica...
			if(tren.getTipoTren() == Tren.TipoTren.PASAJEROS) {
				numTrenesDePasajerosEsperando[tren.getNumEstacion()]++;
			}
			
			// Soltamos la exclusion mutua del semaforo porque ya hemos accedido a las variables compartidas...
			mutexVariablesCompartidas.release();
			
			// Dos trenes no pueden entrar a la vez, as� que protegemos la acci�n de entrada
			synchronized(this) {
				
				// El tren pide acceso, y se bloqueara (o no) en funcion de las condiciones
				peticionDeEntrada(tren);
				
				// Se anota que el tren esta dejando la estacion...
				numTrenesEsperando[tren.getNumEstacion()]--;
				
				// Si el tren que esta entrando en la via es de tipo pasajeros, se deja constancia...
				int numTrenesPasajerosRestantes = 9999;
				if(tren.getTipoTren() == Tren.TipoTren.PASAJEROS) {
					numTrenesPasajerosRestantes = numTrenesDePasajerosEsperando[tren.getNumEstacion()]--;
				}
				
				// Si ya no quedan trenes de pasajeros en la estacion debido a la salida de este tren, desbloqueamos a los trenes bloqueados, pues las condiciones han cambiado
				if(numTrenesPasajerosRestantes == 0) {
					notifyAll();
				}
				
				// Llamamos al algoritmo de asignacion de turnos
				asignarTurno(tren);
				
				// Anotamos el incremento de ocupacion de la via correspondiente al tren entrante a la via...
				numTrenesPasando[tren.getNumEstacion()]++;
				
				// Soltamos la exclusion mutua del semaforo porque ya hemos accedido a las variables compartidas...
				mutexVariablesCompartidas.release();
				
				// El hilo (tren) procede a realizar la accion de salir de la estacion entrando en la via				
				tren.entrarEnVia();	
				
			}
		
			// El hilo (tren) procede a cruzar la via
			tren.cruzarVia();
		}
	}
	
	private void peticionDeEntrada(Tren tren) throws InterruptedException {
		
		// Usamos el semaforo para sincronizar el acceso a las variables compartidas...
		mutexVariablesCompartidas.acquire();
		
		/*
		 * MOTIVOS DE BLOQUEO (condiciones del bucle while, en orden):
		 * - La via contraria esta siendo usada
		 * - El tren es de mercancias y esta en una estacion en la que hay trenes de pasajeros esperando para salir
		 * - El turno le corresponde a los trenes de la otra estacion y, ademas, en dicha estacion hay trenes esperando para salir
		 */
		boolean primerBloqueo = true;
		while(numTrenesPasando[(1-tren.getNumEstacion())] != 0 || (tren.getTipoTren() == Tren.TipoTren.MERCANCIAS && numTrenesDePasajerosEsperando[tren.getNumEstacion()] > 0) || (sentido == (1-tren.getNumEstacion())) && numTrenesEsperando[(1-tren.getNumEstacion())] > 0) {
			// Soltamos la exclusion mutua del semaforo porque ya hemos accedido a las variables compartidas...
			mutexVariablesCompartidas.release();
			try {
				if(primerBloqueo) {		
					ventana.addTrenBloqueado(tren);
					System.err.println("[ El tren " + tren.getIdTren() + " debe esperar (se bloquea) ]");
					primerBloqueo = false;
				} else {
					System.err.println("[ El tren " + tren.getIdTren() + " ha sido desbloqueado, pero nuevamente debe esperar (se bloquea de nuevo) ]");
				}		
				wait();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			// Usamos el semaforo para sincronizar el acceso a las variables compartidas...
			mutexVariablesCompartidas.acquire();
		}
	}
	
	private void asignarTurno(Tren tren) {
		// Si el tren es el primero que pasa, su sentido sera el que se establezca como prioritario en el sistema		
		if(sentido == -1) {
			numTrenesSeguidosMismoSentido = 1;
			sentido = tren.getNumEstacion();
			mensajeTurno();
		} else if(sentido == tren.getNumEstacion()) {
			// Si ya han pasado cinco (o los que sean) trenes seguidos en el mismo sentido que el que esta saliendo de la estacion, el sentido contrario pasara a tener preferencia en el sistema
			if(++numTrenesSeguidosMismoSentido == NUM_TRENES_SEGUIDOS_MISMO_SENTIDO) {
				numTrenesSeguidosMismoSentido = 0;
				sentido = 1 - tren.getNumEstacion();
				mensajeTurno();
			}
		} else if(numTrenesEsperando[(1-tren.getNumEstacion())] == 0) {
			// Si el turno es de la otra estacion, pero alli no hay trenes esperando para salir, la estacion del tren saliente vuelve a recuperar el turno 
			numTrenesSeguidosMismoSentido = 1;
			sentido = tren.getNumEstacion();
			mensajeTurno();
		}
	}
	
	private void mensajeTurno() {
		System.out.println("El turno para entrar a la via lo tienen ahora los trenes de la estacion " + (sentido+1));
	}
	
	public static int[] getNumTrenesPasando() {
		return numTrenesPasando;
	}

	public static Ventana getVentana() {
		return ventana;
	}
	
	public static Sensor[] getSensoresVia() {
		return sensoresVia;
	}
	
	public static Semaforo[][] getSemaforos() {
		return semaforos;
	}
	
	public static Semaphore getMutexVariablesCompartidas() {
		return mutexVariablesCompartidas;
	}
	
	public static Tren hiloTren() {
		if(Thread.currentThread() instanceof Tren) {
			return (Tren) Thread.currentThread();
		}
		return null;
	}
	
	public static void imprimir(String cadena) {
		System.out.println(cadena);
		ventana.addTexto(cadena + '\n');
	}
	
	public static void retardoVisualizacionAmigable() {
		try {
			Thread.sleep(retardoVisualizacion);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static Controlador getControlador() {
		return controlador;
	}

}
