/**
 *
 */
package server;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import javax.media.cdm.CaptureDeviceManager;
import javax.media.CannotRealizeException;
import javax.media.CaptureDeviceInfo;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.format.AudioFormat;
import javax.swing.*;

public class Server implements ActionListener
{
	
	private ServerSocket serverSocket;
	private ArrayList<ServerThread> threads;
	private JFrame frame;
	private JPanel panel;
	private JScrollPane sp;
	private JComboBox<Source> sourceList;
	private JTextArea area, availableSourcesArea;
	private JLabel usersLabel;
	private JButton addSource, removeSource, renameSource, playSource;
	private ArrayList<Source> availableSources;
	private Player audioPlayer;
	
	public Server()
	{
		frame = new JFrame("Pierce Audiocast");
		frame.setPreferredSize(new Dimension(235, 500));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(235, 500));
		
		area = new JTextArea();
		area.setEditable(false);
		area.setPreferredSize(new Dimension(175, 175));
		
		sp = new JScrollPane(area);
		
		usersLabel = new JLabel("Users");
		
		panel.add(usersLabel);
		panel.add(sp);
		panel.add(new JLabel("Sources"));
		
		sourceList = new JComboBox<Source>();
		for(int i = 1; i < CaptureDeviceManager.getDeviceList(new AudioFormat(AudioFormat.LINEAR)).size() + 1; i++)
			sourceList.addItem(new Source("Source "+i, i));
		
		panel.add(sourceList);
		
		addSource = new JButton("Activate Source");
		addSource.addActionListener(this);
		addSource.setActionCommand("add");
		
		removeSource = new JButton("Deactivate Source");
		removeSource.addActionListener(this);
		removeSource.setActionCommand("remove");
		
		renameSource = new JButton("Rename Source");
		renameSource.addActionListener(this);
		renameSource.setActionCommand("rename");
		
		playSource = new JButton("Play Source");
		playSource.addActionListener(this);
		playSource.setActionCommand("play");
		
		panel.add(addSource);
		panel.add(removeSource);
		panel.add(renameSource);
		panel.add(playSource);
		
		availableSourcesArea = new JTextArea();
		availableSourcesArea.setPreferredSize(new Dimension(150, 125));
		
		panel.add(new JLabel("Active Sources"));
		panel.add(availableSourcesArea);
		
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		
		availableSources = new ArrayList<Source>();
	}
	
	/**
	 * The Server listens for connections on its serverSocket. When it receives a connection, it creates a new 
	 * ServerThread to handle it, and adds the thread to its ArrayList threads in order to track it.
	 */
	public void run()
	{
		try
		{
			serverSocket = new ServerSocket(3000);
			threads = new ArrayList<>();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		while(true)
		{
			try
			{
				threads.add(new ServerThread(serverSocket.accept(), threads.size(), this));
				threads.get(threads.size() - 1).start();
				area.append(threads.get(threads.size() - 1).getInfo());
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args)
	{
		new Server().run();
	}
	
	/**
	 * Removes the thread at index from the threads ArrayList.
	 * @param index The index of the thread to be removed.
	 */
	public void removeThread(int index)
	{
		threads.remove(index);
		area.setText("");
		for(ServerThread thread : threads)
		{
			area.append(thread.getInfo()+"\n");
		}
	}
	
	/**
	 * Refreshes the text in available sources and sends a source update datagram to through each serverthread.
	 */
	private void refreshAvailableSourceArea()
	{
		availableSourcesArea.setText("");
		for(Source s : availableSources)
			availableSourcesArea.append(s+"\n");
		for(ServerThread thread : threads)
			thread.sendSourceUpdate();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		switch(e.getActionCommand())
		{
		// Adds the selected source to the available sources list.
		case "add":
			Source addSource = (Source)sourceList.getSelectedItem();
			for(Source s : availableSources)
				if(s.equals(addSource))
					return;
			availableSources.add(addSource);
			refreshAvailableSourceArea();
			break;
		// Removes the selected source from the available sources list.
		case "remove":
			Source removeSource = (Source)sourceList.getSelectedItem();
			for(int i = 0; i < availableSources.size(); i++)
				if(availableSources.get(i).equals(removeSource))
					availableSources.remove(i);
			refreshAvailableSourceArea();
			break;
		// Renames the selected source to another name.
		case "rename":
			String newName = JOptionPane.showInputDialog(frame, "New name:");
			if(newName == null || newName.equals(""))
				break;
			for(int i = 0; i < availableSources.size(); i++)
				if(availableSources.get(i).equals(sourceList.getSelectedItem()))
					availableSources.get(i).setName(newName);
			((Source)sourceList.getSelectedItem()).setName(newName);
			sourceList.repaint();
			refreshAvailableSourceArea();
			break;
		// Plays the selected source, or stops it if it is already playing.
		case "play":
			try
			{
				if(playSource.getText() == "Play Source")
				{
					int index = ((Source)sourceList.getSelectedItem()).getIndex();
					MediaLocator source = ((CaptureDeviceInfo)CaptureDeviceManager.getDeviceList(new AudioFormat(AudioFormat.LINEAR)).get(index-1)).getLocator();
					audioPlayer = Manager.createRealizedPlayer(source);
					audioPlayer.start();
					playSource.setText("Stop Playing");
				}
				else
				{
					audioPlayer.stop();
					audioPlayer.close();
					playSource.setText("Play Source");
				}
			} catch (NoPlayerException | CannotRealizeException | IOException e1)
			{
				e1.printStackTrace();
			}
			break;
		}
	}
	
	/**
	 * @return Returns the available sources list.
	 */
	public ArrayList<Source> getAvailableSources()
	{
		return availableSources;
	}

}
