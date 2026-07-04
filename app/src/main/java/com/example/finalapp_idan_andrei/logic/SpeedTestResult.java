package com.example.finalapp_idan_andrei.logic;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A single completed speed test run, persisted in the "speed_test_history" table.
 * Room turns this into one row per instance saved via {@link SpeedTestDao#insert}.
 * The HistoryFragment reads these back to build its list.
 */
@Entity(tableName = "speed_test_history")
public class SpeedTestResult {
    // autoGenerate = true: Room assigns this automatically on insert, we never set it ourselves.
    @PrimaryKey(autoGenerate = true)
    private int id;

    private long timestamp;      // System.currentTimeMillis() when the test finished
    private double downloadMbps; // final download result of the test
    private double uploadMbps;   // final upload result of the test
    private long pingMs;         // average ping/latency in milliseconds

    // Room needs a no-arg constructor to build instances when reading rows back from the DB.
    public SpeedTestResult() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getDownloadMbps() { return downloadMbps; }
    public void setDownloadMbps(double downloadMbps) { this.downloadMbps = downloadMbps; }

    public double getUploadMbps() { return uploadMbps; }
    public void setUploadMbps(double uploadMbps) { this.uploadMbps = uploadMbps; }

    public long getPingMs() { return pingMs; }
    public void setPingMs(long pingMs) { this.pingMs = pingMs; }
}
