package clasesDelModelo;

public enum TipoTren {
	MERCANCIAS, PASAJEROS;
	
	public static TipoTren getRandom() {
		return values()[(int) (Math.random() * values().length)];
	}
}