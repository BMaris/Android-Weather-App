package bmaris.weatherapp;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static bmaris.weatherapp.Home.weatherDb;

public class ShowWeather extends AppCompatActivity {

    // Declaring arrays to hold all of the data returned by REST calls
    ArrayList<String> date = new ArrayList<String>();
    ArrayList<String> state = new ArrayList<String>();
    ArrayList<String> stateCode = new ArrayList<String>();
    ArrayList<String> windDirection = new ArrayList<String>();
    int[] windSpeed = new int[6];
    int[] humidity = new int[6];
    int[] airPressure = new int[6];
    int[] currentTemp = new int[6];
    int[] maxTemp = new int[6];
    int[] minTemp = new int[6];

    //JSONArray to place the raw json string into
    JSONArray jsonArray;
    //REST call URL
    String url;
    //Index to track which day the user is currently showing
    public static int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_weather);

        // Set title of the page to the name of the area being shown
        TextView displayLoc = (TextView)findViewById(R.id.locationName);
        displayLoc.setText(Home.userLocationName);

        //When activity is started, reset date index and check whether the user is connected
        //to the internet
        index = 0;
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        //Is user connected to the internet?
        if(isConnected)
            new parseWeatherInfo().execute();  //If yes, send a GET request to the REST server and parse returned data into the arrays
        else
            new getSavedWeather().execute();  //If no, populate fields with previously saved data
    }


    // Populate each of the weather fields with data from the arrays declared at the start of the activity
    public void populateWeatherInfo()
    {
        // Use abbreviated state code to download the relevant weather icon from the API
        url = "https://www.metaweather.com/static/img/weather/png/" + stateCode.get(index) + ".png";
        new getWeatherImg().execute();

        // If arrays are not empty, change all text fields to their relevant weather information
        // With units
        if(date.size() != 0 ){


            TextView displayDate = (TextView) findViewById(R.id.locationDate);
            displayDate.setText(date.get(index));

            TextView displayState = (TextView) findViewById(R.id.locState);
            displayState.setText(state.get(index));

            TextView dispWindSpeed = (TextView) findViewById(R.id.windSpeed);
            dispWindSpeed.setText(this.windSpeed[index] + "mph " + windDirection.get(0));

            TextView dispHumidity = (TextView) findViewById(R.id.humidity);
            dispHumidity.setText(humidity[index] + "%");

            TextView dispAirPressure = (TextView) findViewById(R.id.airPressure);
            dispAirPressure.setText(airPressure[index] + "mbar");

            TextView dispCurrTemp = (TextView) findViewById(R.id.currentTemp);

            if(index == 0)  // Only show currentTemp if index = 0 (displaying current day)
                dispCurrTemp.setText(currentTemp[index] + " \u00b0C");
            else
                dispCurrTemp.setText("");


            TextView dispMaxTemp = (TextView) findViewById(R.id.maxTemp);
            dispMaxTemp.setText(maxTemp[index]+" \u00b0C");

            TextView dispMinTemp = (TextView) findViewById(R.id.minTemp);
            dispMinTemp.setText(minTemp[index]+" \u00b0C");
        }
    }


    // Code adapted from Workshop 5
    // Make the REST call to get the weather data for the selected area
    public class parseWeatherInfo extends AsyncTask<String, String, String> {

        String weatherUrl;

        @Override
        protected String doInBackground(String... arg0)  {

            // Complete and store the url
            weatherUrl = "https://www.metaweather.com/api/location/" + Home.userLocationID;

            try {
                // create new instance of the httpConnect class
                httpConnect weatherParser = new httpConnect();
                // Run the getJSONfromUrl function, passing the Url, store result in a String
                String json = weatherParser.getJSONFromUrl(weatherUrl);
                // Move the json String into a JSONObject containing several arrays
                JSONObject jsonInput = new JSONObject(json);
                // Get the array containing the weather information from the JSON object
                jsonArray = jsonInput.getJSONArray("consolidated_weather");

                // For each element in the JSONarray
                for (int i = 0; i < jsonArray.length(); i++) {
                    //Create a JSONObject for each of the objects (dates) inside the JSONArray as they are needed
                    JSONObject json_message = jsonArray.getJSONObject(i);

                    // If JSONArray isn't empty, store each of the details into the next position in the relevant array
                    if (jsonArray != null) {

                        date.add(json_message.getString("applicable_date"));
                        state.add(json_message.getString("weather_state_name"));
                        stateCode.add(json_message.getString("weather_state_abbr"));
                        windDirection.add(json_message.getString("wind_direction_compass"));
                        windSpeed[i] = (int)Math.round(json_message.getDouble("wind_speed"));
                        humidity[i] = (int)Math.round(json_message.getDouble("humidity"));
                        airPressure[i] = (int)Math.round(json_message.getDouble("air_pressure"));
                        currentTemp[i] = (int)Math.round(json_message.getDouble("the_temp"));
                        maxTemp[i] = (int)Math.round(json_message.getDouble("max_temp"));
                        minTemp[i] = (int)Math.round(json_message.getDouble("min_temp"));

                    }
                }

                // For each element in position i in the data arrays, create a new Weather object,
                // populate each of the fields and then save to the database
                for(int i = 0; i < date.size(); i++)
                {
                    Weather locWeather = new Weather();

                    locWeather.locationWoeid = Home.userLocationID;
                    locWeather.locationDate = date.get(i);
                    locWeather.weatherState = state.get(i);
                    locWeather.weatherStateAbbr = stateCode.get(i);
                    locWeather.windDirection = windDirection.get(i);
                    locWeather.windSpeed = windSpeed[i];
                    locWeather.humidity = humidity[i];
                    locWeather.airPressure = airPressure[i];
                    locWeather.currentTemp = currentTemp[i];
                    locWeather.maxTemp = maxTemp[i];
                    locWeather.minTemp = minTemp[i];

                    // Insert object into database as a record
                    Home.weatherDb.weatherDao().insertWeatherData(locWeather);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        // Do this afterwards
        protected void onPostExecute(String strFromDoInBg) {

            // With arrays all filled with necessary data, populate the text fields on-screen
            populateWeatherInfo();

            // Hide progress bar once page is filled with data
            ProgressBar loading = (ProgressBar)findViewById(R.id.loadingBar);
            loading.setVisibility(View.INVISIBLE);

        }
    }

    // Show weather for previous day, if data exists
    public void prevDay(View view)
    {
        // If date is not empty and is greater than 0
        if(date.size() != 0 && index > 0) {
            // Decrement index to get all data from the day before the currently displayed date
            index--;
            populateWeatherInfo();
        } else {
            // If index is already at extreme of data, show message saying no data available
            Toast.makeText(ShowWeather.this, "No weather data available.", Toast.LENGTH_SHORT).show();
        }
    }

    // Show weather data for the next day, if data exists
    public void nextDay(View view)
    {
        // If date is not empty and is not at end of arrays
        if(date.size() != 0 && index < date.size() - 1) {
            //Increment index to get all data from the day after the current one
            index++;
            populateWeatherInfo();
        } else {
            // If index is at data extreme, state that there is no data for days after the currently displayed one
            Toast.makeText(ShowWeather.this, "No weather data available.", Toast.LENGTH_SHORT).show();
        }
    }

    // Async task to fetch image associated with the current weather state
    public class getWeatherImg extends AsyncTask<String, String, Bitmap>
    {
        protected Bitmap doInBackground(String... arg0) {
            try {
                //Use url to create a new HTTPconnection to retrieve the image
                URL u = new URL(url);
                HttpURLConnection imgConnect = (HttpURLConnection) u.openConnection();
                imgConnect.setDoInput(true);
                imgConnect.connect();

                // Get bitmap data from InputStream and use a BitmapFactory to decode it into a bitmap
                InputStream input = imgConnect.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                // Return bitmap to onPostExecute
                return bitmap;

            } catch (MalformedURLException ex) {
                Log.e("Malformed URL", ex.getMessage());
            } catch (IOException ex) {
                Log.e("IO Exception", ex.getMessage());
            }
            return null;
        }

        protected void onPostExecute(Bitmap bitmap)
        {
            // Set weather icon imageview to the downloaded Bitmap
            ImageView weatherIcon = (ImageView)findViewById(R.id.weatherImage);
            weatherIcon.setImageBitmap(bitmap);
        }
    }


    // If no internet connection is available, get saved weather data from WeatherDatabase weatherDb
    public class getSavedWeather extends AsyncTask<String, String, List<Weather>>
    {
        protected List<Weather> doInBackground(String... arg0)
        {
            try
            {
                // Get all records for which the location equals the selected location, store in a list of records
                List<Weather> locationDb = Home.weatherDb.weatherDao().getLocationWeather(Home.userLocationID);

                //Loop through each entry in the record list and add each field to the relevant array ay the current position
                for(int i = 0; i < locationDb.size(); i++)
                {
                    date.add(locationDb.get(i).getLocationDate());
                    state.add(locationDb.get(i).getWeatherState());
                    stateCode.add(locationDb.get(i).getWeatherStateAbbr());
                    windDirection.add(locationDb.get(i).getWindDirection());
                    windSpeed[i] = (locationDb.get(i).getWindSpeed());
                    humidity[i] = (locationDb.get(i).getHumidity());
                    airPressure[i] = (locationDb.get(i).getAirPressure());
                    currentTemp[i] = (locationDb.get(i).getCurrentTemp());
                    maxTemp[i] = (locationDb.get(i).getMaxTemp());
                    minTemp[i] = (locationDb.get(i).getMinTemp());

                }
                // return weather data to onPostExecute
                return locationDb;
            }
            catch (Exception e)
            {
                Log.e("Exception caught", e.getMessage());
            }
            return null;
        }

        protected void onPostExecute(List<Weather> locationDb)
        {
            if(date.size() > 0)
            {
                // If arrays are not empty, call populate fields function
                populateWeatherInfo();

                // Create a toast informing the user that their device is not connected to the internet,
                // but the app was able to use previously saved data. Toast text centered.
                Toast savedData = Toast.makeText(ShowWeather.this, "No internet connection available, showing saved weather data", Toast.LENGTH_LONG);
                TextView savedDataText = (TextView) savedData.getView().findViewById(android.R.id.message);
                savedDataText.setGravity(Gravity.CENTER);
                savedData.show();
            }
            else
            {
                // If arrays are empty, no previous data can be loaded
                // Display a toast informing the user that their device is not connected to the internet,
                // and there is no previous data to show
                Toast noSavedData = Toast.makeText(ShowWeather.this, "No internet connection available, no saved data to show :(", Toast.LENGTH_LONG);
                TextView noSavedDataText = (TextView) noSavedData.getView().findViewById(android.R.id.message);
                noSavedDataText.setGravity(Gravity.CENTER);
                noSavedData.show();
                finish();
            }

            // With window populated, hide loading button
            ProgressBar loading = (ProgressBar)findViewById(R.id.loadingBar);
            loading.setVisibility(View.INVISIBLE);
        }
    }
}
