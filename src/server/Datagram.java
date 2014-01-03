/**
 *
 */
package server;

import java.io.Serializable;

public abstract class Datagram implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7522387707887028289L;
	private String type;
	
	public Datagram(String type)
	{
		this.type = type;
	}
	
	public String getType()
	{
		return type;
	}
	
}
