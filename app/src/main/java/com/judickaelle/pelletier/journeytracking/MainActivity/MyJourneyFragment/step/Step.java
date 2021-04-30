package com.judickaelle.pelletier.journeytracking.MainActivity.MyJourneyFragment.step;

public class Step {
    private String stepTitle;
    private String id_journey;
    private String latitude;
    private String longitude;
    private int stepNumber;

    public Step(){
        //empty construtor needed
    }

    public Step(String stepTitle, String id_journey, String latitude, String longitude, int stepNumber){
        this.stepTitle = stepTitle;
        this.id_journey = id_journey;
        this.latitude = latitude;
        this.longitude = longitude;
        this.stepNumber = stepNumber;
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

    public int getStepNumber() { return stepNumber; }
}
