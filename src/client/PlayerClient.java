/**
 *
 */
package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;

import server.ByteBufferDatagram;
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
	private JButton consoleToggle, playButton;
	private final String HOST_ADDRESS = "192.168.1.101";
	private SourceDataLine output;
	private boolean play;
	
	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	
	public PlayerClient()
	{	
		play = false;
		AudioFormat format = new AudioFormat(192000, 16, 2, true, true);
		try
		{
			output = AudioSystem.getSourceDataLine(format);
			output.open(format);
			output.start();
		} catch (LineUnavailableException e)
		{
			e.printStackTrace();
		}
		
		frame = new JFrame("Audiocast Player");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setPreferredSize(new Dimension(150, 205));
		frame.setResizable(false);
		
		mainPanel = new JPanel();
		playerPanel = new JPanel();
		controlPanel = new JPanel();
		controlPanel.setPreferredSize(new Dimension(125, 220));
		
		mainPanel.setBackground(new Color(54, 201, 153));
		playerPanel.setBackground(new Color(54, 201, 153));
		controlPanel.setBackground(new Color(54, 201, 153));

		
		playButton = new JButton("Play");
		playButton.setActionCommand("play");
		playButton.addActionListener(this);
		playButton.setBackground(new Color(127, 219, 207));
		playerPanel.add(playButton);
				
		sources = new JComboBox<Source>(new Source[0]);
		sources.setActionCommand("sourcesList");
		sources.addActionListener(this);
		sources.setBackground(new Color(127, 219, 207));

		
		statusLabel = new JLabel("Status:");
		
		statusArea = new JTextArea();
		statusArea.setEditable(false);
		statusArea.setBackground(new Color(127, 219, 207));
		
		controlPanel.add(statusLabel);
		controlPanel.add(statusArea);
		
		consoleToggle = new JButton("Show Console");
		consoleToggle.setActionCommand("consoleToggle");
		consoleToggle.addActionListener(this);
		consoleToggle.setBackground(new Color(127, 219, 207));
		
		consoleArea = new JTextArea();
		consoleArea.setEditable(false);
		consolePane = new JScrollPane(consoleArea);
		consolePane.setPreferredSize(new Dimension(125, 125));
		consolePane.setVisible(false);
		consoleArea.setBackground(new Color(127, 219, 207));
		
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
		while(true)
		{
			try
			{
				Datagram gram = (Datagram)ois.readObject();
				switch(gram.getType())
				{
				case "SourceUpdate":
					JOptionPane.showMessageDialog(frame, "Source list updated. Please reselect source.",
							"Source List Updated", JOptionPane.INFORMATION_MESSAGE);
					consoleArea.append("SourceUpdateDatagram recieved. Updating sourcelist.\n");
					sources.setModel(new DefaultComboBoxModel<Source>(((SourceUpdateDatagram)gram).getAvailableSources()));
					playButton.setText("Play");
					break;
				case "ByteBuffer":
					ByteBufferDatagram bbgram = ((ByteBufferDatagram)gram);
					consoleArea.append("ByteBufferDatagram recieved. Playing bytes.\n");
					if(play)
						output.write(bbgram.getBuffer(), 0, bbgram.getBuffer().length);
				}
			} catch (ClassNotFoundException | IOException e)
			{
				consoleArea.append(e.getMessage()+" exception thrown while reading object.\n");
			}
		}
	}
	
	/**
	 * Establishes connection to the server.
	 */
	private void connect()
	{
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
				frame.setPreferredSize(new Dimension(150, 205));
			}
			else
			{
				consoleToggle.setText("Hide Console");
				frame.setPreferredSize(new Dimension(150, 325));
			}
			frame.pack();
			break;
		}
		case "play":
		{
			if(!play)
			{
				play = true;
				playButton.setText("Stop");
			}
			else
			{
				play = false;
				playButton.setText("Play");
			}
		}
		}
	}

}
