package tourGuide;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import gpsUtil.location.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.VisitedLocation;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tripPricer.Provider;

@RestController
public class TourGuideController {

	@Autowired
	TourGuideService tourGuideService;
	
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }
    
    @RequestMapping("/getLocation") 
    public String getLocation(@RequestParam String userName) {
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
		//This method serializes the Location object to a JSON string
        //that can be sent over the web
        //as a response to the getLocation request.
        return JsonStream.serialize(visitedLocation.location);
    }
    
    //  TODO: Change this method to no longer return a List of Attractions.
 	//  Instead: Get the closest five tourist attractions to the user - no matter how far away they are.
 	//  Return a new JSON object that contains:
    	// Name of Tourist attraction, > nom de l'attraction
        // Tourist attractions lat/long, > localisation de l'attraction
        // The user's location lat/long, > localisation du user
        // The distance in miles between the user's location and each of the attractions. distance entre user et att
        // The reward points for visiting each Attraction. > points à gagner pour cette attraction
        //    Note: Attraction reward points can be gathered from RewardsCentral
    @RequestMapping("/getNearbyAttractions") 
    public String getNearbyAttractions(@RequestParam String userName) {
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
    	//return JsonStream.serialize(tourGuideService.getNearByAttractions(visitedLocation));
    	return JsonStream.serialize(tourGuideService.getNearByAttractionsV2(visitedLocation));
    }
    
    @RequestMapping("/getRewards") 
    public String getRewards(@RequestParam String userName) {
    	return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
    }
    
    @RequestMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() {
        HashMap<String, Location> listOfEachUsersMostRecentLocation = new HashMap<>();
        List<User> listOfUsers = tourGuideService.getAllUsers();
        //pour chaque utilisateur de listOfUSers
            //récupérer l'id et le nommer
            //aller chercher le dernier lieu du suer avec public VisitedLocation getUserLocation(User user)
        for(User user : listOfUsers){
            String id = String.valueOf(user.getUserId());
           // UUID idV2 = user.getUserId();//lequel des deux?
            VisitedLocation visitedLocation = tourGuideService.getUserLocation(user);
            Location location = visitedLocation.location;
            listOfEachUsersMostRecentLocation.put(id, location);
        }
    	// TODO: Get a list of every user's most recent location as JSON
    	//- Note: does not use gpsUtil to query for their current location, 
    	//        but rather gathers the user's current location from their stored location history.
    	//
    	// Return object should be the just a JSON mapping of userId to Locations similar to:
    	//     {
    	//        "019b04a9-067a-4c76-8817-ee75088c3822": {"longitude":-48.188821,"latitude":74.84371} 
    	//        ...
    	//     }
    	
    	return JsonStream.serialize(listOfEachUsersMostRecentLocation);
    }
    
    @RequestMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String userName) {
    	List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
    	return JsonStream.serialize(providers);
    }
    
    private User getUser(String userName) {
    	return tourGuideService.getUser(userName);
    }
   

}