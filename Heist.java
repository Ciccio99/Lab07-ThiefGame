import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * File:
 * $Id: Heist.java,v 1.2 2014/04/24 00:58:33 afs2842 Exp $
 * 
 * Log:
 * $Log: Heist.java,v $
 * Revision 1.2  2014/04/24 00:58:33  afs2842
 * Done...
 *
 * Revision 1.1  2014/04/24 00:04:22  afs2842
 * Working state, needs to be cleaned up and commented...
 *
 * 
 */

/**
 * @author Alberto Scicali
 *
 */

public class Heist extends JFrame implements Observer{
	private HeistModel zeGame;
	private ArrayList<JButton> alarmTiles = new ArrayList<JButton>();
	private Timer updateTimer;
	// The top info field that holds move, EMP, and win status information
	private JTextField infoField;
	
	/**
	 * Constructor for Heist object; creates the GUI and instantiates everything that is needed
	 * @param game : HeistModel
	 */
	public Heist(HeistModel game){
		// Creating the main window frame
		super("Heist Game");
		setLayout(new BorderLayout());
		setSize(500, 500);
		setResizable(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		zeGame = game;
		zeGame.addObserver(this);
		
		buildGUI();
		
		// Builds the timer used to update the alarm pattern
		updateTimer = new Timer(game.getRefreshRate(), new TimerListener());
		updateGrid();
		
		// Begins the timer
		updateTimer.start();
	}
	
	/**
	 * Constructs the GUI with a series of JPanels and objects. 
	 * Frame is set to visible once everything has been added.
	 */
	private void buildGUI(){
		// All the necessary panels
		JPanel gridPanel = new JPanel(new GridLayout(zeGame.getDim(), zeGame.getDim()));
		JPanel bottomPanel = new JPanel(new BorderLayout());
		JPanel bottomRightP = new JPanel();
		JPanel topPanel = new JPanel();
		// All the objects within the panels
		infoField = new JTextField("");
		createGridButtons(gridPanel);
		JTextField exitText = new JTextField("ENTER / EXIT");
		JButton empButton = new JButton("EMP");
		empButton.addActionListener(new EMPListener());
		JButton resetButton = new JButton("RESET");
		resetButton.addActionListener(new ResetListener());
		// Adding the pieces to their appropriate panels
		bottomRightP.add(empButton);
		bottomRightP.add(resetButton);
		bottomPanel.add(exitText, BorderLayout.WEST);
		bottomPanel.add(bottomRightP, BorderLayout.EAST);
		// Adding the panels to the main GUI panel
		add(infoField, BorderLayout.NORTH);
		add(gridPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
		
		setVisible(true);
	}
	
	/**
	 * Creates the alarm grid button, and add the appropriate ActionListener to them
	 * so that they may be interacted with for the game.
	 * @param grid : a JPanel with a GridLayout
	 */
	private void createGridButtons(JPanel grid){
		for (int i = 0; i < Math.pow(zeGame.getDim(), 2); i++){
            JButton button = new JButton();
            button.setForeground(Color.BLACK);
            button.setBorderPainted(true);
        	button.setContentAreaFilled(false);
        	button.setOpaque(true);
        	button.setActionCommand(Integer.toString(i));
        	button.addActionListener(new ButtonListener());
            alarmTiles.add(button);
            grid.add(button);
        }
	}
	
	/**
	 * Updates the alarm tile buttons to their appropriate pattern. 
	 * Also displays where the thief  and jewels are on the tile map.
	 */
	private void updateGrid(){
		ArrayList<Boolean> alarms = (ArrayList<Boolean>) zeGame.getAlarms();
		for(int i = 0; i < Math.pow(zeGame.getDim(), 2); i++){
			if(alarms.get(i)){
				alarmTiles.get(i).setIcon(null);
            	alarmTiles.get(i).setBackground(Color.BLUE);
			}
            else{
				alarmTiles.get(i).setIcon(null);
            	alarmTiles.get(i).setBackground(Color.WHITE);
            }
			
			if(i == zeGame.getThiefLocation()){
				if(zeGame.getAreJewelsStolen())
					alarmTiles.get(i).setIcon(new ImageIcon("Escape.JPG"));
				else
					alarmTiles.get(i).setIcon(new ImageIcon("Thief.JPG"));
			}
			else if(!zeGame.getAreJewelsStolen() && i == zeGame.getJewelsLocation())
				alarmTiles.get(i).setIcon(new ImageIcon("Jewels.JPG"));
		}
		validate();
		updateInfo();
	}
	
	
	/**
	 * Updates the text info field at the top of the GUI with the move count, 
	 * if the EMP as been used or not and displays a winning or losing state.
	 */
	private void updateInfo(){
		if(zeGame.getGameStatus() == 0)
			infoField.setText("Moves: " + zeGame.getMoveCount() + " GAME OVER -- ALARM TRIGGERED!");
		else if(zeGame.getGameStatus() == 2)
			infoField.setText("Moves: " + zeGame.getMoveCount() + " YOU WON!!!!");
		else
			infoField.setText("Moves: " + zeGame.getMoveCount() + "\t EMP availablity: " + !zeGame.getEMPUsed());
	}
	
	/**
	 * Observer override, updates the state of the game when necessary.
	 */
	public void update(Observable arg0, Object arg1) {
		updateGrid();
	}
	
	/**
	 * The main method the instantiates the game
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
	    new Heist(new HeistModel("heist1.txt"));
	}
	
	/**
	 * ActionListener that listens for button clicks so that
	 * the character can move.
	 * @author Alberto Scicali
	 *
	 */
	class ButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			zeGame.selectCell(Integer.parseInt(e.getActionCommand()));
		}
	}
	
	/**
	 * ActionListener that sets of the EMP when the button 
	 * is pressed, only if the EMP has not been used already.
	 * @author Alberto Scicali
	 *
	 */
	class EMPListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(!zeGame.getEMPUsed())
				zeGame.disableAlarm();
		}
	}
	
	/**
	 * ActionListener that listens for if the reset button
	 * is pressed so that the state of the game may be reset.
	 * @author Alberto Scicli
	 *
	 */
	class ResetListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			zeGame.reset();
			updateTimer.restart();
		}
	}

	/**
	 * ActionListener that listens for when the Timer object
	 * calls for its action, which is to update the alarm pattern.
	 * @author Alberto Scicali
	 *
	 */
	class TimerListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			zeGame.updateAlarmPattern();
		}
	}
}
