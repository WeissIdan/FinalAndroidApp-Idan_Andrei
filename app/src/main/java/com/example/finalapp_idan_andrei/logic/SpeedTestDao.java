package com.example.finalapp_idan_andrei.logic;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object (DAO) for the app's two tables. Room generates the actual
 * SQL-executing implementation of this interface at compile time - these method
 * bodies don't exist anywhere in source, Room writes them based on the annotations.
 * Every method here does real disk I/O, so all of them must be called from a
 * background thread (see the calling Fragments, which always wrap DAO calls in
 * a Thread/ExecutorService).
 */
@Dao
public interface SpeedTestDao {
    @Insert
    void insert(SpeedTestResult result);

    @Query("SELECT * FROM speed_test_history ORDER BY timestamp DESC")
    List<SpeedTestResult> getAllResults();

    @Query("DELETE FROM speed_test_history")
    void deleteAll();

    // Settings Queries
    // REPLACE: since AppSettings.id is always 1, this always overwrites the single
    // settings row instead of failing on a primary-key conflict or inserting a duplicate.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveSettings(AppSettings settings);

    @Query("SELECT * FROM app_settings WHERE id = 1")
    AppSettings getSettings();
}
