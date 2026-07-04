package com.example.finalapp_idan_andrei.logic;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity class representing a single speed test record in the database.
 */
@Entity(tableName = "speed_test_history")
public class SpeedTestResult {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private long timestamp;
    private double downloadMbps;
    private double uploadMbps;
    private long pingMs;

    // Constructors
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
