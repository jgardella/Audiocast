/**
 *
 */
package server;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
	private JTabbedPane panel;
	private JScrollPane sp;
	private JComboBox<Source> sourceList;
	private JTextArea area, availableSourcesArea;
	private JButton addSource, removeSource, renameSource, playSource;
	private ArrayList<Source> availableSources;
	private Player audioPlayer;
	private JPanel usersPanel, sourcePanel;
	
	public Server()
	{
		availableSources = new ArrayList<Source>();
		
		frame = new JFrame("Pierce Audiocast");
		frame.setPreferredSize(new Dimension(235, 350));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		panel = new JTabbedPane();
		panel.setPreferredSize(new Dimension(235, 350));
		
		sourcePanel = new JPanel();
		sourcePanel.setPreferredSize(new Dimension(235, 350));
		
		usersPanel = new JPanel();
		usersPanel.setPreferredSize(new Dimension(235, 350));
		
		area = new JTextArea();
		area.setEditable(false);
		area.setPreferredSize(new Dimension(175, 175));
		
		sp = new JScrollPane(area);
		sp.setBorder(BorderFactory.createTitledBorder("Users"));
				
		usersPanel.add(sp);
		
		sourcePanel.add(new JLabel("Sources"));
		
		sourceList = new JComboBox<Source>();
		
		sourcePanel.add(sourceList);
		
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
		
		sourcePanel.add(addSource);
		sourcePanel.add(removeSource);
		sourcePanel.add(renameSource);
		sourcePanel.add(playSource);
		
		availableSourcesArea = new JTextArea();
		availableSourcesArea.setPreferredSize(new Dimension(150, 125));
		availableSourcesArea.setBorder(BorderFactory.createTitledBorder("Available Sources"));
		
		sourcePanel.add(availableSourcesArea);
		
		threads = new ArrayList<>();
		
		if(!readSources())
		{
			for(int i = 1; i < CaptureDeviceManager.getDeviceList(new AudioFormat(AudioFormat.LINEAR)).size() + 1; i++)
				sourceList.addItem(new Source("Source "+i, i));
		}
		
		panel.addTab("Users", usersPanel);
		panel.addTab("Source", sourcePanel);
		
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
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
		saveSources();
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
	
	/**
	 * Reads the saved sources from the file sources.src, if it exists.
	 * @return Returns true if the sources file exists, false otherwise.
	 */
	private boolean readSources()
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader("sources.src"));
			String[] line;
			while(br.ready())
			{
				line = br.readLine().split(" ");
				Source src = new Source(line[0], Integer.parseInt(line[1]));
				sourceList.addItem(src);
				if(line[2].equals("true"))
					availableSources.add(src);
			}
			br.close();
			refreshAvailableSourceArea();
		}
		catch (FileNotFoundException e1)
		{
			return false;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	private void saveSources()
	{
		BufferedWriter bw;
		try
		{
			bw = new BufferedWriter(new FileWriter("sources.src"));
			bw.write("");
			for(int i = 0; i < CaptureDeviceManager.getDeviceList().size(); i++)
			{
				Source src = sourceList.getItemAt(i);
				bw.append(src.toString() + " " + src.getIndex());
				boolean found = false;
				for(Source s : availableSources)
					if(s.equals(src))
					{
						bw.append(" true\n");
						found = true;
					}
				if(!found)
					bw.append(" false\n");
			}
			bw.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
