/**
 *
 */
package test;

import java.net.Socket;
import java.io.*;

public class ServerThread extends Thread
{
	
	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private JMFManager jmf;
	private boolean running = true;
	private int index;
	private Server server;
	
	public ServerThread(Socket socket, int index, Server server)
	{
		this.index = index;
		this.socket = socket;
		this.server = server;
		try
		{
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
			jmf = new JMFManager(socket.getInetAddress().toString()+":3001");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public String getInfo()
	{
		return socket.getInetAddress().toString();
	}
	
	public void run()
	{
		jmf.startTransmitting();
		while(running)
		{
			try
			{
				ChangeSourceDatagram gram = (ChangeSourceDatagram) ois.readObject();
				jmf.changeSource(gram.getSource());
				oos.writeObject(new ChangeSourceDatagram("changed"));
			} catch (ClassNotFoundException | IOException e)
			{
				running = false;
			}
		}
		server.removeThread(index);
	}
	
}
