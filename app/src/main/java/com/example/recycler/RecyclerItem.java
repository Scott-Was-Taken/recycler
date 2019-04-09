//
// Name                 Scott Thompson
// Student ID           S1507806
// Programme of Study   Computing
//
//this class is used to create item objects for the recycler view to display

package com.example.recycler;

public class RecyclerItem {
    private int imageResource;
    private String text1;
    private String text2;

    public RecyclerItem(int imageResource, String text1, String text2) {
        this.imageResource = imageResource;
        this.text1 = text1;
        this.text2 = text2;
    }
    //get the text for the first text field
    public String getText1() {
        return text1;
    }
    //get the image
    public int getImageResource() {
        return imageResource;
    }
    //get the text for the second text field
    public String getText2() {
        return text2;
    }

}