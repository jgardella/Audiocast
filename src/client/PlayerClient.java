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

import test.ChangeSourceDatagram;

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
		frame = new JFrame("ListenToPierce Player");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.getContentPane().setLayout(new BorderLayout());
		player = null;
		playerPanel = new JPanel();
		try
		{
			socket = new Socket("155.246.126.220", 3000);
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e1)
		{
			e1.printStackTrace();
		}
		String[] sourceList = { "BigScreen", "TV1", "TV2", "TV3", "TV4", "TV5" };
		controlPanel = new JPanel();
		sources = new JComboBox<String>(sourceList);
		sources.addActionListener(this);
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
		new PlayerClient();

	}

	@Override
	public void actionPerformed(ActionEvent e)
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
