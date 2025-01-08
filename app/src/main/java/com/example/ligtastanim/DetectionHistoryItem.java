package com.example.ligtastanim;

public class DetectionHistoryItem {
    private String classification;
    private long timestamp;
    private String id;

    public DetectionHistoryItem() {
        // Required empty constructor for Firebase
    }

    public DetectionHistoryItem(String classification, long timestamp, String id) {
        this.classification = classification;
        this.timestamp = timestamp;
        this.id = id;
    }

    public String getClassification() {
        return classification;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getId() {
        return id;
    }
}
