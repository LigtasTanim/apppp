package com.example.ligtastanim;

public class UpcomingEvent {
    private String title;
    private String date;
    private String description;

    // Required empty constructor for Firebase
    public UpcomingEvent() {}

    public UpcomingEvent(String title, String date, String description) {
        this.title = title;
        this.date = date;
        this.description = description;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
} 