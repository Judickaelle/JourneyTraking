package com.judickaelle.pelletier.journeytracking;

public class JourneyItem {
    private String title;
    private String secretKey;
    private String owner;

    public JourneyItem(){
        //empty constructor needed
    }

    public JourneyItem(String title, String secretKey, String owner){
        this.title = title;
        this.secretKey = secretKey;
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getOwner() {
        return owner;
    }
}
