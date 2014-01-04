/**
 *
 */
package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.media.CannotRealizeException;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.swing.*;
import javax.media.*;


import server.ChangeSourceDatagram;
import server.Datagram;
import server.Source;
import server.SourceUpdateDatagram;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PlayerClient implements ActionListener
{
	
	private JFrame frame;
	private JPanel mainPanel, playerPanel, controlPanel;
	private JComboBox<Source> sources;
	private JLabel statusLabel;
	private JTextArea statusArea, consoleArea;
	private JScrollPane consolePane;
	private JButton consoleToggle;
	private final String HOST_ADDRESS = "192.168.1.101";
	
	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private Player player;
	private Component controls;
	
	public PlayerClient()
	{		
		frame = new JFrame("Audiocast Player");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setPreferredSize(new Dimension(150, 185));
		frame.setResizable(true);
		
		player = null;
		mainPanel = new JPanel();
		playerPanel = new JPanel();
		controlPanel = new JPanel();
		controlPanel.setPreferredSize(new Dimension(125, 220));
				
		sources = new JComboBox<Source>(new Source[0]);
		sources.setActionCommand("sourcesList");
		sources.addActionListener(this);
		
		statusLabel = new JLabel("Status:");
		
		statusArea = new JTextArea();
		statusArea.setEditable(false);
		controlPanel.add(statusLabel);
		controlPanel.add(statusArea);
		
		consoleToggle = new JButton("Show Console");
		consoleToggle.setActionCommand("consoleToggle");
		consoleToggle.addActionListener(this);
		
		consoleArea = new JTextArea();
		consoleArea.setEditable(false);
		consolePane = new JScrollPane(consoleArea);
		consolePane.setPreferredSize(new Dimension(125, 125));
		consolePane.setVisible(false);
		
		controlPanel.add(sources);
		controlPanel.add(consoleToggle);
		controlPanel.add(consolePane);
		
		mainPanel.add(new JLabel("Audiocast Player"));
		mainPanel.add(playerPanel);
		mainPanel.add(controlPanel);
		
		frame.add(mainPanel);
		frame.pack();
		
		connect();
	}
	
	/**
	 * Listens for a ChangeSourceDatagram, which indicates that the JMF has changed its audio source, and resets the player
	 * in order to restart the JMF connection.
	 */
	public void run()
	{
		Datagram gram;
		while(true)
		{
			try
			{
				gram = (Datagram)ois.readObject();
				switch(gram.getType())
				{
				case "ChangeSource":
					player.close();
					player = Manager.createRealizedPlayer(new MediaLocator("rtp://"+HOST_ADDRESS+":3001/audio"));
					if(player.getControlPanelComponent() != null)
					{
						controls = player.getControlPanelComponent();
						playerPanel.add(controls);
						frame.pack();
					}
					break;
				case "SourceUpdate":
					consoleArea.append("SourceUpdateDatagram recieved. Updating sourcelist.\n");
					sources.setModel(new DefaultComboBoxModel<Source>(((SourceUpdateDatagram)gram).getAvailableSources()));
					break;
				}
			} catch (ClassNotFoundException | NoPlayerException
					| CannotRealizeException | IOException e)
			{
				consoleArea.append(e.getMessage()+" exception thrown while reading object.\n");
			}
		}
	}
	
	private void connect()
	{
		// Establishing the connection to the server and to the JMF stream.
		while(socket == null)
		{
			try
			{
				consoleArea.append("Connecting to server.\n");
				statusArea.setText("Connecting to server.");
				socket = new Socket(HOST_ADDRESS, 3000);
				oos = new ObjectOutputStream(socket.getOutputStream());
				ois = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e1)
			{
				consoleArea.append("Connection failed.\n");
				statusArea.setText("Connection failed.");
				if(JOptionPane.showConfirmDialog(frame, "Failed to connect to server. Retry?", "Connection Failed",
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					System.exit(0);
			}
		}
		boolean streamConnected = false;
		while(!streamConnected)
		{
			try
			{
				statusArea.setText("Connecting to stream.");
				consoleArea.append("Connecting to stream.\n");
				player = Manager.createRealizedPlayer(new MediaLocator("rtp://"+HOST_ADDRESS+":3001/audio"));
				if(player.getControlPanelComponent() != null)
				{
					controls = player.getControlPanelComponent();
					playerPanel.add(controls);
				}
				streamConnected = true;
			}
			catch(Exception e)
			{
				statusArea.setText("Stream connection failed.");
				consoleArea.append("Stream connection failed.\n");
				if(JOptionPane.showConfirmDialog(frame, "Failed to connect to server. Retry?", "Connection Failed",
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					System.exit(0);
			}
		}
		statusArea.setText("Connected");
		consoleArea.append("Connected.\n");
	}

	public static void main(String[] args)
	{
		new PlayerClient().run();

	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		switch(e.getActionCommand())
		{
		// Sends a ChangeSourceDatagram when the user selects a source from the sources combobox.
		case "sourcesList":
		{
			try
			{
				oos.writeObject(new ChangeSourceDatagram((Source)sources.getSelectedItem()));
				playerPanel.remove(controls);
				frame.pack();
			} catch (IOException e1)
			{
				consoleArea.append("IOException thrown while changing source.\n");
			}
			break;
		}
		// Toggles the visibility of the console.
		case "consoleToggle":
		{
			boolean currentVis = consolePane.isVisible();
			consolePane.setVisible(!currentVis);
			if(currentVis)
			{
				consoleToggle.setText("Show Console");
				frame.setPreferredSize(new Dimension(150, 185));
			}
			else
			{
				consoleToggle.setText("Hide Console");
				frame.setPreferredSize(new Dimension(150, 315));
			}
			frame.pack();
			break;
		}
		}
	}

}
