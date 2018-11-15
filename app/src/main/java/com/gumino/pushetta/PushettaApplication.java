package com.gumino.pushetta;

import java.util.Stack;

import android.app.Application;
import android.util.Log;

import com.gumino.core.contentprovider.PushMessageContentProvider;
import com.gumino.pushetta.core.Helpers;
import com.gumino.pushetta.core.IPushettaServiceClient;
import com.gumino.pushetta.core.PushettaClientFactory;
import com.gumino.pushetta.core.PushettaConsts;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;



public class PushettaApplication extends Application {
	// instance
	private static PushettaApplication instance = null;
	private JobManager jobManager;

	
	public Stack<Integer> readMessagesIds;
	
	public void sendReadMessagesFeedback(){
		int messagesToSend = readMessagesIds.size();
		Integer messagesList[] = new Integer[messagesToSend] ;
		
		for(Integer i = 0; i < messagesToSend; i++){
			messagesList[i] = readMessagesIds.pop();
		}
		
		IPushettaServiceClient client = PushettaClientFactory.getClient();
		client.messageReadFeedback(Helpers.getUniquePsuedoID(), messagesList);
	}

	/**
	 * Convenient accessori, saves having to call and cast
	 * getApplicationContext()
	 */
	public static PushettaApplication getInstance() {
		checkInstance();
		return instance;
	}

	private static void checkInstance() {
		if (instance == null)
			throw new IllegalStateException("Application not created yet!");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// provide an instance for our static accessors
		instance = this;


		configureJobManager();
		
		// Inizializzazione della lista messaggi letti
		readMessagesIds = new Stack<Integer>();
		

		// Cleanup dei push message expired
		int deleted = getContentResolver().delete(PushMessageContentProvider.CONTENT_URI,
				"date(expire) < datetime()", null); // or (expire is null and
												// "anzianita' basata su
												// createdate)
		Log.d("Pushetta", String.format("Deleted %d expired messages", deleted));
		
		 DisplayImageOptions displayDefaultOptions = new DisplayImageOptions.Builder()
		 		.cacheInMemory(true)
		 		.cacheOnDisk(true)
		 		.considerExifParams(true)                         
		 		.build();
		 
		 ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
         		.threadPriority(Thread.NORM_PRIORITY - 2)
         		.denyCacheImageMultipleSizesInMemory()
         		.defaultDisplayImageOptions(displayDefaultOptions)
         		.diskCacheFileNameGenerator (new Md5FileNameGenerator())    
         		.build();
		 
		 ImageLoader.getInstance().init(config);

	}

	
	/**
	 * Configurazione del JobManager per il sistema di code
	 */
	private void configureJobManager() {
		Configuration configuration = new Configuration.Builder(this)
				.minConsumerCount(1)// always keep at least one consumer alive
				.maxConsumerCount(3)// up to 3 consumers at a time
				.loadFactor(3)// 3 jobs per consumer
				.consumerKeepAlive(120)// wait 2 minute
				.build();
		jobManager = new JobManager(this, configuration);
	}

	public JobManager getJobManager() {
		return jobManager;
	}
	


}
