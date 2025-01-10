package com.example.ligtastanim;

public class WateringDate {
    public String date;
    public String status;
    public String rescheduledDate;

    public WateringDate(String date, String status) {
        this.date = date;
        this.status = status;
        this.rescheduledDate = null;
    }

    public WateringDate(String date, String status, String rescheduledDate) {
        this.date = date;
        this.status = status;
        this.rescheduledDate = rescheduledDate;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public String getRescheduledDate() {
        return rescheduledDate;
    }
}