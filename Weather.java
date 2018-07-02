package bmaris.weatherapp;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

@Entity(primaryKeys = {"locationWoeid", "locationDate"})
public class Weather {

    String locationWoeid;
    String locationDate;
    String weatherState;
    String weatherStateAbbr;
    String windDirection;
    int windSpeed; // mph
    int humidity; // %
    int airPressure; // mbar
    int currentTemp; // degrees C
    int maxTemp; // degrees C
    int minTemp; // degrees C

    public String getLocationWoeid()
    {
        return locationWoeid;
    }

    public void setLocationWoeid(String woeid)
    {
        this.locationWoeid = woeid;
    }

    public String getLocationDate()
    {
        return locationDate;
    }

    public void setLocationDate(String date)
    {
        this.locationDate = date;
    }

    public String getWeatherState()
    {
        return weatherState;
    }

    public void setWeatherState(String state)
    {
        this.weatherState = state;
    }

    public String getWeatherStateAbbr()
    {
        return weatherStateAbbr;
    }

    public void setWeatherStateAbbr(String stateAbbr)
    {
        this.weatherStateAbbr = stateAbbr;
    }

    public String getWindDirection()
    {
        return windDirection;
    }

    public void setWindDirection(String wDirection)
    {
        this.windDirection = wDirection;
    }

    public int getWindSpeed()
    {
        return windSpeed;
    }

    public void setWindSpeed(int wSpeed)
    {
        this.windSpeed = wSpeed;
    }

    public int getHumidity()
    {
        return humidity;
    }

    public void setHumidity(int newHumidity)
    {
        this.humidity = newHumidity;
    }

    public int getAirPressure()
    {
        return airPressure;
    }

    public void setAirPressure(int aPressure)
    {
        this.airPressure = aPressure;
    }

    public int getCurrentTemp()
    {
        return currentTemp;
    }

    public void setCurrentTemp(int currTemp)
    {
        this.currentTemp = currTemp;
    }

    public int getMaxTemp()
    {
        return maxTemp;
    }

    public void setMaxTemp(int mxTemp)
    {
        this.maxTemp = mxTemp;
    }

    public int getMinTemp()
    {
        return minTemp;
    }

    public void setMinTemp(int mnTemp)
    {
        this.minTemp = mnTemp;
    }

}

