/**
 *
 */
package test;

import java.io.Serializable;

public class ChangeSourceDatagram implements Serializable
{
	
	public enum Source {BIG, TV1, TV2, TV3, TV4, TV5};
	private Source source;
	
	public ChangeSourceDatagram(Object object)
	{
		String s = (String) object;
		switch(s)
		{
		case "BigScreen":
			this.source = Source.BIG;
			break;
		case "TV1":
			this.source = Source.TV1;
			break;
		case "TV2":
			this.source = Source.TV2;
			break;
		case "TV3":
			this.source = Source.TV3;
			break;
		case "TV4":
			this.source = Source.TV4;
			break;
		case "TV5":
			this.source = Source.TV5;
			break;
		}
	}
	
	public Source getSource()
	{
		return source;
	}

}
