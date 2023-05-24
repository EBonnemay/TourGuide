package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.dto.AttractionWithDistanceToUser;
import tourGuide.dto.ListOfFiveAttractionsCloseToOneUser;
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
	//rajouter un executorService??
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;
	private GPSUtilService gpsUtil;
	private final ExecutorService executorService = Executors.newFixedThreadPool(60);
	
	public TourGuideService(GPSUtilService gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;
		
		if(testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			//créations de users pour le test
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
	//A TESTER
	public VisitedLocation getUserLocation(User user) {

		VisitedLocation visitedLocation = (user.getVisitedLocations().size()>0)?user.getLastVisitedLocation():trackUserLocation(user).join();
		return visitedLocation;

	}

	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}
	
	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
		//converts map to list through a stream of user objects
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
	/*public VisitedLocation trackUserLocation(User user) {
		//les deux lignes ci-dessous ne sont pas nécessaires car elles sont overridées par la ligne 110
		Location location = new Location(generateRandomLatitude(), generateRandomLongitude());
		VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(),location, getRandomTime());
		//vérification???????
		try{
			CompletableFuture<Void> userLocationFuture = gpsUtil.getUserLocation(user, this);
			userLocationFuture.get();
		}catch(NumberFormatException numberFormatException){
			numberFormatException.printStackTrace();
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}*/
	public CompletableFuture<Void> trackAllUserLocation(List<User> users) {

		List<CompletableFuture<VisitedLocation>> completableFutures = users.stream()
				.map(user -> this.trackUserLocation(user))
				.collect(Collectors.toList());
		return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
	}

	public CompletableFuture<VisitedLocation> trackUserLocation(User user) {

		//les deux lignes ci-dessous ne sont pas nécessaires car elles sont overridées par la ligne 110
		CompletableFuture<VisitedLocation> visitedLocationCompletableFuture = CompletableFuture.supplyAsync(() -> {
			VisitedLocation loc = gpsUtil.getUserLocation(user.getUserId());
			return loc;
		}, executorService).thenApplyAsync((loc) -> {
			user.addToVisitedLocations(loc);
			rewardsService.calculateRewards(user).join();
			return loc;
		}, rewardsService.getExecutor());
		return visitedLocationCompletableFuture;
	}



	//en paramètre : un lieu visité; en return : une proposition de listes de lieux à visiter

	//au lieu de renvoyer une lis
	//A TESTER
	/*public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
		List<Attraction> nearbyAttractions = new ArrayList<>();
		for(Attraction attraction : gpsUtil.getListOfAttractions()) {
			if(rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
				nearbyAttractions.add(attraction);
			}
		}
		return nearbyAttractions;
	}*/
	//replace previous by :
	public ListOfFiveAttractionsCloseToOneUser  getNearByAttractionsV2(VisitedLocation visitedLocation){
		ArrayList<AttractionWithDistanceToUser> listOfAttractionsWithDistances = new ArrayList<>();
		List<Attraction> allAttractions = gpsUtil.getListOfAttractions();
		for (Attraction attraction : allAttractions){
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
		ListOfFiveAttractionsCloseToOneUser listOfFiveAttractionsCloseToOneUser = new ListOfFiveAttractionsCloseToOneUser();
		ArrayList<AttractionWithDistanceToUser> listAttributeOfListObject = new ArrayList<>();
		for (int i = 0; i<5 && i <allAttractions.size();i++){
			listAttributeOfListObject.add(listOfAttractionsWithDistances.get(i));
		}
		listOfFiveAttractionsCloseToOneUser.setListOfAttractionsCloseToUser(listAttributeOfListObject);
		return listOfFiveAttractionsCloseToOneUser;
	};

	private void addShutDownHook() {
		//shutdown hook is a thread that gets executed when the JVM shuts down, either normally or abnormally.
		//when jvm shuts down, run method of this thread is executed : it calls the stopTracking method on the tracker Object, causing it to
		// stop tracking user locations and shut down the thread.
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
	//A TESTER
	public HashMap<String, Location> getAllUsersCurrentLocations() {
		HashMap<String, Location> listOfEachUsersMostRecentLocation = new HashMap<>();
		List<User> listOfUsers = getAllUsers();
		//pour chaque utilisateur de listOfUSers
		//récupérer l'id et le nommer
		//aller chercher le dernier lieu du suer avec public VisitedLocation getUserLocation(User user)
		for (User user : listOfUsers) {
			String id = String.valueOf(user.getUserId());
			// UUID idV2 = user.getUserId();//lequel des deux?
			VisitedLocation visitedLocation = getUserLocation(user);
			Location location = visitedLocation.location;
			listOfEachUsersMostRecentLocation.put(id, location);
		}
		return listOfEachUsersMostRecentLocation;
	}

	public boolean isTestMode() {
		return testMode;
	}
}
