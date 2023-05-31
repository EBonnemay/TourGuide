package tourGuide;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

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

public class TestRewardsService {

	@Test

	public void userGetRewards() {
		//ARRANGE
		GPSUtilService gpsUtil = new GPSUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		tourGuideService.tracker.stopTracking();
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Attraction attraction = gpsUtil.getListOfAttractions().get(0);
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		tourGuideService.trackUserLocation(user).join();

		//ACT
		List<UserReward> userRewards = user.getUserRewards();


		//ASSERT
		assertTrue(userRewards.size() == 1);
	}
	
	@Test
	public void isWithinAttractionProximity() {
		GPSUtilService gpsUtil = new GPSUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		Attraction attraction = gpsUtil.getListOfAttractions().get(0);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}
	
	@Test
	public void nearAllAttractions() {
		GPSUtilService gpsUtil = new GPSUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);

		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		tourGuideService.tracker.stopTracking();

		rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0)).join();

		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));

		assertEquals(gpsUtil.getListOfAttractions().size(), userRewards.size());
	}
	@Test
	public void addUserRewardsTest(){
		//ARRANGE
		GPSUtilService gpsUtil = new GPSUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		tourGuideService.tracker.stopTracking();
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Attraction attraction = gpsUtil.getListOfAttractions().get(0);

		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		assertTrue(user.getUserRewards().isEmpty());
		//ACT
		user.addUserReward(new UserReward(visitedLocation, attraction, 4));
		assertEquals(1, user.getUserRewards().size());

		//ASSERT
	}
	@Test
	public void addUserRewardsIfAlreadyExistsTest(){
		//ARRANGE
		GPSUtilService gpsUtil = new GPSUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		tourGuideService.tracker.stopTracking();
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Attraction attraction = gpsUtil.getListOfAttractions().get(0);

		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addUserReward(new UserReward(visitedLocation, attraction, 4));

		UserReward reward = user.getUserRewards().get(0);

		user.addUserReward(reward);
		assertEquals(1, user.getUserRewards().size());
	}
}
