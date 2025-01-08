package com.example.ligtastanim;

public class MonitoringCrops {
    private String name;
    private int imageResId;

    public MonitoringCrops(String name, int imageResId) {
        this.name = name;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }
}
