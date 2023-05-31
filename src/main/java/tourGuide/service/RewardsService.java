package tourGuide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@Service
public class RewardsService {

    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 60;
	private final GPSUtilService gpsUtil;


	private final RewardCentral rewardsCentral;

	private final ExecutorService executorService = Executors.newFixedThreadPool(60);

	//Rewards service class is constructed with gpsUtil and RewardCentral
	
	public RewardsService(GPSUtilService gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
	}
	public RewardCentral getRewardsCentral(){
		return rewardsCentral;
	}
	
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}


	public CompletableFuture<Void> calculateRewards(User user) {
		//make a copy of userLocations (user.getVisitedLocations)
		List<VisitedLocation> userLocations = new ArrayList<>(user.getVisitedLocations());

		List<UserReward> listOfUserRewards = user.getUserRewards();

		//cause erreur :
		//List<VisitedLocation> userLocations = user.getVisitedLocations();

		List<Attraction> attractions = gpsUtil.getListOfAttractions();

		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (Attraction attraction : attractions) {

			for (VisitedLocation visitedLocation : userLocations) {

				if (nearAttraction(visitedLocation, attraction)) {
					//future captures the result of asynchronous task
					CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
						user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
					}, executorService);
					//The task execution will be handled by the executorService, which is an ExecutorService instance passed as an argument.
					futures.add(future);
					break;
				}
			}
		}
		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
	}

	public CompletableFuture<Void> calculateAllRewards(List<User> users) {

		List<CompletableFuture<Void>> completableFutures = users.stream()
				.map(user -> this.calculateRewards(user))
				.collect(Collectors.toList());
		return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
	}
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	//This method checks if a given location is within the attraction proximity range.
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}
	//This method calculates
	// the reward points for a given attraction based on the user's id.
	private int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}

	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
	}

	public Executor getExecutor(){
		return this.executorService;
	}

}
