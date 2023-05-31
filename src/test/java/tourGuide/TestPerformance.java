package tourGuide;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Ignore;
import org.junit.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.GPSUtilService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import static org.junit.Assert.*;

public class TestPerformance {
	

	@Test
	public void highVolumeTrackLocation() {
		GPSUtilService gpsUtil = new GPSUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(100000);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		tourGuideService.tracker.stopTracking();

		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();

		for(User u : allUsers){
			assertEquals(3, u.getVisitedLocations().size());
		}

	    StopWatch stopWatch = new StopWatch();
		stopWatch.start(); // chrono started

		tourGuideService.trackAllUserLocation(allUsers).join();
		assertEquals(100000, tourGuideService.getAllUsers().size());
		stopWatch.stop(); // chrono stopped

		for(User u : allUsers){
			assertEquals(4, u.getVisitedLocations().size());
		}


		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");

		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
	
	//@Ignore
	@Test
	public void highVolumeGetRewards() {
		//ARRANGE
		GPSUtilService gpsUtil = new GPSUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		InternalTestHelper.setInternalUserNumber(100000);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		tourGuideService.tracker.stopTracking();

		Attraction attraction = gpsUtil.getListOfAttractions().get(0);
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		//ACT
		allUsers.forEach(u -> {
			u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date()));
		});
		rewardsService.calculateAllRewards(allUsers).join();
		/** this is another option
		try {
			TimeUnit.SECONDS.sleep(10);
		} catch (InterruptedException e) {
			System.out.println("interrupted exception");
			throw new RuntimeException();
		}*/
		//ASSERT
		for(User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		stopWatch.stop();
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
	
}
