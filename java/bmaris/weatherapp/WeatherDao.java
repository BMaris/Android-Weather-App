package bmaris.weatherapp;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;

@Dao
public interface WeatherDao {
    @Query("SELECT * FROM Weather")
    List<Weather> getAll();

    @Query("SELECT * FROM Weather WHERE locationWoeid == :location")
    List<Weather> getLocationWeather(String location);

    @Query("SELECT COUNT(*) from Weather")
    int countLocations();

    @Insert
    void insertAll(Weather... weather);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertWeatherData(Weather... weather);

    @Delete
    void delete(Weather weather);
}