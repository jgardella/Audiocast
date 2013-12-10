/**
 *
 */
package test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.media.CannotRealizeException;
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

import test.ChangeSourceDatagram.Source;

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
			File mediaFile = new File("C:/Users/Class2017/Desktop/gamejam/seahorsesong.wav");
			ds = Manager.createDataSource(new MediaLocator(mediaFile.toURL()));
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
			e.printStackTrace();
		}
		dataSink.close();
		mediaProcessor.stop();
		mediaProcessor.close();
	}
	
	public void changeSource(Source source)
	{
		stopTransmitting();
		File newSourceFile = null;
		switch(source)
		{
		case BIG:
			newSourceFile = new File("C:/Users/Class2017/Desktop/gamejam/seahorsesong.wav");
			break;
		case TV1:
			newSourceFile = new File("C:/Users/Class2017/Desktop/TV1.wav");
			break;
		case TV2:
			newSourceFile = new File("TV2.wav");
			break;
		case TV3:
			newSourceFile = new File("TV3.wav");
			break;
		case TV4:
			newSourceFile = new File("TV4.wav");
			break;
		case TV5:
			newSourceFile = new File("TV5.wav");
			break;
		}
		try
		{
			ds = Manager.createDataSource(new MediaLocator(newSourceFile.toURL()));
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
