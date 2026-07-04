package com.example.finalapp_idan_andrei.logic;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * The app's Room database. Holds two tables (see {@link SpeedTestResult} and
 * {@link AppSettings}), both accessed through {@link SpeedTestDao}.
 *
 * Room requires a real query-execution engine, so it generates the actual
 * implementation of this abstract class at compile time (via the
 * "androidx.room:room-compiler" annotation processor) - we never implement
 * speedTestDao() ourselves.
 */
@Database(entities = {SpeedTestResult.class, AppSettings.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    // Singleton: only one Room database instance should exist per process.
    private static AppDatabase instance;

    public abstract SpeedTestDao speedTestDao();

    /**
     * Lazily creates (once) and returns the single shared database instance.
     * fallbackToDestructiveMigration() means bumping "version" above and changing
     * the entities just wipes and recreates the tables instead of requiring a
     * hand-written migration - fine for this app since history/settings aren't
     * critical data worth preserving across schema changes.
     */
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
