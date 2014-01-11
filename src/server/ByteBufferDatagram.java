/**
 *
 */
package server;

public class ByteBufferDatagram extends Datagram
{
	
	private static final long serialVersionUID = 7281662364857642509L;
	private byte[] buffer;

	public ByteBufferDatagram(byte[] b)
	{
		super("ByteBuffer");
		buffer = b;
	}
	
	/**
	 * @return Returns the byte buffer stored in the datagram.
	 */
	public byte[] getBuffer()
	{
		return buffer;
	}

}
