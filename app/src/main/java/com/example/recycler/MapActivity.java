
//
// Name                 Scott Thompson
// Student ID           S1507806
// Programme of Study   Computing
//

package com.example.recycler;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback{
    public void startProgress() {
        // Run network access on a separate thread;
        new Thread(new MapActivity.Task(urlSource)).start();
    } //

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;
        //dump markers on the map using the results from XML parsing


    }

    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private String result;
    private String urlSource = "http://quakes.bgs.ac.uk/feeds/MhSeismology.xml";
    double ukLat=55.3781;
    double ukLon=-2.3360;
    LatLng ukLatLon = new LatLng(ukLat, ukLon);

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private List<RecyclerItem> markerList;
    String resultString;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        configureBackButton();
        getLocationPermission();
        initMap();
        startProgress();

    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        //assume that permission is not granted
        mLocationPermissionsGranted = false;
        //check requestcode
        switch(requestCode){
            //if a permission is granted
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        //if permission is not granted
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }


    //configure the button that takes you back to main activity
    private void configureBackButton(){
        Button nextButton = (Button) findViewById(R.id.BackButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //a method to parse a string of xml data and return an arraylist of item objects
    ArrayList<Item> parseXML(String stream) {
        //remove the null from the start of the stream?!?! WAIT WHAT THAT ACTUALLY WORKED?! THIS TOOK ME FOREVER TO FIGURE OUT.
        stream = stream.replaceFirst("null", "");
        //give the first title tag a different name
        //stream = stream.replaceFirst("title", "heading");
        //stream = stream.replaceFirst("title", "heading");
        Log.v("ParseXML running on", stream);
        //convert the result string into an input stream
        InputStream streams = new ByteArrayInputStream(stream.getBytes(Charset.forName("UTF-8")));
        XmlPullParserFactory parserFactory;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            //InputStream is = getAssets().open(urlSource);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            //parser.setInput(is,null);
            parser.setInput(streams, null);
            return processParsing(parser);
        } catch (XmlPullParserException | IOException ignored) {
            Toast.makeText(MapActivity.this,
                    "the map be crashing boys", Toast.LENGTH_LONG).show();
        }
        return null;
    }

    private ArrayList<Item> processParsing(XmlPullParser parser) throws XmlPullParserException, IOException {
        //arraylist items contains parsed item objects
        ArrayList<Item> items = new ArrayList<>();
        int eventType = parser.getEventType();
        Item currentItem = null;
        //while we are not at the end of the XML
        while (eventType != XmlPullParser.END_DOCUMENT) {
            //eltName holds the name of the xml tag
            String eltName;
            //
            switch (eventType) {
                case (XmlPullParser.START_TAG):
                    //get the element name
                    eltName = parser.getName();
                    //if an item tag is found then we have found the start of an item object
                    if ("item".equals(eltName)) {
                        Log.d("Item", "---------------ITEM FOUND---------------");
                        //create a new item object
                        currentItem = new Item();
                        items.add(currentItem);
                    }
                    //else, as long as an item exists we are currently parsing it
                    //(when currentItem is null, we arent looking at an item we're looking at anything above it)
                    else if (currentItem != null) {
                        //check the element name and set any matches to the currentItem object
                        if ("title".equals(eltName)) {
                            currentItem.setTitle(parseTitle(parser.nextText()));
                            Log.i("title", currentItem.getTitle().getTitle());
                        } else if ("description".equals(eltName)) {
                            currentItem.setDescription(parseDesc(parser.nextText()));
                            //Log.i("description DateTime", currentItem.getDescription().getDateTime());
                        } else if ("link".equals(eltName)) {
                            currentItem.setLink(parser.nextText());
                            Log.i("link", currentItem.getLink());
                        } else if ("pubDate".equals(eltName)) {
                            currentItem.setPubDate(parser.nextText());
                            Log.i("pubDate", currentItem.getPubDate());
                        } else if ("category".equals(eltName)) {
                            currentItem.setCategory(parser.nextText());
                            Log.i("Category", currentItem.getCategory());
                        } else if ("geo:lat".equals(eltName)) {
                            currentItem.setLat(parser.nextText());
                            Log.i("lat", currentItem.getLat());
                        } else if ("geo:long".equals(eltName)) {
                            currentItem.setLon(parser.nextText());
                            Log.i("lon", currentItem.getLon());
                        }
                    }
                    break;
            }
            //we are finished looking here, move the parser forward
            eventType = parser.next();
        }
        //now that we have an arraylist of items, return them
        return items;
    }

    //method to parse the title field of the xml tag and return a title object
    private Title parseTitle(String unParsed){
        Log.e("Title Unparsed", unParsed);
        //create a new title object
        Title title= new Title();
        //split the string at colon to remove the "UK Earthquake alert" and grab the magnitude
        String[] splitter = unParsed.split(":");
        //the magnitude section of the string is at splitter 1
        String mag=splitter[1];
        double parsedMag=parseMag(mag);
        //the location/datetime section of the string is at splitter 2
        String locdate=splitter[2];
        Log.i("location and date ",locdate);
        String parsedLoc=parseLocation(locdate);
        String parsedDate=parseDate(locdate);



        title.setTitle("UK Earthquake");
        title.setMagnitude(parsedMag);
        title.setLocation(parsedLoc);
        title.setDate(parsedDate);
        return title;
    }
    //method to parse a magnitude string from the XML file
    private double parseMag(String mag){
        mag = mag.replaceAll("[^\\.0123456789]","");
        Double numericMag=Double.parseDouble(mag);
        Log.i("magnitude",String.valueOf(numericMag));
        return numericMag;
    }
    //a method to parse the location out of the location and date portion of title
    private String parseLocation(String locString){
        //delimit using commas
        String[] splitter = locString.split(",");
        //since the last two values of the array are the date which we dont want, we can simply replace them with empty strings
        int lengthArray=splitter.length-1;
        splitter[lengthArray]="";
        splitter[lengthArray-1]="";
        String location= Arrays.toString(splitter);
        location=location.replace("[","");
        location=location.replace("]","");
        location=location.replace(",","");
        location=location.trim();
        location=location.replace(" ",", ");
        Log.i("location",location);
        return location;
    }
    //method to pull a date string out of the location and date portion of the title
    private String parseDate(String dateString){
        //delimit using commas
        String[] splitter = dateString.split(",");
        //since the last two values of the array are the date, we can simply take them into a new string
        int lengthArray=splitter.length-1;
        //day and day2 are the last two delimited parts of the array we want to keep and make a string out of
        String day2=splitter[lengthArray];
        day2 = day2.substring(0, day2.length() - 2);

        String day=splitter[lengthArray-1];
        String date=day + day2;

        Log.i("date",date);
        return date;
    }



    //method to parse the title field of the xml tag and return a title object
    private Description parseDesc(String unParsed){
        //create a new description object
        Log.e("Description Unparsed", unParsed);
        Description description= new Description();
        //delimit using semicolons
        String[] splitter = unParsed.split(";");
        String parsedDateTime=parseDateTime(splitter[0]);
        String parsedLatLong=parseDescString(splitter[2]);

        //split the lat and lon into seperate double variables one for lat and one for lon
        String[] latlonSplitter = parsedLatLong.split(",");
        double lat=Double.parseDouble(latlonSplitter[0]);
        double lon=Double.parseDouble(latlonSplitter[1]);
        int depth=parseDepth(splitter[3]);

        description.setDateTime(parsedDateTime);
        description.setLat(lat);
        description.setLon(lon);
        description.setDepth(depth);
        Log.d("DateTime(parsed f desc)",description.getDateTime());
        Log.d("lat (parsed f desc)",Double.toString(description.getLat()));
        Log.d("lon (parsed f desc)",Double.toString(description.getLon()));
        Log.d("depth (parsed f desc)",Integer.toString(description.getDepth())+" Kilometers");
        return description;
    }
    //a function to strip the name off of a description component string
    private String parseDescString(String unParsed){
        //delimit using colon
        String[] splitter = unParsed.split(":");
        return splitter[1];
    }

    //a function to strip the name off of a description component string
    private String parseDateTime(String unParsed){
        //delimit using colon
        String[] splitter = unParsed.split(":");
        String outputString=splitter[1]+":"+splitter[2]+":"+splitter[3];
        //String outputString="hello";
        return outputString;
    }

    private int parseDepth(String unParsed){
        //delimit using colon
        String[] splitter = unParsed.split(":");
        //take only the number from the string i.e the KM at the end of the depth string will be removed and the int returned will be the depth
        String depthString= splitter[1].replaceAll("\\D+","");
        //parse depth to be an int and not a string
        int depth=Integer.parseInt(depthString);
        return depth;
    }
    //a method to build a description string out of a description object
    private String descriptionMaker(Description description ,Title title){
        String dateTime=description.getDateTime();
        String day=title.getDate();
        String magnitude=Double.toString(title.getMagnitude());
        String depth=Integer.toString(description.getDepth());
        String lat=Double.toString(description.getLon());
        String lon=Double.toString(description.getLon());
        String location=title.getLocation();
        String descriptionString="An earthquake of magnitude "+magnitude+ " was reported in "+ location + " on " + day + ". This quake was recorded at a depth of "+ depth +" kilometers from the earth's surface";
        return descriptionString;
    }

    //method to fill the arraylist with the markers
    private void fillMarkerList(ArrayList<Item> quakes) {
        markerList = new ArrayList<>();
        //instantiate a variable with the arraylist of quakes
        //the problem is here fix this tomorrow
        //String teststring= quakes(1).get().getDescription;
        //for each item in the list
        for(int i=0; i<quakes.size(); i++) {
            //add it to markerlist
            double lat=quakes.get(i).getDescription().getLat();
            double lng=quakes.get(i).getDescription().getLon();
            LatLng location = new LatLng(lat, lng);
            Log.e("lat for latlng",Double.toString(lat));
            Log.e("lng for latlng",Double.toString(lng));
            MarkerOptions marker=new MarkerOptions()
                    .position(location)
                    .title("Earthquake in "+quakes.get(i).getTitle().getLocation() )
                    .snippet("Magnitude "+ quakes.get(i).getTitle().getMagnitude()+ " quake "+ quakes.get(i).getDescription().getDepth() + " kilometers below ground");
            mMap.addMarker(marker);
        }
        //set the camera on the UK
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ukLatLon,5));
    }

    // Need separate thread to access the internet resource over network
    // Other neater solutions should be adopted in later iterations.
    private class Task implements Runnable {
        private String url;

        public Task(String aurl) {
            url = aurl;
        }

        @Override
        public void run() {

            URL aurl;
            URLConnection yc;
            BufferedReader in = null;
            String inputLine = "";


            Log.e("MyTag", "in run");

            try {
                Log.e("MyTag", "in try");
                aurl = new URL(url);
                yc = aurl.openConnection();
                in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                //
                // Throw away the first 2 header lines before parsing
                //
                //
                //
                while ((inputLine = in.readLine()) != null) {
                    result = result + inputLine;
                    Log.e("MyTag", inputLine);

                }
                in.close();
            } catch (IOException ae) {
                Log.e("MyTag", "ioexception");
            }


            //UI THREAD FOR MAPACTIVITY
            MapActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Log.d("UI thread", "I am the UI thread");
                    //InputStream parseStream = null;
                    //fillMarkerList(parseXML(result));
                    fillMarkerList(parseXML(result));
                }
            });
        }
    }
}
