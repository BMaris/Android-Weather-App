package bmaris.weatherapp;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

@Entity
public class Location {
    @PrimaryKey
    String locationWoeid;
    String locationName;

    public String getLocationWoeid()
    {
        return locationWoeid;
    }

    public void setLocationWoeid(String woeid)
    {
        this.locationWoeid = woeid;
    }

    public String getLocationName()
    {
        return locationName;
    }

    public void setLocationName(String name)
    {
        this.locationName = name;
    }
}
