//
// Name                 Scott Thompson
// Student ID           S1507806
// Programme of Study   Computing
//
//a class to create objects of type description for the purpose of parsing the description section of the XML file
package com.example.recycler;

public class Title {
    private String title;
    private double magnitude;
    private String location;
    private String date;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    private String time;
}
