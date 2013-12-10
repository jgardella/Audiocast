/**
 *
 */
package test;

import java.awt.Dimension;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import javax.swing.*;

public class Server
{
	
	private ServerSocket serverSocket;
	private ArrayList<ServerThread> threads;
	private JFrame frame;
	private JPanel panel;
	private JScrollPane sp;
	private JTextArea area;
	private JLabel usersLabel;
	
	public Server()
	{
		frame = new JFrame("Pierce Audiocast");
		frame.setPreferredSize(new Dimension(400, 250));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(400, 250));
		area = new JTextArea();
		area.setEditable(false);
		area.setPreferredSize(new Dimension(350, 175));
		sp = new JScrollPane(area);
		usersLabel = new JLabel("Users");
		panel.add(usersLabel);
		panel.add(sp);
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
	}
	
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

	public void removeThread(int index)
	{
		threads.remove(index);
		area.setText("");
		for(ServerThread thread : threads)
		{
			area.append(thread.getInfo()+"\n");
		}
	}

}
