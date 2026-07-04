package com.example.finalapp_idan_andrei.logic;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * The Room Database class that serves as the main access point to the persisted data.
 */
@Database(entities = {SpeedTestResult.class, AppSettings.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract SpeedTestDao speedTestDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "speed_test_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
