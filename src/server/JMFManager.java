/**
 *
 */
package server;

import java.io.IOException;

import javax.media.CannotRealizeException;
import javax.media.CaptureDeviceManager;
import javax.media.CaptureDeviceInfo;
import javax.media.DataSink;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSinkException;
import javax.media.NoDataSourceException;
import javax.media.NoProcessorException;
import javax.media.NotRealizedError;
import javax.media.Processor;
import javax.media.ProcessorModel;
import javax.media.format.AudioFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;

/**
 * THE JMFManager class abstracts all of the serverside JMF components into two methods to start and stop transmitting, and one
 * to change the source to be transmitted.
 */
public class JMFManager
{
	
	private static final Format[] FORMATS = { new AudioFormat(AudioFormat.LINEAR) };
	private static final ContentDescriptor CONTENT_DESCRIPTOR = new ContentDescriptor(ContentDescriptor.RAW_RTP);
	
	private MediaLocator mediaLocator;
	private DataSink dataSink;
	private Processor mediaProcessor;
	private DataSource ds;
	private String socketInfo;
	
	public JMFManager(String socketInfo)
	{
		this.socketInfo = socketInfo;
		try
		{
			ds = Manager.createDataSource(((CaptureDeviceInfo)CaptureDeviceManager.getDeviceList(new AudioFormat(AudioFormat.LINEAR)).firstElement()).getLocator());
			mediaLocator = new MediaLocator("rtp:/"+socketInfo+"/audio");
			mediaProcessor = Manager.createRealizedProcessor(new ProcessorModel(ds, FORMATS, CONTENT_DESCRIPTOR));
			dataSink = Manager.createDataSink(mediaProcessor.getDataOutput(), mediaLocator);
		} catch (NoProcessorException | CannotRealizeException
				| IOException | NoDataSinkException | NotRealizedError | NoDataSourceException e)
		{
			System.out.println("Error creating JMFManager.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Starts transmitting to the user.
	 */
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
	
	/**
	 * Stops the transmission.
	 */
	public void stopTransmitting()
	{
		try
		{
			dataSink.stop();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		dataSink.close();
		mediaProcessor.stop();
		mediaProcessor.close();
	}
	
	/**
	 * Changes the source to be transmitted and restarts the transmission.
	 * @param source The new source to be transmitted.
	 */
	public void changeSource(Source source)
	{
		stopTransmitting();
		if(source.getIndex() > 0)
		{
			MediaLocator newSource = ((CaptureDeviceInfo)CaptureDeviceManager.getDeviceList(new AudioFormat(AudioFormat.LINEAR)).get(source.getIndex()-1)).getLocator();
			try
			{
				ds = Manager.createDataSource(newSource);
				mediaLocator = new MediaLocator("rtp:/"+socketInfo+"/audio");
				mediaProcessor = Manager.createRealizedProcessor(new ProcessorModel(ds, FORMATS, CONTENT_DESCRIPTOR));
				dataSink = Manager.createDataSink(mediaProcessor.getDataOutput(), mediaLocator);
			} catch (NoDataSourceException | IOException | NoDataSinkException | NotRealizedError | NoProcessorException | CannotRealizeException e)
			{
				System.out.println("Exception changing source.");
				e.printStackTrace();
			}
			startTransmitting();
		}
	}

}
