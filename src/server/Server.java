/**
 *
 */
package server;

import java.awt.Color;
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

import javasound.JavasoundManager;
import javasound.SourceThread;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;

public class Server implements ActionListener
{
	
	private ServerSocket serverSocket;
	private ArrayList<ServerThread> threads;
	private JFrame frame;
	private JPanel mainPanel;
	private JTabbedPane tabPane;
	private JScrollPane sp;
	private JComboBox<Source> sourceList;
	private JTextArea area, availableSourcesArea;
	private JButton addSource, removeSource, renameSource, playSource;
	private ArrayList<Source> availableSources;
	private JPanel usersPanel, sourcePanel;
	private JavasoundManager jsm;
	private SourceDataLine serverOutput;
	
	public Server()
	{
		
		AudioFormat format = new AudioFormat(192000, 16, 2, true, true);
		try
		{
			serverOutput = AudioSystem.getSourceDataLine(format);
		} catch (LineUnavailableException e)
		{
			e.printStackTrace();
		}
		
		jsm = new JavasoundManager(this, serverOutput);
			
		availableSources = new ArrayList<Source>();
		
		frame = new JFrame("Pierce Audiocast");
		frame.setPreferredSize(new Dimension(235, 400));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);

		tabPane = new JTabbedPane();
		tabPane.setPreferredSize(new Dimension(225, 400));
		tabPane.setBackground(new Color(54, 201, 153));
		
		sourcePanel = new JPanel();
		sourcePanel.setPreferredSize(new Dimension(225, 400));
		sourcePanel.setBackground(new Color(54, 201, 153));
		
		usersPanel = new JPanel();
		usersPanel.setPreferredSize(new Dimension(225, 400));
		usersPanel.setBackground(new Color(54, 201, 153));
		
		area = new JTextArea();
		area.setEditable(false);
		area.setPreferredSize(new Dimension(175, 175));
		area.setBackground(new Color(127, 219, 207));
		
		sp = new JScrollPane(area);
		sp.setBorder(BorderFactory.createTitledBorder("Users"));
		sp.setBackground(new Color(127, 219, 207));
				
		usersPanel.add(sp);
		
		sourcePanel.add(new JLabel("Sources"));
		
		sourceList = new JComboBox<Source>();
		sourceList.setPrototypeDisplayValue(new Source("XXXXXXXXXXXXXXXXXXXXXX", -1));
		sourceList.setBackground(new Color(127, 219, 207));
		
		sourcePanel.add(sourceList);
		
		addSource = new JButton("Activate Source");
		addSource.addActionListener(this);
		addSource.setActionCommand("add");
		addSource.setBackground(new Color(127, 219, 207));
	
		removeSource = new JButton("Deactivate Source");
		removeSource.addActionListener(this);
		removeSource.setActionCommand("remove");
		removeSource.setBackground(new Color(127, 219, 207));

		
		renameSource = new JButton("Rename Source");
		renameSource.addActionListener(this);
		renameSource.setActionCommand("rename");
		renameSource.setBackground(new Color(127, 219, 207));
		
		playSource = new JButton("Play Source");
		playSource.addActionListener(this);
		playSource.setActionCommand("play");
		playSource.setBackground(new Color(127, 219, 207));
		
		sourcePanel.add(addSource);
		sourcePanel.add(removeSource);
		sourcePanel.add(renameSource);
		sourcePanel.add(playSource);
		
		availableSourcesArea = new JTextArea();
		availableSourcesArea.setPreferredSize(new Dimension(150, 125));
		availableSourcesArea.setBorder(BorderFactory.createTitledBorder("Available Sources"));
		availableSourcesArea.setEditable(false);
		availableSourcesArea.setBackground(new Color(127, 219, 207));
		
		sourcePanel.add(availableSourcesArea);
		
		threads = new ArrayList<>();
		
		if(!readSources())
		{
			ArrayList<SourceThread> threads = jsm.getSourceList();
			for(int i = 0; i < threads.size(); i++)
				sourceList.addItem(new Source("Source "+(i+1), i));
		}
		
		tabPane.addTab("Users", usersPanel);
		tabPane.addTab("Source", sourcePanel);
		
		mainPanel = new JPanel();
		mainPanel.add(new JLabel("Audiocast Server"));
		mainPanel.setBackground(new Color(54, 201, 153));
		mainPanel.add(tabPane);
		
		frame.add(mainPanel);
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
	 * Refreshes the text in available source, sends a source update datagram through each ServerThread, and saves the sources
	 * to the sources.src file.
	 */
	private void sourceUpdate()
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
			sourceUpdate();
			break;
		// Removes the selected source from the available sources list.
		case "remove":
			Source removeSource = (Source)sourceList.getSelectedItem();
			for(int i = 0; i < availableSources.size(); i++)
				if(availableSources.get(i).equals(removeSource))
					availableSources.remove(i);
			sourceUpdate();
			break;
		// Renames the selected source to another name.
		case "rename":
			String newName = JOptionPane.showInputDialog(frame, "New name:                              (no commas)");
			if(newName == null || newName.equals("") || newName.indexOf(',') != -1)
				break;
			for(int i = 0; i < availableSources.size(); i++)
				if(availableSources.get(i).equals(sourceList.getSelectedItem()))
					availableSources.get(i).setName(newName);
			((Source)sourceList.getSelectedItem()).setName(newName);
			sourceList.repaint();
			sourceUpdate();
			break;
		// Plays the selected source, or stops it if it is already playing.
		case "play":
			int index = ((Source)sourceList.getSelectedItem()).getIndex();
			if(playSource.getText() == "Play Source")
			{
				sourceList.setEnabled(false);
				jsm.getSourceList().get(index).setOutput(true);
				playSource.setText("Stop Playing");
			}
			else
			{
				sourceList.setEnabled(true);
				jsm.getSourceList().get(index).setOutput(false);
				playSource.setText("Play Source");
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
				line = br.readLine().split(",");
				Source src = new Source(line[0], Integer.parseInt(line[1]));
				sourceList.addItem(src);
				if(line[2].equals("true"))
					availableSources.add(src);
			}
			br.close();
			sourceUpdate();
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
	
	/**
	 * Saves the sources and if they are available or not to the file sources.src.
	 */
	private void saveSources()
	{
		BufferedWriter bw;
		try
		{
			bw = new BufferedWriter(new FileWriter("sources.src"));
			bw.write("");
			for(int i = 0; i < jsm.getNumSources(); i++)
			{
				Source src = sourceList.getItemAt(i);
				bw.append(src.toString() + "," + src.getIndex());
				boolean found = false;
				for(Source s : availableSources)
					if(s.equals(src))
					{
						bw.append(",true\n");
						found = true;
					}
				if(!found)
					bw.append(",false\n");
			}
			bw.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Iterates over all ServerThreads ands writes the byte array b to each one which has the source specified by
	 * sourceIndex selected.
	 * @param b The byte array to be written.
	 * @param sourceIndex The index of the source which the byte array is from and which specifies which threads should be written to.
	 */
	public void writeByteBuffers(byte[] b, int sourceIndex)
	{
		for(ServerThread thread : threads)
			if(thread.getSourceIndex() == sourceIndex)
				thread.writeByteBuffer(b);
	}

}
