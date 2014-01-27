/**
 *
 */
package javasound;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import server.Server;

public class SourceThread extends Thread
{
	
	private byte[] b;
	private TargetDataLine line;
	private int sourceIndex;
	private SourceDataLine serverOutput;
	private boolean output;
	private Server server;
	
	public SourceThread(Server s, SourceDataLine serverOutput, TargetDataLine t, int sourceIndex)
	{
		super("SourceThread");
		output = false;
		line = t;
		this.serverOutput = serverOutput;
		server = s;
		this.sourceIndex = sourceIndex;
	}
	
	public void run()
	{
		try
		{
			b = new byte[6300];
			line.open(new AudioFormat(44100, 16, 1, true, true), 6300);
			line.start();
			while(true)
			{
				line.read(b, 0, b.length);
				server.writeByteBuffers(b, sourceIndex);
				if(output)
					serverOutput.write(b, 0, b.length);
			}
		} catch (LineUnavailableException e)
		{
			e.printStackTrace();
			System.out.println(sourceIndex);
		}
		finally
		{
			line.stop();
			line.close();
		}
	}
	
	/**
	 * Sets the value of output to newOutput. The output variable specifies whether or not to play the audio of this source
	 * on the server.
	 * @param newOutput The new value for output.
	 */
	public void setOutput(boolean newOutput)
	{
		output = newOutput;
		if(output)
		{
			try
			{
				serverOutput.open();
			} catch (LineUnavailableException e)
			{
				e.printStackTrace();
			}
			serverOutput.start();
		}
		else
		{
			serverOutput.stop();
			serverOutput.close();
		}
	}

}
