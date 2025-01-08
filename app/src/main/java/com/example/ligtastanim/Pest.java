package com.example.ligtastanim;

import java.util.ArrayList;
import java.util.List;

public class Pest {
    private String pestName;
    private String imageUrl;
    private String description1;
    private String description2;
    private List<String> referenceImageUrls;

    public Pest() {
        this.referenceImageUrls = new ArrayList<>();
    }

    public Pest(String pestName, String imageUrl, String description1, String description2) {
        this.pestName = pestName;
        this.imageUrl = imageUrl;
        this.description1 = description1;
        this.description2 = description2;
        this.referenceImageUrls = new ArrayList<>();
    }

    public Pest(String pestName, String imageUrl, String description1, String description2, List<String> referenceImageUrls) {
        this.pestName = pestName;
        this.imageUrl = imageUrl;
        this.description1 = description1;
        this.description2 = description2;
        this.referenceImageUrls = referenceImageUrls != null ? referenceImageUrls : new ArrayList<>();
    }

    public String getPestName() {
        return pestName;
    }

    public void setPestName(String pestName) {
        this.pestName = pestName;
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
