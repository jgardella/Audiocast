/**
 *
 */
package server;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

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

import server.ChangeSourceDatagram.Source;

/**
 * THE JMFManager class abstracts all of the serverside JMF components into two methods to start and stop transmitting, and one
 * to change the source to be transmitted.
 */
public class JMFManager
{
	
	private static final Format[] FORMATS = { new AudioFormat(AudioFormat.DVI_RTP) };
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
			ds = Manager.createDataSource(((CaptureDeviceInfo)CaptureDeviceManager.getDeviceList(new AudioFormat(AudioFormat.LINEAR, 44100, 16, 2)).firstElement()).getLocator());
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
		MediaLocator newSource = null;
		switch(source)
		{
		case BIG:
			newSource = ((CaptureDeviceInfo)CaptureDeviceManager.getDeviceList(new AudioFormat(AudioFormat.LINEAR, 44100, 16, 2)).firstElement()).getLocator();
			break;
		/*
		case TV1:
			newSource = new File("C:/Users/Class2017/Desktop/TV1.wav");
			break;
		case TV2:
			newSource = new File("TV2.wav");
			break;
		case TV3:
			newSource = new File("TV3.wav");
			break;
		case TV4:
			newSource = new File("TV4.wav");
			break;
		case TV5:
			newSource = new File("TV5.wav");
			break;
			*/
		}
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
