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

    public List<Attraction> getListOfAttractions(){
        return gpsUtil.getAttractions();
    }
}
