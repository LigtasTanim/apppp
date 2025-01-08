package com.example.ligtastanim;

import java.util.ArrayList;
import java.util.List;

public class Disease {
    private String diseaseName;
    private String imageUrl;
    private String description;
    private List<String> referenceImageUrls;

    public Disease() {
        this.referenceImageUrls = new ArrayList<>();
    }

    public Disease(String diseaseName, String imageUrl, String description) {
        this.diseaseName = diseaseName;
        this.imageUrl = imageUrl;
        this.description = description;
        this.referenceImageUrls = new ArrayList<>();
    }

    public Disease(String diseaseName, String imageUrl, String description, List<String> referenceImageUrls) {
        this.diseaseName = diseaseName;
        this.imageUrl = imageUrl;
        this.description = description;
        this.referenceImageUrls = referenceImageUrls != null ? referenceImageUrls : new ArrayList<>();
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
