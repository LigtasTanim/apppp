package com.example.ligtastanim;

public class Officer {
    private String phoneNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String position;

    // Default constructor for Firebase
    public Officer() {}

    // Getters
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPosition() {
        return position;
    }

    // Setters
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    // Add this to your Officer class
    int getPositionRank() {
        if (position == null) return Integer.MAX_VALUE; // Put null positions at the end
        
        switch (position.toLowerCase()) {
            case "president": return 1;
            case "vice president": return 2;
            case "secretary": return 3;
            case "treasurer": return 4;
            case "auditor": return 5;
            case "p.r.o": return 6;
            case "p.i.o": return 7;
            case "sergeant at arms": return 8;
            case "board member": return 9;
            default: return 10;
        }
    }
}