package bmaris.weatherapp;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

@Database(entities = {Weather.class, Location.class}, version = 1)
public abstract class WeatherDatabase extends RoomDatabase {

    private static WeatherDatabase INSTANCE;

    public abstract WeatherDao weatherDao();

    public abstract LocationDao locationDao();

    public static WeatherDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), WeatherDatabase.class, "weatherDb")
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

}
