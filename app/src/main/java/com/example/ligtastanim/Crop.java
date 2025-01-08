package com.example.ligtastanim;

import java.util.ArrayList;
import java.util.List;

public class Crop {
    private String cropName;
    private String imageUrl;
    private String description1;
    private String description2;
    private List<String> referenceImageUrls;

    public Crop() {
        this.referenceImageUrls = new ArrayList<>();
    }

    public Crop(String cropName, String imageUrl, String description1, String description2) {
        this.cropName = cropName;
        this.imageUrl = imageUrl;
        this.description1 = description1;
        this.description2 = description2;
        this.referenceImageUrls = new ArrayList<>();
    }

    public Crop(String cropName, String imageUrl, String description1, String description2, List<String> referenceImageUrls) {
        this.cropName = cropName;
        this.imageUrl = imageUrl;
        this.description1 = description1;
        this.description2 = description2;
        this.referenceImageUrls = referenceImageUrls != null ? referenceImageUrls : new ArrayList<>();
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription1() {
        return description1;
    }

    public void setDescription1(String description1) {
        this.description1 = description1;
    }

    public String getDescription2() {
        return description2;
    }

    public void setDescription2(String description2) {
        this.description2 = description2;
    }

    public List<String> getReferenceImageUrls() {
        return referenceImageUrls;
    }

    public void setReferenceImageUrls(List<String> referenceImageUrls) {
        this.referenceImageUrls = referenceImageUrls != null ? referenceImageUrls : new ArrayList<>();
    }

    public void addReferenceImageUrl(String url) {
        if (this.referenceImageUrls == null) {
            this.referenceImageUrls = new ArrayList<>();
        }
        this.referenceImageUrls.add(url);
    }
}
