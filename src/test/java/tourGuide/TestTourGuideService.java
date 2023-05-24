package tourGuide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import gpsUtil.location.Location;
import org.junit.Test;

import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.dto.AttractionWithDistanceToUser;
import tourGuide.dto.ListOfFiveAttractionsCloseToOneUser;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.GPSUtilService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tripPricer.Provider;

public class TestTourGuideService {

	@Test
	public void getUserLocation() {
		GPSUtilService gpsUtil = new GPSUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user).join();
		tourGuideService.tracker.stopTracking();
		assertTrue(visitedLocation.userId.equals(user.getUserId()));
	}
	
	@Test
	public void addUser() {
		GPSUtilService gpsUtil = new GPSUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);
		
		User retrivedUser = tourGuideService.getUser(user.getUserName());
		User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

		tourGuideService.tracker.stopTracking();
		
		assertEquals(user, retrivedUser);
		assertEquals(user2, retrivedUser2);
	}
	
	@Test
	public void getAllUsers() {
		GPSUtilService gpsUtil = new GPSUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);
		
		List<User> allUsers = tourGuideService.getAllUsers();

		tourGuideService.tracker.stopTracking();
		
		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));
	}
	
	@Test
	public void trackUser() {
		GPSUtilService gpsUtil = new GPSUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user).join();
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(user.getUserId(), visitedLocation.userId);
	}
	
	//@Ignore // Not yet implemented
	@Test
	public void getNearbyAttractions() {
		GPSUtilService gpsUtil = new GPSUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user).join();
		System.out.println("user location is lat : "+ visitedLocation.location.latitude+" long : "+ visitedLocation.location.longitude);
		//List<Attraction> attractions = tourGuideService.getNearByAttractions(visitedLocation);
		ListOfFiveAttractionsCloseToOneUser listObject = tourGuideService.getNearByAttractionsV2(visitedLocation);
		tourGuideService.tracker.stopTracking();
		
		//assertEquals(5, attractions.size());
		assertEquals(5, listObject.getListOfAttractionsCloseToUser().size());
		for(AttractionWithDistanceToUser attraction : listObject.getListOfAttractionsCloseToUser()){
			System.out.println(attraction.getNameOfTouristAttraction()+" "+ attraction.getDistanceInMilesBetweenTheUsersLocationAndThisAttraction());
		}
	}


	@Test
	public void getTripDeals() {
		GPSUtilService gpsUtil = new GPSUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		List<Provider> providers = tourGuideService.getTripDeals(user);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(5, providers.size());
	}

	/*@Test
	public void getUserLocationTest(){
		//ARRANGE
		GPSUtilService gpsUtil = new GPSUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		VisitedLocation lastVisitedLocation = user
		//ACT

		getUserLocation(User user)

		//ASSERT

	}*/

	@Test
	public void  getAllUsersCurrentLocationsTest() {
		//ARRANGE
		GPSUtilService gpsUtil = new GPSUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(3);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		//ACT
		HashMap<String, Location> allUsersCurrentLocationsMap = tourGuideService.getAllUsersCurrentLocations();
		//ASSERT
		assertEquals(3, allUsersCurrentLocationsMap.size());
	}
	@Test
	public void addUserAlreadyInListTest(){
		//ARRANGE
		GPSUtilService gpsUtil = new GPSUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		List <User> listOfUsers = tourGuideService.getAllUsers();
		User user = listOfUsers.get(0);
		//ACT
		tourGuideService.addUser(user);
		//ASSERT
		assertEquals(1, tourGuideService.getAllUsers().size());
	}
	@Test
	public void addNewUserTest(){
		//ARRANGE
		GPSUtilService gpsUtil = new GPSUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		List <User> listOfUsers = tourGuideService.getAllUsers();

		User user = listOfUsers.get(0);

		User newUser = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		//ACT
		tourGuideService.addUser(newUser);
		System.out.println(user.getUserId());
		System.out.println(user.getUserName());
		System.out.println(newUser.getUserId());
		System.out.println(newUser.getUserName());
		//ASSERT
		assertEquals(2, tourGuideService.getAllUsers().size());
		assertTrue(tourGuideService.getAllUsers().contains(newUser));
	}

	
}
