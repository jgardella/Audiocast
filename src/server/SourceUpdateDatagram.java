/**
 *
 */
package server;

import java.io.Serializable;
import java.util.ArrayList;

public class SourceUpdateDatagram extends Datagram implements Serializable
{
	
	private static final long serialVersionUID = 6981281332363894267L;
	private Source[] sourceList;

	public SourceUpdateDatagram(ArrayList<Source> sources)
	{
		super("SourceUpdate");
		sourceList = new Source[sources.size()];
		for(int i = 0; i < sources.size(); i++)
			sourceList[i] = sources.get(i);
	}
	
	/**
	 * @return Returns the list of available sources stored in the datagram.
	 */
	public Source[] getAvailableSources()
	{
		return sourceList;
	}

}
