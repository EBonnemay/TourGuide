package tourGuide.tracker;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tourGuide.service.TourGuideService;
import tourGuide.user.User;

import javax.annotation.Generated;

//extends Thread : this class can be run in the background
public class Tracker extends Thread {
	private Logger logger = LoggerFactory.getLogger(Tracker.class);
	private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
	//tracking every 5 minutes

	//private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	//chercher une classe qui génère les threads de façon dynamique

	private final ExecutorService executorService = Executors.newFixedThreadPool(60);
	//private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	//1000 threads can run at the same time

	private final TourGuideService tourGuideService;
	private boolean stop = false;
	public Tracker(TourGuideService tourGuideService) {
		System.out.println("hi in tracker  constructor with tour guide service parameter");
		this.tourGuideService = tourGuideService;

		//the following line : this Tracker is parameter of submit method of executor service.
		// //By doing that, run() method of Tracker object is executed asynchronously in a separate thread.

		executorService.submit(this);




		System.out.println("after calling executorService");
		System.out.println("this is "+ this.getName());
		System.out.println("Tracker constructor done");


		//this : the tracker object instanciated with this tracker instance
		//step1 : task added to the queue of tasks waiting to be executed by ExecutorService
		//step2 : if worker thread available, it is assigned to execute the task
		//step3 : if no worker thread available, new thread is created (no more than the limit) and assigned to the task
		//step4 : the worker thread executes the run method of the runnable object passed to executorService.submit()
		//step5 : when the run method completes, the worker thread is returned to the pool of available threads to be assigned to another task
		//this process continues until all submitted tasks have been executed, or until the ExecutorService is shut down
	}

	/**
	 * Assures to shut down the Tracker thread
	 */
	public void stopTracking() {
		stop = true;
		executorService.shutdownNow();
	}
	/*méthode principale qui est appelée lorsque le thread est démarré.
	Elle récupère tous les utilisateurs, suit leur localisation,
	mesure le temps passé à suivre leur position
	et met le thread en pause pour un certain temps
	avant de recommencer le suivi de localisation.
	List<User> users = tourGuideService.getAllUsers(); récupère tous les utilisateurs
	et users.forEach(u -> tourGuideService.trackUserLocation(u));
	suit la position de chaque utilisateur en parallèle
	en utilisant les threads disponibles dans le pool de threads.*/
	@Override
	public void run() {
		System.out.println("hi in run method of tracker");
		StopWatch stopWatch = new StopWatch();
		while(true) {
			if(Thread.currentThread().isInterrupted() || stop) {
				logger.debug("Tracker stopping");
				break;
			}

			List<User> users = tourGuideService.getAllUsers();
			logger.debug("Begin Tracker. Tracking " + users.size() + " users.");
			stopWatch.start();
			users.forEach(u -> tourGuideService.trackUserLocation(u));
			stopWatch.stop();
			logger.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
			stopWatch.reset();
			try {
				logger.debug("Tracker sleeping");
				TimeUnit.SECONDS.sleep(trackingPollingInterval);
			} catch (InterruptedException e) {
				break;
			}


		}
		
	}
}
