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
public interface LocationDao {

    @Query("SELECT * FROM Location")
    List<Location> getAllRecords();

    @Query("SELECT * FROM Location WHERE locationWoeid == :location")
    List<Location> getLocation(String location);

    @Query("SELECT * FROM Location WHERE locationName == :location")
    List<Location> getWoeid(String location);

    @Query("SELECT COUNT(*) from Location")
    int countLocations();

    @Insert
    void insertAll(Location... location);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertLoc(Location... location);

    @Delete
    void delete(Location location);
}