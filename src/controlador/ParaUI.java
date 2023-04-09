package controlador;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import modelo.Coordenada;
import modelo.Densidad;
import modelo.Dificultad;
import modelo.GestionSonidos;
import modelo.GestionTablero;
import modelo.Tablero;
import modelo.TableroAleatorio;
import vista.Derrota;
import vista.UI;
import vista.Victoria;

public class ParaUI extends UI {

	private GestionTablero miGestion;
	private MouseListener mouseListener;
	protected GestionSonidos gestionSonidos;
	private Victoria victoriaPanel;
	private Derrota derrotaPanel;
	
	public ParaUI(Victoria victoriaPanel, Derrota derrotaPanel) {
		this.gestionSonidos = new GestionSonidos();
		this.victoriaPanel =  victoriaPanel;
		this.derrotaPanel = derrotaPanel;
		
		behaviourDifficultyButtons(getDifficultyButton());
		this.miGestion = new GestionTablero(Dificultad.medio);
		createMouseListener();
		behaviourGameButtons();
		listenerSliceControl();
		gestionSonidos.reproducirMusica();
	}

	private void listenerSliceControl() {
		JSlider slider = getSlider();
		slider.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent e) {
				float volumenActual = getSlider().getValue();
				gestionSonidos.cambiarVolumen(volumenActual);
			}
		});

	}

	private void createMouseListener() {
		this.mouseListener = new MouseListener() {

			public void mouseReleased(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
				quitHover((BotonCasilla) e.getSource());
			}

			public void mouseEntered(MouseEvent e) {
				setHover((BotonCasilla) e.getSource());
			}

			public void mouseClicked(MouseEvent e) {
				setClicked((BotonCasilla) e.getSource());
			}
		};
	}

	public void actualizarTablero() {
		TableroAleatorio miTablero = miGestion.getMiTablero();

		for (int i = 0; i < miTablero.getAncho(); i++) {
			for (int j = 0; j < miTablero.getAlto(); j++) {
				Coordenada coordenadaActual = new Coordenada(i, j);
				BotonCasilla boton = getPanelMinador().getBoton(coordenadaActual);
				// SI NO ESTÁ DESVELADA, IMPRIMIR VACIO
				if (miTablero.getCasilla(coordenadaActual).isVelada()) {
					boton.setText(" ");
				} else {
					boton.removeMouseListener(this.mouseListener);
					setClicked(boton);
					// SI ESTA DESVELADA Y ES BOMBA
					if (miTablero.getCasilla(coordenadaActual).isMina()) {
						Image img = Toolkit.getDefaultToolkit().getImage("resources/MINA.png").getScaledInstance(
								boton.getWidth()-3, boton.getWidth()-3, Image.SCALE_DEFAULT);
						boton.setHorizontalAlignment(SwingConstants.LEFT);
						boton.setIcon(new ImageIcon(img));
						boton.setBackground(new Color(190, 61, 61));
						
						miGestion.getDificultad().getLongitud();
						
						Botonera panelMinador = getPanelMinador();
						panelMinador.disableButtons();
						
						mostrarDerrota();
						reproducirEfectoDerrota();

					} // SI ESTA DESVELADA Y NO ES BOMBA
					else {
						int minasAlrededor = miTablero.getCasilla(coordenadaActual).getMinasAlrededor();
						if (minasAlrededor == 0)
							boton.setText(" ");
						else
							boton.setText(Integer.toString(minasAlrededor));

					}
				}
			}
		}
	}

	public void quitMouseListener(BotonCasilla boton) {
		boton.removeMouseListener(null);
		;
	}

	// COMPORTAMIENTO BOTON DIFICULTADES
	public void behaviourDifficultyButtons(JMenuItem myButtons[]) {
		// FACIL
		myButtons[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getPanelMinador().crearBotonera(Dificultad.facil);
				miGestion = new GestionTablero(Dificultad.facil);
				behaviourGameButtons();
				
				esconderPaneles();
				// actualizar
				getPanelMinador().revalidate();
				// System.out.println("Actualizado a facil");
			}
		});
		// MEDIO
		myButtons[1].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getPanelMinador().crearBotonera(Dificultad.medio);
				miGestion = new GestionTablero(Dificultad.medio);
				behaviourGameButtons();
				
				esconderPaneles();
				// actualizar
				getPanelMinador().revalidate();
				// System.out.println("Actualizado a medio");
			}
		});
		// DIFICIL
		myButtons[2].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getPanelMinador().crearBotonera(Dificultad.dificil);
				miGestion = new GestionTablero(Dificultad.dificil);
				behaviourGameButtons();
				
				esconderPaneles();
				// actualizar
				getPanelMinador().revalidate();
				// System.out.println("Actualizado a dificil");
			}
		});

	}

	// COMPORTAMIENTO BOTON JUEGO
	public void behaviourGameButtons() {
		BotonCasilla tableroBotones[][] = getPanelMinador().getBotones();
		for (int i = 0; i < tableroBotones.length; i++) {
			for (int j = 0; j < tableroBotones[0].length; j++) {
				tableroBotones[i][j].addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						Coordenada cordenadasBoton = ((BotonCasilla) e.getSource()).getCoordenada();
						miGestion.hacerMovimiento(cordenadasBoton);
						reprodicirEfecto();
						actualizarTablero();

						if(miGestion.getNumeroMinas() == miGestion.getMiTablero().getCasillasVeladas()) {
							mostrarVictoria();
							getPanelMinador().disableButtons();
							reproducirEfectoVictoria();
							
						}
					
					}
				});

				tableroBotones[i][j].addMouseListener(this.mouseListener);

			}
		}
	}

	
	
	
	public void mostrarVictoria() {
		victoriaPanel.setVisible(true);
	}

	public void mostrarDerrota() {
		derrotaPanel.setVisible(true);
	}

	private void reproducirEfectoDerrota() {
		gestionSonidos.reproducirEfectoDerrota();

	}
	private void reproducirEfectoVictoria() {
		gestionSonidos.reproducirEfectoVictoria();
	}
	private void reprodicirEfecto() {
		gestionSonidos.reproducirEfecto();
	}
	
	private void esconderPaneles() {
		this.derrotaPanel.setVisible(false);
		this.victoriaPanel.setVisible(false);
	}

}
