package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.AttractionWithDistanceToUser;
import tourGuide.ListOfAttractionsCloseToUser;
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;
	
	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;
		
		if(testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}
	// renvoie les récompenses de l'utilisateur passé en paramètre.
	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}
	//renvoie la localisation de l'utilisateur passée en paramètre
	// en utilisant la méthode trackUserLocation pour la récupérer
	// s'il n'y a pas de localisations enregistrées pour l'utilisateur.

	/**
	 * equivalent de getUserLocation
	 * VisitedLocation visitedLocation;
	 * if (user.getVisitedLocations().size() > 0) {
	 *     visitedLocation = user.getLastVisitedLocation();
	 * } else {
	 *     visitedLocation = trackUserLocation(user);
	 * }
	 */
	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
			user.getLastVisitedLocation() :
			trackUserLocation(user);
		return visitedLocation;

	}

	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}
	
	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}
	
	public void addUser(User user) {
		if(!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}
	//renvoie une liste d'offres de voyage pour l'utilisateur passée en paramètre.
	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(), 
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}
	//Cette méthode permet de suivre la position de l'utilisateur
	// et d'ajouter la dernière position visitée à sa liste de lieux visités.
	// Elle utilise le service GPS pour obtenir la position actuelle de l'utilisateur
	// et ajoute cette position à sa liste de positions visitées
	// en appelant la méthode "addToVisitedLocations" de l'utilisateur.
	// Ensuite, elle utilise le service de récompenses
	// pour calculer les récompenses potentielles que l'utilisateur pourrait recevoir
	// en visitant cette position.
	//Enfin, elle retourne la dernière position visitée.
	public VisitedLocation trackUserLocation(User user) {
		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}
	//en paramètre : un lieu visité; en return : une proposition de listes de lieux à visiter

	//au lieu de renvoyer une lis
	public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
		List<Attraction> nearbyAttractions = new ArrayList<>();
		for(Attraction attraction : gpsUtil.getAttractions()) {
			if(rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
				nearbyAttractions.add(attraction);
			}
		}
		return nearbyAttractions;
	}
	//replace previous by :
	public ListOfAttractionsCloseToUser  getNearByAttractionsV2(VisitedLocation visitedLocation){
		ArrayList<AttractionWithDistanceToUser> listOfAttractionsWithDistances = new ArrayList<>();
		double distance = 0;
		List<Attraction> listOfAttractions = gpsUtil.getAttractions();
		for (Attraction attraction : gpsUtil.getAttractions()){
			Location attractionLocation = new Location(attraction.latitude, attraction.longitude);
			Location locationOfVisitedLocation = new Location(visitedLocation.location.latitude, visitedLocation.location.longitude);
			double foundDistance = rewardsService.getDistance(locationOfVisitedLocation, attractionLocation);
			AttractionWithDistanceToUser attractionWithDistanceToUser = new AttractionWithDistanceToUser();
			attractionWithDistanceToUser.setNameOfTouristAttraction(attraction.attractionName);
			attractionWithDistanceToUser.setLocationOfTouristAttraction(attractionLocation);
			attractionWithDistanceToUser.setLocationOfUserCloseToAttraction(locationOfVisitedLocation);
			attractionWithDistanceToUser.setDistanceInMilesBetweenTheUsersLocationAndThisAttraction(foundDistance);
			listOfAttractionsWithDistances.add(attractionWithDistanceToUser);
		}
		Comparator<AttractionWithDistanceToUser> byDistance = Comparator.comparing(AttractionWithDistanceToUser::getDistanceInMilesBetweenTheUsersLocationAndThisAttraction);
		Collections.sort(listOfAttractionsWithDistances, byDistance);
		ListOfAttractionsCloseToUser listOfAttractionsCloseToUser= new ListOfAttractionsCloseToUser();
		//listOfAttractionsCloseToUser.get
		//aller chercher la liste d'attractions closetouser dans l'objet list of attractionsclose...
		ArrayList<AttractionWithDistanceToUser> listAttributeOfListObject = new ArrayList<>();

		listAttributeOfListObject.add(listOfAttractionsWithDistances.get(0));
		listAttributeOfListObject.add(listOfAttractionsWithDistances.get(1));
		listAttributeOfListObject.add(listOfAttractionsWithDistances.get(2));
		listAttributeOfListObject.add(listOfAttractionsWithDistances.get(3));
		listAttributeOfListObject.add(listOfAttractionsWithDistances.get(4));
		listOfAttractionsCloseToUser.setListOfAttractionsCloseToUser(listAttributeOfListObject);


		//dans cette liste ajouter les objets 0 à 4 de la list of attractions with disrtances
		//set la liste dans l'objet listofattr
		//retourner l'objet

		return listOfAttractionsCloseToUser;
	};

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() { 
		      public void run() {
		        tracker.stopTracking();
		      } 
		    }); 
	}
	
	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();
	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);
			
			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}
	
	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i-> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}
	
	private double generateRandomLongitude() {
		double leftLimit = -180;
	    double rightLimit = 180;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
	    double rightLimit = 85.05112878;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
	    return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}
	
}
