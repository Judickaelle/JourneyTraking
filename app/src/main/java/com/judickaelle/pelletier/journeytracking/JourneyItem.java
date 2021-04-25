package com.judickaelle.pelletier.journeytracking;

public class JourneyItem {
    private String title;
    private String owner;

    public JourneyItem(){
        //empty constructor needed
    }

    public JourneyItem(String title, String owner){
        this.title = title;
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public String getOwner() {
        return owner;
    }
}
