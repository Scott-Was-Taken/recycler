//
// Name                 Scott Thompson
// Student ID           S1507806
// Programme of Study   Computing
//
//a class to create objects of type description for the purpose of parsing the description section of the XML file
//since title class handles the majority of this, we only need to pull the depth and the latlong out of the desc.
package com.example.recycler;

public class Description {
    private int depth;
    private double lat;

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    private double lon;

}
