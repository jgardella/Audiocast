/**
 *
 */
package javasound;

import java.util.ArrayList;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import server.Server;

public class JavasoundManager
{
	
	private ArrayList<SourceThread> sourceThreads;
	private int numSources;
	private SourceDataLine serverOutput;
	private Server server;
	
	public JavasoundManager(Server server, SourceDataLine serverOutput)
	{
		sourceThreads = new ArrayList<>();
		this.serverOutput = serverOutput;
		this.server = server;
		findSources();
	}
	
	/**
	 * Finds all audio sources which are to be broadcasted and adds them to the sourceThreads ArrayList. These sources must be
	 * manually named in the Sound section of the Control Panel to the name: "Audiocast #", where # is a number.
	 */
	private void findSources()
	{
		numSources = 0;
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		for(Mixer.Info info : mixerInfos)
		{
			if(info.getName().contains("Audiocast") && !info.getName().contains("Port Audiocast"))
			{
				sourceThreads.add(new SourceThread(server, serverOutput, AudioSystem.getMixer(info), sourceThreads.size()));
				sourceThreads.get(sourceThreads.size()-1).start();
				numSources++;
			}
		}
	}
	
	/**
	 * @return Returns the number of sources.
	 */
	public int getNumSources()
	{
		return numSources;
	}
	
	/**
	 * @return Returns the source list.
	 */
	public ArrayList<SourceThread> getSourceList()
	{
		return sourceThreads;
	}

}
