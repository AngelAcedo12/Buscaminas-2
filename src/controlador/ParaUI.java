package controlador;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import modelo.Coordenada;
import modelo.Dificultad;
import modelo.GestionSonidos;
import modelo.GestionTablero;
import modelo.TableroAleatorio;
import vista.Derrota;
import vista.UI;
import vista.Victoria;

public class ParaUI extends UI {

	private GestionTablero miGestion;
	protected GestionSonidos gestionSonidos;
	private Victoria victoriaPanel;
	private Derrota derrotaPanel;

	public ParaUI(Victoria victoriaPanel, Derrota derrotaPanel) {
		this.gestionSonidos = new GestionSonidos();
		this.victoriaPanel = victoriaPanel;
		this.derrotaPanel = derrotaPanel;

		behaviourDifficultyButtons(getDifficultyButton());
		this.miGestion = new GestionTablero(Dificultad.medio);
		createMouseListener(true);
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

	private MouseListener createMouseListener(boolean velada) {
		MouseListener mouseListener = new MouseListener() {
			private boolean hover = velada;

			public void mouseReleased(MouseEvent e) {
				Coordenada cordenadasBoton = ((BotonCasilla) e.getSource()).getCoordenada();
				// CLICK IZQUIERDO
				if (e.getButton() == 1) {
					miGestion.hacerMovimiento(cordenadasBoton);
					reprodicirEfecto();
					actualizarTablero();

					if (miGestion.getNumeroMinas() == miGestion.getMiTablero().getCasillasVeladas()) {
						mostrarVictoria();
						removeAllMouseListener();
						reproducirEfectoVictoria();

					}
				}
				// CLICK DERECHO
				if (e.getButton() == 3) {
					miGestion.marcarCasilla(cordenadasBoton);
					actualizarTablero();
				}
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
				if (hover)
					quitHover((BotonCasilla) e.getSource());

			}

			public void mouseEntered(MouseEvent e) {
				if (hover)
					setHover((BotonCasilla) e.getSource());
			}

			public void mouseClicked(MouseEvent e) {
				setClicked((BotonCasilla) e.getSource());

			}

		};
		return mouseListener;
	}

	public void actualizarTablero() {
		TableroAleatorio miTablero = miGestion.getMiTablero();
		// miTablero.showMinasTablero();
		boolean terminado = false;
		for (int i = 0; i < miTablero.getAncho() && !terminado; i++) {
			for (int j = 0; j < miTablero.getAlto() && !terminado; j++) {
				Coordenada coordenadaActual = new Coordenada(i, j);
				BotonCasilla boton = getPanelMinador().getBoton(coordenadaActual);
				// SI NO ESTÁ DESVELADA, IMPRIMIR VACIO
				if (miTablero.getCasilla(coordenadaActual).isMarcada()) {

					Image flagIMG = Toolkit.getDefaultToolkit().getImage("resources/FLAG.png")
							.getScaledInstance(boton.getWidth() - 3, boton.getWidth() - 3, Image.SCALE_DEFAULT);
					boton.setHorizontalAlignment(SwingConstants.LEFT);

					boton.setIcon(new ImageIcon(flagIMG));

				} else if (miTablero.getCasilla(coordenadaActual).isVelada()) {
					boton.setIcon(null);
					boton.setHorizontalAlignment(SwingConstants.CENTER);
				} else {

					boton.removeMouseListener(boton.getMouseListeners()[1]);
					boton.addMouseListener(createMouseListener(false));
					setClicked(boton);
					// SI ESTA DESVELADA Y ES BOMBA
					if (miTablero.getCasilla(coordenadaActual).isMina()) {

						Image img = Toolkit.getDefaultToolkit().getImage("resources/MINA.png")
								.getScaledInstance(boton.getWidth() - 3, boton.getWidth() - 3, Image.SCALE_DEFAULT);
						boton.setHorizontalAlignment(SwingConstants.LEFT);
						boton.setIcon(new ImageIcon(img));
						boton.setBackground(new Color(190, 61, 61));

						miGestion.getDificultad().getLongitud();

						removeAllMouseListener();
						mostrarDerrota();
						reproducirEfectoDerrota();
						terminado = true;
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

	private void removeAllMouseListener() {
		BotonCasilla[][] casillas = getPanelMinador().getBotones();
		for (int i = 0; i < casillas.length; i++) {
			for (int j = 0; j < casillas[0].length; j++) {
				if (casillas[i][j].getMouseListeners().length == 2) {
					casillas[i][j].removeMouseListener(casillas[i][j].getMouseListeners()[1]);
				}
			}
		}
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
				tableroBotones[i][j].addMouseListener(createMouseListener(true));
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
