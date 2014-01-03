/**
 *
 */
package server;

import java.io.Serializable;

public class Source implements Serializable
{
	
	private static final long serialVersionUID = 139836441366472306L;
	private String name;
	private int index;
	
	public Source(String name, int index)
	{
		this.name = name;
		this.index = index;
	}
	
	public String toString()
	{
		return name;
	}
	
	public int getIndex()
	{
		return index;
	}
	
	public void setName(String newName)
	{
		name = newName;
	}
	
	public boolean equals(Object object)
	{
		if(!(object instanceof Source))
			return false;
		Source otherSource = (Source)object;
		if(otherSource.toString().equals(name))
			return true;
		return false;
	}
	
}
