/**
 *
 */
package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import javax.media.CannotRealizeException;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.swing.*;
import javax.media.*;
import javax.media.format.AudioFormat;
import javax.media.rtp.InvalidSessionAddressException;
import javax.media.rtp.RTPManager;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SessionAddress;

import server.ChangeSourceDatagram;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PlayerClient implements ActionListener
{
	
	private JFrame frame;
	private JPanel playerPanel, controlPanel;
	private JComboBox sources;
	
	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private Player player;
	private Component controls;
	
	public PlayerClient()
	{
		String[] sourceList = { "BigScreen", "TV1", "TV2", "TV3", "TV4", "TV5" };
		
		frame = new JFrame("ListenToPierce Player");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.getContentPane().setLayout(new BorderLayout());
		
		player = null;
		playerPanel = new JPanel();
		controlPanel = new JPanel();
		
		sources = new JComboBox<String>(sourceList);
		sources.setActionCommand("sourcesList");
		sources.addActionListener(this);
		
		// Establishing the connection to the server and to the JMF stream.
		try
		{
			socket = new Socket("155.246.126.220", 3000);
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e1)
		{
			e1.printStackTrace();
		}
		
		try
		{
			player = Manager.createRealizedPlayer(new MediaLocator("rtp://155.246.126.220:3001/audio"));
			if(player.getControlPanelComponent() != null)
			{
				controls = player.getControlPanelComponent();
				playerPanel.add(controls);
			}
		}
		catch(Exception e)
		{
			System.err.println("Got exception "+ e);
			e.printStackTrace();
		}
		
		controlPanel.add(sources);
		frame.getContentPane().add("Center", playerPanel);
		frame.getContentPane().add("South", controlPanel);
		frame.pack();
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
				if(ois.readObject() instanceof ChangeSourceDatagram)
				{
					player.close();
					player = Manager.createRealizedPlayer(new MediaLocator("rtp://155.246.126.220:3001/audio"));
					if(player.getControlPanelComponent() != null)
					{
						controls = player.getControlPanelComponent();
						playerPanel.add(controls);
						frame.pack();
					}
				}
			} catch (ClassNotFoundException | NoPlayerException
					| CannotRealizeException | IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args)
	{
		new PlayerClient().run();

	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// Sends a ChangeSourceDatagram when the user selects a source from the sources combobox.
		if(e.getActionCommand().equals("sourcesList"))
		{
			try
			{
				oos.writeObject(new ChangeSourceDatagram(sources.getSelectedItem()));
				playerPanel.remove(controls);
				frame.pack();
			} catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
	}

}
