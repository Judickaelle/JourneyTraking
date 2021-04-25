package com.judickaelle.pelletier.journeytracking;

public class Step {
    private String stepTitle;
    private String id_journey;
    private String latitude;
    private String longitude;

    public Step(){
        //empty construtor needed
    }

    public Step(String title, String id_journey, String latitude, String longitude){
        this.stepTitle = title;
        this.id_journey = id_journey;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getStepTitle() {
        return stepTitle;
    }

    public String getId_journey() {
        return id_journey;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
