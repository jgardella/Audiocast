/**
 *
 */
package javasound;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import server.Server;

public class SourceThread extends Thread
{
	
	private byte[] b;
	private Mixer source;
	private int sourceIndex;
	private SourceDataLine serverOutput;
	private boolean output;
	private Server server;
	
	public SourceThread(Server s, SourceDataLine serverOutput, Mixer m, int sourceIndex)
	{
		super("SourceThread");
		output = false;
		source = m;
		this.serverOutput = serverOutput;
		server = s;
		this.sourceIndex = sourceIndex;
	}
	
	public void run()
	{
		try
		{
			TargetDataLine line = (TargetDataLine) AudioSystem.getLine(source.getTargetLineInfo()[0]);
			System.out.println(line.getFormat());
			b = new byte[line.getBufferSize() / 50];
			line.open(new AudioFormat(44100, 16, 2, true, false));
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
