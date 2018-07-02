package bmaris.weatherapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ResourceCredits extends AppCompatActivity {

    // Simple page containing several text views crediting authors of the API used
    // for this app as well as the creators of each of the image resources.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_credits);
    }
}
