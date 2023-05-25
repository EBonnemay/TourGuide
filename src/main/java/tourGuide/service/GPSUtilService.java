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


    private final ExecutorService executorService = Executors.newFixedThreadPool(60);
    public GPSUtilService(){
        System.out.println("hi in gpsUtilService constructor");
        gpsUtil = new GpsUtil();
    }
    TourGuideService tourGuideService;
    public VisitedLocation getUserLocation(UUID userId){

        try{
            return gpsUtil.getUserLocation(userId);
        }catch(NumberFormatException numberFormatException){
            numberFormatException.printStackTrace();
        }
        return null;
    }

/*    public void getUserLocation(User user, TourGuideService tourGuideService){
        CompletableFuture.supplyAsync(()->{
            return gpsUtil.getUserLocation(user.getUserId());

        }, executorService).thenAccept(visitedLocation -> {
            tourGuideService.trackUserLocation(user);
        });
    }*/
    /* public void trackUserLocationWithThread(User user) {
        executorService.execute(new Runnable() {
            public void run() {
                trackUserLocation(user);
            }
        });
    }*/
    /*first refactoring
    public void getUserLocation(User user){
        //this CompletableFuture is supplied with a task to execute asynchronously useing supplyAsync
        //The task inside supplyAsync captures the current user's ID and retrieves the user's location from gpsUtil.
        //When the task completes, the result (visitedLocation) is passed to the thenAccept callback.
        //In the thenAccept callback, the trackUserLocation method of tourGuideService
        // is called with the user and the visited location.
        //This allows for the asynchronous execution of getUserLocation and subsequent tracking
        // of the user's location using the CompletableFuture and the specified executorService.

        //In this context, "asynchronously" means that the supplyAsync method
       // will initiate the execution of the provided task in a separate thread,
        //allowing it to run independently in the background while the main thread continues with its execution. This allows for parallel execution of tasks and potential performance improvements.
//each call - for 1 user - creates a new CompletableFuture instance
        //The thenAccept method specifies a callback that will be executed when the previous task
        // (in this case, the task inside supplyAsync) completes.
        CompletableFuture.supplyAsync(()->{
            System.out.println("in getUserLocation");
            return gpsUtil.getUserLocation(user.getUserId());

        }, executorService).thenAccept(visitedLocation -> {
            tourGuideService.trackUserLocation(user);
        });
        //Even though the visitedLocation parameter is not used in the body of the lambda expression,
        // it is still required as part of the Consumer interface.
        // It's simply a placeholder for the result of the CompletableFuture,
        // which is then passed to the trackUserLocation method.
    }
//
    //exéc service crée 1000 threads parallèles
    //completablefuture = gestionnaire de threads, peut lancer et arrêter les threads
    //supplyAsync gère de façon asynchrone
    // dans ces threads : getULoc qui a besoin de executor service qui crée le thread et le rajoute à un autre fil de thread
    //thenAccept
    //visitedLocation résultat de getUserLoc(); une fois qu'on a le result on accepte trackuserLoc
    */
    public List<Attraction> getListOfAttractions(){
        return gpsUtil.getAttractions();
    }

    //refactoring
    /*public CompletableFuture<Void> getUserLocation(User user, TourGuideService tourGuideService){
        //return type enables the caller to wait for the completion of all asynchronous tasks if needed.
        //ensures that all users' locations are tracked before proceeding
        return CompletableFuture.supplyAsync(()->{
            System.out.println("in getUserLocation");
            return gpsUtil.getUserLocation(user.getUserId());

        }, executorService).thenAcceptAsync(visitedLocation -> {
            tourGuideService.trackUserLocation(user);
        }, executorService);
        //Even though the visitedLocation parameter is not used in the body of the lambda expression,
        // it is still required as part of the Consumer interface.
        // It's simply a placeholder for the result of the CompletableFuture,
        // which is then passed to the trackUserLocation method.
    }*/





    //
    //exéc service crée 1000 threads parallèles
    //completablefuture = gestionnaire de threads, peut lancer et arrêter les threads
    //supplyAsync gère de façon asynchrone
    // dans ces threads : getULoc qui a besoin de executor service qui crée le thread et le rajoute à un autre fil de thread
    //thenAccept
    //visitedLocation résultat de getUserLoc(); une fois qu'on a le result on accepte trackuserLoc



}
