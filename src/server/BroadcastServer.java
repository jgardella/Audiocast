/**
 *
 */
package server;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.media.*;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.format.AudioFormat;

public class BroadcastServer
{
	
	private static final Format[] FORMATS = { new AudioFormat(AudioFormat.DVI_RTP) };
	private static final ContentDescriptor CONTENT_DESCRIPTOR = new ContentDescriptor(ContentDescriptor.RAW_RTP);
	
	private MediaLocator mediaLocator;
	private DataSink dataSink;
	private Processor mediaProcessor;
	
	public BroadcastServer(DataSource ds)
	{
			try
			{
				mediaLocator = new MediaLocator("rtp://192.168.1.255:3500/audio");
				mediaProcessor = Manager.createRealizedProcessor(new ProcessorModel(ds, FORMATS, CONTENT_DESCRIPTOR));
				dataSink = Manager.createDataSink(mediaProcessor.getDataOutput(), mediaLocator);
			} catch (NoProcessorException | CannotRealizeException
					| IOException | NoDataSinkException | NotRealizedError e)
			{
				e.printStackTrace();
			}
	}
	
	public void startTransmitting()
	{
		mediaProcessor.start();
		try
		{
			dataSink.open();
			dataSink.start();
		} catch (SecurityException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void stopTransmitting()
	{
		try
		{
			dataSink.stop();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dataSink.close();
		mediaProcessor.stop();
		mediaProcessor.close();
	}
	
	public static void main(String[] args)
	{
		CaptureDeviceManager manager = new CaptureDeviceManager();
		System.out.println(new CaptureDeviceManager().getDeviceList(new AudioFormat(AudioFormat.LINEAR)));
		DataSource source = null;
		File mediaFile = new File("C:/Users/Class2017/Desktop/gamejam/seahorsesong.wav");
		try
		{
			source = Manager.createDataSource(new MediaLocator(mediaFile.toURL()));
		} catch (NoDataSourceException e)
		{
			e.printStackTrace();
		} catch (MalformedURLException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		BroadcastServer server = new BroadcastServer(source);
		server.startTransmitting();
		
	}
	
}
