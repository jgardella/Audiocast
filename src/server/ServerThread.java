package server;

import java.net.Socket;
import java.io.*;

public class ServerThread extends Thread
{
	
	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private boolean running = true;
	private int serverThreadIndex, sourceIndex;
	private Server server;
	
	public ServerThread(Socket socket, int index, Server server)
	{
		this.serverThreadIndex = index;
		this.socket = socket;
		this.server = server;
		sourceIndex = -1;
		try
		{
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @return Returns the ip address of the client who is connected to this thread.
	 */
	public String getInfo()
	{
		return socket.getInetAddress().toString();
	}
	
	/**
	 *  Each thread waits to hear a ChangeSourceDatagram from its client. Once received, it changes its JMFManager's source
	 *  accordingly and then sends back a Datagram to indicate that the source has been changed.
	 */
	public void run()
	{
		try
		{
			oos.writeObject(new SourceUpdateDatagram(server.getAvailableSources()));
		} catch (IOException e1)
		{
			e1.printStackTrace();
		}
		while(running)
		{
			try
			{
				ChangeSourceDatagram gram = (ChangeSourceDatagram) ois.readObject();
				sourceIndex = gram.getSource().getIndex();
			} catch (ClassNotFoundException | IOException e)
			{
				running = false;
			}
		}
		try
		{
			socket.close();
			server.removeThread(serverThreadIndex);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends a SourceUpdateDatagram to the thread's client, which contains a list of the available sources.
	 */
	public void sendSourceUpdate()
	{
		try
		{
			oos.writeObject(new SourceUpdateDatagram(server.getAvailableSources()));
			oos.reset();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Puts the byte buffer specified by b into a ByteBufferDatagram and writes it to the thread's client.
	 * @param b The byte buffer to be written to the thread's client.
	 */
	public void writeByteBuffer(byte[] b)
	{
		try
		{
			oos.writeObject(new ByteBufferDatagram(b));
			oos.reset();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @return Returns the thread's sourceIndex, which specifies which source the client has selected.
	 */
	public int getSourceIndex()
	{
		return sourceIndex;
	}
	
}
