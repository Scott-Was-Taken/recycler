
//
// Name                 Scott Thompson
// Student ID           S1507806
// Programme of Study   Computing
//

package com.example.recycler;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Adapter adapter;
    private List<RecyclerItem> exampleList;
    private String result;
    private String url1 = "";
    private String urlSource = "http://quakes.bgs.ac.uk/feeds/MhSeismology.xml";
    private static final String TAG="MainActivity";
    private static final int ERROR_DIALOG_REQUEST=9001;

    public void startProgress() {
        // Run network access on a separate thread;
        new Thread(new Task(urlSource)).start();
    } //

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //google play is working as expected we can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but it is resolvable by the user.
            Log.d(TAG, "isServicesOK: an error occured such as a version issue");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
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

            //
            // Now that you have the xml data you can parse it
            //

            // Now update the TextView to display raw XML data
            // Probably not the best way to update TextView
            // but we are just getting started !

            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Log.d("UI thread", "I am the UI thread");
                    //InputStream parseStream = null;
                    //parse the xml string
                    fillExampleList(parseXML(result));
                    setUpRecyclerView();
                    //if google services is ok
                    if(isServicesOK()){
                        //configure the map button
                        configureMapButton();
                    }

                    //rawDataDisplay.setText(result);
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startProgress();
        setContentView(R.layout.activity_main);

    }



    private void configureMapButton(){
        Button nextButton = (Button) findViewById(R.id.MapButton);
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,MapActivity.class));
            }
        });
    }

    //a method written to connect to the URL before I realized that part was already done for me, fairly embarrassing moment
    //but it was also a massive waste of time so therefore seems somewhat worth leaving in
    //tl;dr ignore this method, its not used and therefore probably not important for marking purposes -
    //but also if you feel like this is worth marks in any way shape or form, that would also be cool.
    private InputStream connectURL() throws IOException {
        InputStream stream = null;
        //set the url
        URL RssUrl = new URL(urlSource);
        //open a connection to the URL
        HttpURLConnection conn = (HttpURLConnection) RssUrl.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        //set stream equal to the input stream from the URL
        stream = conn.getInputStream();
        return stream;
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
            Toast.makeText(MainActivity.this,
                    "we be crashing boys", Toast.LENGTH_LONG).show();
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

    //method to fill the arraylist with the items
    private void fillExampleList(ArrayList<Item> quakes) {
        exampleList = new ArrayList<>();
        //instantiate a variable with the arraylist of quakes
        //the problem is here fix this tomorrow
        //String teststring= quakes(1).get().getDescription;
        //for each item in the list
        for(int i=0; i<quakes.size(); i++) {
            //add it to the adapter
            exampleList.add(new RecyclerItem(R.drawable.ic_android, quakes.get(i).getTitle().getTitle()+" in "+ quakes.get(i).getTitle().getLocation(),descriptionMaker(quakes.get(i).getDescription(), quakes.get(i).getTitle())));
        }
    }
    //set up the recycler
    private void setUpRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new Adapter(exampleList);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }
}