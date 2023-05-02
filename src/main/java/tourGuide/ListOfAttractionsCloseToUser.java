package tourGuide;

import java.util.ArrayList;

public class ListOfAttractionsCloseToUser {
    ArrayList<AttractionWithDistanceToUser> listOfAttractionsCloseToUser;

    public ArrayList<AttractionWithDistanceToUser> getListOfAttractionsCloseToUser(){
        return listOfAttractionsCloseToUser;
    }
    public void setListOfAttractionsCloseToUser(ArrayList<AttractionWithDistanceToUser> listOfAttractionsCloseToUser){
        this.listOfAttractionsCloseToUser = listOfAttractionsCloseToUser;
    }
}
