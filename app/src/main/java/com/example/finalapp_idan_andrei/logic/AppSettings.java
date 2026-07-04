package com.example.finalapp_idan_andrei.logic;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_settings")
public class AppSettings {
    @PrimaryKey
    private int id = 1; // Always use ID 1 for the single settings row

    private boolean useMegabytes = false;
    private long downloadSizeBytes = 50000000; // 50MB Default
    private int pingIterations = 5;
    private String theme = "Ambient";

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
