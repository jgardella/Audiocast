/**
 *
 */
package server;

import java.io.Serializable;

public class ChangeSourceDatagram extends Datagram implements Serializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3912125437089354682L;
	private Source source;
	
	public ChangeSourceDatagram(Source source)
	{
		super("ChangeSource");
		this.source = source;
	}
	
	/**
	 * @return Returns the source that the Datagram is holding.
	 */
	public Source getSource()
	{
		return source;
	}

}
