package bmaris.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class SelectLocation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);

        // Check whether or not app already has location permissions
        int checkLocPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        // If permissions not already granted, run permissions function
        if(checkLocPermission != 0) {
            LocationPermissions();
        }

        // Reset userInput value
        Home.userInput = null;
    }

    public void searchLocation(View view)
    {
        // Get text input into search bar and store in a variable in Home
        EditText locationText = (EditText)findViewById(R.id.locationText);

        if(locationText.getText().toString().equals("")) {
            Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show();
        } else {
            Home.userInput = locationText.getText().toString();

            // Empty search bar
            locationText.setText("");

            // Start new intent to return to Home activity and trigger onActivityResult
            Intent returnIntent = new Intent();
            setResult(this.RESULT_CANCELED, returnIntent);
            this.finish();
        }

    }

    // Use phone location to get user lat/long
    public void getPhoneLocation(View view)
    {
        // create variable to check whether or not app has location permissions
        int checkLocPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if(checkLocPermission == 0) {
            // get location code
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            // Use network provider to get last known location
            String locationProvider = LocationManager.GPS_PROVIDER;
            Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

            // check if a last known location exists
            if (lastKnownLocation == null) {
                // if no lastlocation is available, set lat/long to London
                Home.userLatitude = 51.5074;
                Home.userLongitude = 0.1278;
            } else {
                // if last location exists then set the lat/long to phone location
                Home.userLatitude = lastKnownLocation.getLatitude();
                Home.userLongitude = lastKnownLocation.getLongitude();
            }

            // Start intent to close activity and return to home, triggering onActivityResult
            Intent returnIntent = new Intent();
            setResult(this.RESULT_CANCELED, returnIntent);
            this.finish();

        } else {
            // Else inform the user that location permissions are required for this feature
            Toast.makeText(SelectLocation.this, "This feature requires location permissions", Toast.LENGTH_SHORT).show();
        }

    }

    // Function to ask the user for runtime permission for the app to use location sensor
    public void LocationPermissions()
    {
        ActivityCompat.requestPermissions(SelectLocation.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // If permission was not granted, create an alert dialog stating this to the user
                    AlertDialog.Builder error = new AlertDialog.Builder(this);
                    error.setMessage("Without permissions, phone location cannot be used to determine location");
                    error.setCancelable(true);

                    // Create ok button to close the dialog window
                    error.setPositiveButton(
                            "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    // Create alertdialog with these settings, and then show to the user
                    AlertDialog noPermissions = error.create();
                    noPermissions.show();
                }
                return;
            }
        }
    }

}
