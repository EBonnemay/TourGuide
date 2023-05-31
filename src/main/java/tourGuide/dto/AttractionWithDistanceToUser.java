package tourGuide.dto;

import gpsUtil.location.Location;

public class AttractionWithDistanceToUser {
    String nameOfTouristAttraction;

    Location locationOfTouristAttraction;
    Location locationOfUserCloseToAttraction;

    Double distanceInMilesBetweenTheUsersLocationAndThisAttraction;

    int rewardsPointsForVisitingThisAttraction;

    public AttractionWithDistanceToUser (String nameOfTouristAttraction, Location locationOfTouristAttraction, Location locationOfUserCloseToAttraction, Double distanceInMilesBetweenTheUsersLocationAndThisAttraction, int rewardsPointsForVisitingThisAttraction ){
        this.nameOfTouristAttraction=nameOfTouristAttraction;
        this.locationOfTouristAttraction=locationOfTouristAttraction;
        this.locationOfUserCloseToAttraction=locationOfUserCloseToAttraction;
        this.distanceInMilesBetweenTheUsersLocationAndThisAttraction = distanceInMilesBetweenTheUsersLocationAndThisAttraction;
    }
    public AttractionWithDistanceToUser(){

    }
    public void setNameOfTouristAttraction(String name){
        this.nameOfTouristAttraction = name;
    }
    public void setLocationOfTouristAttraction(Location location){
        this.locationOfTouristAttraction = location;
    }
    public void setLocationOfUserCloseToAttraction(Location location){
        this.locationOfUserCloseToAttraction = location;
    }
    public void setDistanceInMilesBetweenTheUsersLocationAndThisAttraction(double distance){
        this.distanceInMilesBetweenTheUsersLocationAndThisAttraction=distance;
    }
    public void setRewardsPointsForVisitingThisAttraction(int points){
        this.rewardsPointsForVisitingThisAttraction = points;
    }
    public String getNameOfTouristAttraction(){
        return nameOfTouristAttraction;
    }
    public Location getLocationOfTouristAttraction(){
        return locationOfTouristAttraction;
    }
    public Location getLocationOfUserCloseToAttraction(){
        return locationOfUserCloseToAttraction;
    }
    public double getDistanceInMilesBetweenTheUsersLocationAndThisAttraction(){
        return distanceInMilesBetweenTheUsersLocationAndThisAttraction;
    }
    public int getRewardsPointsForVisitingThisAttraction(){
        return rewardsPointsForVisitingThisAttraction;
    }
    // Name of Tourist attraction, > nom de l'attraction
    // Tourist attractions lat/long, > localisation de l'attraction
    // The user's location lat/long, > localisation du user
    // The distance in miles between the user's location and each of the attractions. distance entre user et att
    // The reward points for visiting each Attraction. > points à gagner pour cette attraction
    //    Note: Attraction reward points can be gathered from RewardsCentral
}
