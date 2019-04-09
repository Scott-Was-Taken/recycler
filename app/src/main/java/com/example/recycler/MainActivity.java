package com.example.recycler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

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
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ExampleAdapter adapter;
    private List<ExampleItem> exampleList;
    private String result;
    private String url1 = "";
    private String urlSource = "http://quakes.bgs.ac.uk/feeds/MhSeismology.xml";


    public void startProgress() {
        // Run network access on a separate thread;
        new Thread(new Task(urlSource)).start();
    } //


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

    //a method written to connect to the URL before I realized that part was already done, fairly embarrassing moment
    //but it was also a massive waste of time so therefore seems somewhat worth leaving in
    //tl;dr ignore this method, its not used and therefore probably not important for marking purposes
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


    private ArrayList<Item> parseXML(String stream) {

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
                            currentItem.setTitle(parser.nextText());
                            Log.i("title", currentItem.getTitle());
                        } else if ("description".equals(eltName)) {
                            currentItem.setDescription(parser.nextText());
                            Log.i("desc", currentItem.getDescription());
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
        //now that we have an arraylist of items, pass them to a print function
        return items;
    }
    //method to fill the arraylist with the items
    private void fillExampleList(ArrayList<Item> quakes) {
        exampleList = new ArrayList<>();
        //instantiate a variable with the arraylist of quakes
        //the problem is here fix this tomorrow
        //String teststring= quakes(1).get().getDescription;
        Log.e("tracer", quakes.get(1).getDescription());
        //for each item in the list
        for(int i=0; i<quakes.size(); i++) {
            //add it to the adapter
            exampleList.add(new ExampleItem(R.drawable.ic_android, quakes.get(i).getTitle(),quakes.get(i).getDescription()));
        }
    }
    //set up the recycler
    private void setUpRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new ExampleAdapter(exampleList);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);

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