package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import tourGuide.user.User;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Service
public class GPSUtilService {
    GpsUtil gpsUtil;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10000);
    public GPSUtilService(){
        gpsUtil = new GpsUtil();
    }
    public VisitedLocation getUserLocation(UUID userId){
        try{
            return gpsUtil.getUserLocation(userId);


        }catch(NumberFormatException numberFormatException){
            numberFormatException.printStackTrace();
        }
        return null;
    }
    public void getUserLocation(User user, TourGuideService tourGuideService){
        CompletableFuture.supplyAsync(()->{
            return gpsUtil.getUserLocation(user.getUserId());

        }, executorService).thenAccept(visitedLocation -> {
            tourGuideService.trackUserLocation(user);
        });
    }
    //exéc service crée 1000 threads parallèles
    //completablefuture = gestionnaire de threads, peut lancer et arrêter les threads
    //supplyAsync gère de façon asynchrone
    // dans ces threads : getULoc qui a besoin de executor service qui crée le thread et le rajoute à un autre fil de thread
    //thenAccept
    //visitedLocation résultat de getUserLoc(); une fois qu'on a le result on accepte trackuserLoc
    public List<Attraction> getListOfAttractions(){
        return gpsUtil.getAttractions();
    }
}
