package com.example.finalapp_idan_andrei.logic;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * User-configurable test settings, persisted in the "app_settings" table.
 * There is only ever one row (id is always 1) - see {@link SpeedTestDao#saveSettings}
 * which uses REPLACE so saving always overwrites that single row instead of inserting more.
 * Edited from SettingsFragment and read by SpeedTestFragment/SpeedTestManager before each test.
 */
@Entity(tableName = "app_settings")
public class AppSettings {
    @PrimaryKey
    private int id = 1; // Always use ID 1 for the single settings row

    private boolean useMegabytes = false;          // false = show Mbps, true = show MB/s
    private long downloadSizeBytes = 50000000;     // how much data to pull for the download test (50MB default)
    private int pingIterations = 5;                // how many ping round-trips to average for latency/jitter
    private String theme = "Ambient";              // reserved for a future theme setting, unused for now

    // Room needs a no-arg constructor to build instances when reading rows back from the DB.
    public AppSettings() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public boolean isUseMegabytes() { return useMegabytes; }
    public void setUseMegabytes(boolean useMegabytes) { this.useMegabytes = useMegabytes; }

    public long getDownloadSizeBytes() { return downloadSizeBytes; }
    public void setDownloadSizeBytes(long downloadSizeBytes) { this.downloadSizeBytes = downloadSizeBytes; }

    public int getPingIterations() { return pingIterations; }
    public void setPingIterations(int pingIterations) { this.pingIterations = pingIterations; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
}
