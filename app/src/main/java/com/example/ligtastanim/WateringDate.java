package com.example.ligtastanim;

public class WateringDate {
    public String date;
    public String status;

    public WateringDate() {
    }

    public WateringDate(String date, String status) {
        this.date = date;
        this.status = status;
    }
    public String getStatus() {
        return status; 
    }

    public String getDate() {
        return date;
    }
}