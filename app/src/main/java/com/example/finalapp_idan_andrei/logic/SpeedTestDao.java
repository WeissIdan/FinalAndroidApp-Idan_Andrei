package com.example.finalapp_idan_andrei.logic;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object (DAO) for speed test results.
 * Defines the database operations.
 */
@Dao
public interface SpeedTestDao {
    @Insert
    void insert(SpeedTestResult result);

    @Query("SELECT * FROM speed_test_history ORDER BY timestamp DESC")
    List<SpeedTestResult> getAllResults();

    @Query("DELETE FROM speed_test_history")
    void deleteAll();
}
