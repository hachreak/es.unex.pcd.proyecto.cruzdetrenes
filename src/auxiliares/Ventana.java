package auxiliares;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import clasesDelModelo.Tren;

/*
 * NOTA: La implementacion de esta clase no es la mejor ni la mas adecuada, 
 * pues la hemos hecho sin ajustarnos a las buenas practicas de programacion. 
 * El objetivo era crear una interfaz de usuario funcional, independientemente 
 * de la calidad del codigo.
 */

public class Ventana extends JFrame implements Runnable {
	private JPanel panelPrincipal;
	private JPanel panelSuperior, panelInferior, panelIzquierda;
	private DefaultListModel listModel, listModelTrenesCentro;
	private JScrollPane listScroller;
	private JList list, trenesCentro;
	private GridLayout semaforosInferiores, semaforosIzquierda;
	private DefaultListModel[][] listasTrenes;
	private JScrollPane[][] listScrollers;
	private JList[][] lists;
	private JPanel panelCentral;
	private JLabel sensor1, sensor2, imagenCentral;
	private Color colorFondo = new Color(243,245,249);
	private Map<Tren, JLabel> etiquetasTrenes = new HashMap<Tren, JLabel>();
	private List<String> trenesSolicitantes = new ArrayList<String>();
	private List<String> trenesBloqueados = new ArrayList<String>();

	/**
	 * Create the frame.
	 */
	public Ventana() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Cruce de trenes - Proyecto PCD");
		setBounds(100, 25, 570, 640);
		panelPrincipal = new JPanel();
		panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));
		panelPrincipal.setLayout(new BorderLayout(10, 10));
		this.setResizable(false);
		panelPrincipal.setBackground(colorFondo);
		setContentPane(panelPrincipal);
		
		listasTrenes = new DefaultListModel[2][3];
		listScrollers = new JScrollPane[2][3];
		lists = new JList[2][3];
		for(int i=0; i<2; i++)
			for(int j=0; j<3; j++) {
				listasTrenes[i][j] = new DefaultListModel();
				JList lista = new JList(listasTrenes[i][j]);
				lists[i][j] = lista;
				lista.setCellRenderer( new MyListRenderer() );  
				lista.setLayoutOrientation(JList.VERTICAL);
				JScrollPane listScrollerAux = new JScrollPane(lista);
				listScrollerAux.setPreferredSize(new Dimension(150, 150));
				listScrollerAux.setMaximumSize(new Dimension(150, 150));
				listScrollers[i][j] = listScrollerAux;
				lista.setBackground(new Color(248,253,255));
			}
		
		panelIzquierda = new JPanel();
		panelIzquierda.setBackground(colorFondo);
		semaforosIzquierda = new GridLayout(3, 2, 5, 5);
		panelIzquierda.setLayout(semaforosIzquierda);
		panelIzquierda.add(listScrollers[0][0]);
		panelIzquierda.add(getSemaforoCerrado());
		panelIzquierda.add(listScrollers[0][1]);
		panelIzquierda.add(getSemaforoCerrado());
		panelIzquierda.add(listScrollers[0][2]);
		panelIzquierda.add(getSemaforoCerrado());
		panelIzquierda.setPreferredSize(new Dimension(200, 350));
		panelPrincipal.add(panelIzquierda, BorderLayout.WEST);
		
		panelInferior = new JPanel();
		panelInferior.setBackground(colorFondo);
		semaforosInferiores = new GridLayout(2, 4, 5, 5);
		panelInferior.setLayout(semaforosInferiores);
		panelInferior.add(new JLabel("             "));
		panelInferior.add(getSemaforoCerrado());
		panelInferior.add(getSemaforoCerrado());
		panelInferior.add(getSemaforoCerrado());
		DefaultListModel modLeyenda = new DefaultListModel();
		
		JList leyenda = new JList(modLeyenda);
		leyenda.setCellRenderer( new MyListRenderer2() );  
		leyenda.setLayoutOrientation(JList.VERTICAL);
		JScrollPane listScrollerAux = new JScrollPane(leyenda);
		listScrollerAux.setPreferredSize(new Dimension(150, 150));
		listScrollerAux.setMaximumSize(new Dimension(150, 150));
		leyenda.setBackground(colorFondo);
		modLeyenda.addElement(" ");
		modLeyenda.addElement(" ");
		modLeyenda.addElement("Tren inactivo");
		modLeyenda.addElement("Tren solicitante");
		modLeyenda.addElement("Tren bloqueado");
		leyenda.setFocusable(false);
		
		leyenda.setForeground(colorFondo);
		panelInferior.add(leyenda);
		panelInferior.add(listScrollers[1][0]);
		panelInferior.add(listScrollers[1][1]);
		panelInferior.add(listScrollers[1][2]);
		panelInferior.setPreferredSize(new Dimension(350, 200));
		panelPrincipal.add(panelInferior, BorderLayout.SOUTH);
		
		panelSuperior = new JPanel();
		panelSuperior.setBackground(colorFondo);
		panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
		
		listModel = new DefaultListModel();
		list = new JList(listModel);
		list.setLayoutOrientation(JList.VERTICAL);
		listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(520, 100));
		panelSuperior.add(listScroller);
		
		panelCentral = new JPanel();
		panelCentral.setBackground(colorFondo);
		
		imagenCentral = new JLabel("");
		imagenCentral.setBounds(0, 50, 200, 200);
		panelCentral.add(imagenCentral);
		
		sensor2 = getSensorVia(1);
		sensor2.setBounds(5 , -30 , 250, 60);
		panelCentral.add(sensor2);
		
		listModelTrenesCentro = new DefaultListModel();
		trenesCentro = new JList(listModelTrenesCentro);
		trenesCentro.setOpaque(false);
		trenesCentro.setBounds(85, 100, 75, 80);
		trenesCentro.setBackground(colorFondo);
		//panelCentral.add(trenesCentro);
		
		sensor1 = getSensorVia(0);
		sensor1.setBounds(260, 70, 60, 180);
		panelCentral.add(sensor1);
		
		panelCentral.setLayout(null);
		panelPrincipal.add(panelCentral, BorderLayout.CENTER);
		
	}
	
	public void addTexto(String texto) {	
		
		final String text = texto; 
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	listModel.addElement(text);
            	list.ensureIndexIsVisible(listModel.size()-1);  
            }
         });
	}
	
	public void cerrarSemaforo(int estacion) {	
		if(estacion == 0) {
			panelIzquierda.remove(1);
			panelIzquierda.add(getSemaforoCerrado(), 1);
			panelIzquierda.remove(3);
			panelIzquierda.add(getSemaforoCerrado(), 3);		
			panelIzquierda.remove(5);
			panelIzquierda.add(getSemaforoCerrado(), 5);
			panelIzquierda.revalidate();
		} else {
			panelInferior.remove(1);
			panelInferior.add(getSemaforoCerrado(), 1);
			panelInferior.remove(2);
			panelInferior.add(getSemaforoCerrado(), 2);		
			panelInferior.remove(3);
			panelInferior.add(getSemaforoCerrado(), 3);
			panelInferior.revalidate();
		}
	}
	
	public void abrirSemaforo(Tren tren) {	
		int estacion = tren.getNumEstacion();
		int semaforo = tren.getNumViaEstacion();
		
		if(estacion == 0)
			imagenCentral.setIcon(new ImageIcon("horizontal.png"));
		else
			imagenCentral.setIcon(new ImageIcon("vertical.png"));
		
		lists[estacion][semaforo].setSelectedValue(tren.toString(), true);
		
		if(estacion == 0) {
			switch(semaforo) {
			case 0:
				panelIzquierda.remove(1);
				panelIzquierda.add(getSemaforoAbierto(), 1);
				break;
			case 1:
				panelIzquierda.remove(3);
				panelIzquierda.add(getSemaforoAbierto(), 3);
				break;
			case 2:
				panelIzquierda.remove(5);
				panelIzquierda.add(getSemaforoAbierto(), 5);
				break;
			}
			panelIzquierda.revalidate();
		} else {
			switch(semaforo) {
			case 0:
				panelInferior.remove(1);
				panelInferior.add(getSemaforoAbierto(), 1);
				break;
			case 1:
				panelInferior.remove(2);
				panelInferior.add(getSemaforoAbierto(), 2);
				break;
			case 2:
				panelInferior.remove(3);
				panelInferior.add(getSemaforoAbierto(), 3);
				break;
			}
			panelInferior.revalidate();
		}
		
	}
	
	public void sacarTrenDeEstacion(Tren tren) {
		listasTrenes[tren.getNumEstacion()][tren.getNumViaEstacion()].removeElement(tren.toString());
		listModelTrenesCentro.addElement(tren.toString());
		JLabel etiquetaT = new JLabel("");
		etiquetaT.setIcon(new ImageIcon("tren.gif"));
		etiquetaT.setText("" + tren.getIdTren());
		etiquetaT.setFont(new Font(etiquetaT.getFont().getName(), Font.PLAIN, 15));
		etiquetasTrenes.put(tren, etiquetaT);
		if(tren.getNumEstacion() == 0)
			etiquetaT.setBounds(35, 125, 70, 35);
		else
			etiquetaT.setBounds(100, 220, 70, 35);
		panelCentral.add(etiquetaT);
		panelCentral.repaint();
	}
	
	public void moverTren (Tren tren, int x, int y) {
		JLabel etiquetaTren = etiquetasTrenes.get(tren);
		Rectangle medidas = etiquetaTren.getBounds();
		etiquetaTren.setBounds((int)(medidas.getX()+x),(int)(medidas.getY()+y), 70, 35);
		panelCentral.repaint();
	}
	
	public void insertarTrenEnEstacion(Tren tren) {
		listasTrenes[tren.getNumEstacion()][tren.getNumViaEstacion()].addElement(tren.toString());
	}
	
	public static JLabel getSemaforoCerrado() {
		JLabel etiqueta = new JLabel("", JLabel.CENTER);
		etiqueta.setIcon(new ImageIcon("semafororojo.png"));
		return etiqueta;
	}
	
	public static JLabel getSemaforoAbierto() {
		JLabel etiqueta = new JLabel("", JLabel.CENTER);
		etiqueta.setIcon(new ImageIcon("semaforoverde.png"));
		return etiqueta;
	}
	
	public void activarSensor(Tren tren) {
		JLabel sensor;
		int estacion = tren.getNumEstacion();
		if(estacion == 0)
			sensor = sensor1;
		else
			sensor = sensor2;
		sensor.setForeground(Color.ORANGE);
		trenesCentro.setSelectedValue(tren.toString(), true);
	}
	
	public void desactivarSensor(Tren tren) {
		JLabel sensor;
		int estacion = tren.getNumEstacion();
		if(estacion == 0)
			sensor = sensor1;
		else
			sensor = sensor2;
		sensor.setForeground(Color.BLACK);
		listModelTrenesCentro.removeElement(tren.toString());
		if(listModelTrenesCentro.isEmpty())
			imagenCentral.setIcon(new ImageIcon("transparente.png"));
		JLabel etiquetaTren = etiquetasTrenes.get(tren);
		panelCentral.remove(etiquetaTren);
		panelCentral.repaint();
	}
	
	public synchronized void addTrenSolicitante(Tren tren) {
		trenesSolicitantes.add(tren.toString());
		panelInferior.revalidate();
		panelIzquierda.revalidate();
		panelInferior.repaint();
		panelIzquierda.repaint();
		repaint();
	}
	
	public synchronized void addTrenBloqueado(Tren tren) {
		trenesBloqueados.add(tren.toString());
		panelInferior.revalidate();
		panelIzquierda.revalidate();
		panelInferior.repaint();
		panelIzquierda.repaint();
		repaint();
	}
	
	public static JLabel getSensorVia(int numVia) {
		JLabel etiqueta;
		int tam = 30;
		if(numVia == 0) {
			etiqueta = new JLabel("|", JLabel.CENTER);
		} else {
			etiqueta = new JLabel("_", JLabel.CENTER);
			etiqueta.setVerticalTextPosition(JLabel.TOP);
			tam = 40;
		}
		etiqueta.setForeground(Color.BLACK);
		etiqueta.setFont(new Font(etiqueta.getFont().getName(), Font.BOLD, tam));
		return etiqueta;
	}
	
	private class MyListRenderer extends DefaultListCellRenderer  
    {  
   
        public Component getListCellRendererComponent( JList list,  
                Object value, int index, boolean isSelected,  
                boolean cellHasFocus )  
        {  
            super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );  
   
            if( isSelected )  
            {  
            	setForeground( new Color(40,130,10) );  
            } else if(trenesBloqueados.contains(value)) {
        		setForeground( Color.red );  
            } else if(trenesSolicitantes.contains(value)) {
         		setForeground( Color.blue );  
            } else {
            	setForeground( Color.black ); 
            }
   
            return( this );  
        }  
    }  
	
	private class MyListRenderer2 extends DefaultListCellRenderer  
    {  
   
        public Component getListCellRendererComponent( JList list,  
                Object value, int index, boolean isSelected,  
                boolean cellHasFocus )  
        {  
            super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );  
   
            list.clearSelection();

            setFont(new Font(this.getFont().getName(), Font.PLAIN, 12));
            
            if(value=="Tren bloqueado") {
        		setForeground( Color.red );  
            } else if(value=="Tren solicitante") {
         		setForeground( Color.blue );  
            } else {
            	setForeground( Color.black ); 
            }
   
            return( this );  
        }  
    }  

	@Override
	public void run() {
		this.setVisible(true);
	}

}
