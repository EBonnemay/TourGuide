package tourGuide.dto;

import tourGuide.dto.AttractionWithDistanceToUser;

import java.util.ArrayList;

public class ListOfFiveAttractionsCloseToOneUser {
    ArrayList<AttractionWithDistanceToUser> listOfAttractionsCloseToUser;


    public ArrayList<AttractionWithDistanceToUser> getListOfAttractionsCloseToUser(){
        return listOfAttractionsCloseToUser;
    }
    public void setListOfAttractionsCloseToUser(ArrayList<AttractionWithDistanceToUser> listOfAttractionsCloseToUser){
        this.listOfAttractionsCloseToUser = listOfAttractionsCloseToUser;
    }
}
