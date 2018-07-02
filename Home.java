package bmaris.weatherapp;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {

    // Freely accessible doubles to hold the user's lat/long if they use location services, and a
    // sting for if they decide to enter a city name manually
    public static double userLatitude;
    public static double userLongitude;
    public static String userInput;

    // Freely accessible variable to store the location name and location ID of the selected location
    // as they appear in the api, to minimise errors on the weather data rest call
    public static String userLocationID;
    public static String userLocationName;

    // Array of strings to hold data from the Location table, used to populate the drop down box
    // containing previous location searches
    ArrayList<String> woeid = new ArrayList<String>();
    ArrayList<String> locName = new ArrayList<String>();

    // Declaring database instance and a list of location records
    public static WeatherDatabase weatherDb;
    static List<Location> locList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // On app startup, instantiate the weather database, then run the Async task to get previous
        // location searches and populate the drop down box with them.
        weatherDb = WeatherDatabase.getAppDatabase(this);
        new populateSpinner().execute();
    }

    // Start Select New Location activity
    public void selectLocation(View view)
    {
        Intent location = new Intent(this, SelectLocation.class);

        // Check whether user's device has an internet connection
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        // If device is connected, start the New Location activity which will trigger onActivityResult
        if(isConnected)
            startActivityForResult(location, 1);
        else // Else inform the user that rest calls to obtain location ID cannot be made without internet access
            Toast.makeText(Home.this, "Adding new location requires internet access.", Toast.LENGTH_SHORT).show();
    }

    // Start Show Weather activity
    public void showWeather(View view)
    {
        Intent weather = new Intent(this, ShowWeather.class);

        // Get the currently selected location name
        Spinner locSpinner=(Spinner) findViewById(R.id.locDropDown);


        // If location name is not null
        if(locSpinner.getSelectedItem() == null)
        {
            // If text == null request that user selects a city
            Toast.makeText(Home.this, "Please select a city", Toast.LENGTH_SHORT).show();
        }
        else
        {
            userLocationName = locSpinner.getSelectedItem().toString();
            // Loop through each element in the location table and find the ID
            // associated with the selected location name
            for(int i = 0; i < locList.size(); i++) {
                if(userLocationName == locList.get(i).locationName){
                    userLocationID = locList.get(i).locationWoeid;
                    break;
                }
            }

            //With ID acquired, start ShowWeather activity
            startActivity(weather);

        }
    }

    // Start Resource Credits page containing credits to authors of all app resources used
    public void showCredits(View view)
    {
        Intent credits = new Intent(this, ResourceCredits.class);
        startActivity(credits);
    }

    // Called when SelectLocation activity is closed/ended
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //Show loading bar while new location is added to database

        if(Home.userInput != null || Home.userLatitude != 0) {
            ProgressBar loadingloc = (ProgressBar) findViewById(R.id.loadingLoc);
            loadingloc.setVisibility(View.VISIBLE);
            new parseUserLocation().execute();
        }
    }

    // Async task to get ID and Name of user detected/input location
    public class parseUserLocation extends AsyncTask<String, String, String> {

        String searchLocUrl;

        @Override
        // Perform asynchronously in the background
        protected String doInBackground(String... arg0)  {

            //Empty existing arrays of name/ids
            woeid.clear();
            locName.clear();

            // If userInput has a value assigned in SelectLocation, userlocation is not being used
            // Create url based on the type of data the user used
            if(Home.userInput != null)
            {
                searchLocUrl = "https://www.metaweather.com/api/location/search/?query=" + userInput;
            } else {
                searchLocUrl = "https://www.metaweather.com/api/location/search/?lattlong=" + userLatitude + "," + userLongitude;
            }

            try {
                // Code here adapted from Workshop 5
                // create new instance of the httpConnect class
                httpConnect jParser = new httpConnect();

                // get json string from service url
                String json = jParser.getJSONFromUrl(searchLocUrl);

                // parse returned json string into json array
                JSONArray jsonArray = new JSONArray(json);

                // loop through json array and populate arrays with location data
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json_message = jsonArray.getJSONObject(i);

                    if (json_message != null) {
                        //add each tweet to ArrayList as an item
                        woeid.add(json_message.getString("woeid"));
                        locName.add(json_message.getString("title"));
                    }

                }

                if(woeid.size() > 0) {
                    // If user specifies city name, api returns only one result which will be at index 0
                    // If user uses lat/long, several locations are returned in order of proximity to
                    //  the specified lat/long, so index 0 is the closest option
                    userLocationID = woeid.get(0);
                    userLocationName = locName.get(0);

                    // Create a new Location record object and populate fields
                    Location locData = new Location();
                    locData.setLocationName(userLocationName);
                    locData.setLocationWoeid(userLocationID);

                    // Insert record into Location table
                    weatherDb.locationDao().insertLoc(locData);

                } else {
                    // Else show that no location could be found from the input data

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        // Do this afterwards
        protected void onPostExecute(String strFromDoInBg) {
            // With new data added, spinner needs to be updated
            if(woeid.size() > 0)
                new populateSpinner().execute();
            else
                Toast.makeText(Home.this, "No location found", Toast.LENGTH_SHORT).show();

            // Hide progress bar
            ProgressBar loadingloc = (ProgressBar)findViewById(R.id.loadingLoc);
            loadingloc.setVisibility(View.INVISIBLE);
        }
    }


    // Async task to get saved location data from Location table and populate dropdown box
    public class populateSpinner extends AsyncTask<String, String, List<Location>>
    {
        protected List<Location> doInBackground(String... arg0)
        {
            try
            {
                // get all locations saved in database and return List of records to onPostExecute
                List<Location> locationDb = Home.weatherDb.locationDao().getAllRecords();
                return locationDb;
            }
            catch (Exception e)
            {
                Log.e("Exception caught", e.getMessage());
            }
            return null;
        }

        protected void onPostExecute(List<Location> locationDb)
        {
            // If list of locations contains records, populate spinner with location names
            if(locationDb != null) {
                // String array to hold location names
                String[] locNames = new String[locationDb.size()];

                // Loop through records and add location names to spinner array
                for(int i = 0; i <= locationDb.size()-1; i++)
                {
                    locNames[i] = locationDb.get(locationDb.size()-(1+i)).getLocationName();
                }

                // Get spinner id and use an arrayadapter to add string array elements to drop down list
                Spinner locNameSpinner = (Spinner) findViewById(R.id.locDropDown);
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(Home.this, android.R.layout.simple_spinner_item, locNames);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                // With adapter created, attach to spinner
                locNameSpinner.setAdapter(spinnerAdapter);

                // Add list of location records to locList for other functions to use
                locList = locationDb;

            }
            else
            {
                Toast.makeText(Home.this, "No location data available!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
