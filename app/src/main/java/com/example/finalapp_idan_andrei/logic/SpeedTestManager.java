package com.example.finalapp_idan_andrei.logic;

import android.os.Handler;
import android.os.Looper;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manager class to handle speed test logic (Ping, Download, Upload).
 */
public class SpeedTestManager {

    private static final String PING_URL = "https://speed.cloudflare.com/__down?bytes=0";
    private static final String DOWNLOAD_URL = "https://speed.cloudflare.com/__down?bytes=50000000"; // 50MB
    private static final String UPLOAD_URL = "https://speed.cloudflare.com/__up";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private volatile boolean cancelled = false;

    /**
     * Stops delivering further callbacks and unblocks the background loops as soon as possible.
     * Call this when the listener (e.g. a Fragment view) is no longer valid.
     */
    public void cancel() {
        cancelled = true;
        executorService.shutdownNow();
    }

    public interface SpeedTestListener {
        void onPingResult(long pingMs, double jitterMs);
        void onDownloadProgress(double mbps);
        void onDownloadFinished(double finalMbps);
        void onUploadProgress(double mbps);
        void onUploadFinished(double finalMbps);
        void onError(String message);
    }

    public void startTest(SpeedTestListener listener) {
        executorService.execute(() -> {
            try {
                // 1. Ping & Jitter
                runPingAndJitter(listener);
                if (cancelled) return;

                // 2. Download
                runDownload(listener);
                if (cancelled) return;

                // 3. Upload
                runUpload(listener);

            } catch (Exception e) {
                postError(listener, e.getMessage());
            }
        });
    }

    private void runPingAndJitter(SpeedTestListener listener) {
        List<Long> pings = new ArrayList<>();
        try {
            for (int i = 0; i < 5 && !cancelled; i++) {
                long start = System.currentTimeMillis();
                HttpURLConnection connection = (HttpURLConnection) new URL(PING_URL).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.connect();
                connection.getResponseCode(); // Wait for headers
                long end = System.currentTimeMillis();
                pings.add(end - start);
                connection.disconnect();
            }

            long avgPing = 0;
            for (long p : pings) avgPing += p;
            avgPing /= pings.size();

            double totalJitter = 0;
            for (int i = 0; i < pings.size() - 1; i++) {
                totalJitter += Math.abs(pings.get(i + 1) - pings.get(i));
            }
            double jitter = totalJitter / (pings.size() - 1);

            final long finalPing = avgPing;
            final double finalJitter = jitter;
            if (!cancelled) mainHandler.post(() -> listener.onPingResult(finalPing, finalJitter));

        } catch (Exception e) {
            postError(listener, "Ping Error: " + e.getMessage());
        }
    }

    private void runDownload(SpeedTestListener listener) {
        try {
            URL url = new URL(DOWNLOAD_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream input = connection.getInputStream();
            byte[] buffer = new byte[8192];
            long totalBytesRead = 0;
            long startTime = System.currentTimeMillis();
            long lastUpdate = startTime;

            int bytesRead;
            while (!cancelled && (bytesRead = input.read(buffer)) != -1) {
                totalBytesRead += bytesRead;
                long now = System.currentTimeMillis();

                // Update UI every 250ms
                if (now - lastUpdate > 250) {
                    double mbps = calculateMbps(totalBytesRead, now - startTime);
                    if (!cancelled) mainHandler.post(() -> listener.onDownloadProgress(mbps));
                    lastUpdate = now;
                }
            }

            double finalMbps = calculateMbps(totalBytesRead, System.currentTimeMillis() - startTime);
            if (!cancelled) mainHandler.post(() -> listener.onDownloadFinished(finalMbps));
            input.close();
            connection.disconnect();

        } catch (Exception e) {
            postError(listener, "Download Error: " + e.getMessage());
        }
    }

    private void runUpload(SpeedTestListener listener) {
        try {
            URL url = new URL(UPLOAD_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            // Generate ~10MB of random data
            byte[] dummyData = new byte[1024 * 1024 * 10];
            new Random().nextBytes(dummyData);

            // Without this, HttpURLConnection buffers the whole body in memory and only
            // actually sends it over the network once getResponseCode() is called below,
            // so the write loop finishes instantly and progress never has real time to sample.
            connection.setFixedLengthStreamingMode(dummyData.length);

            long startTime = System.currentTimeMillis();
            OutputStream output = connection.getOutputStream();
            
            int chunkSize = 8192;
            long totalBytesWritten = 0;
            long lastUpdate = startTime;

            for (int i = 0; i < dummyData.length && !cancelled; i += chunkSize) {
                int length = Math.min(chunkSize, dummyData.length - i);
                output.write(dummyData, i, length);
                totalBytesWritten += length;

                long now = System.currentTimeMillis();
                if (now - lastUpdate > 250) {
                    double mbps = calculateMbps(totalBytesWritten, now - startTime);
                    if (!cancelled) mainHandler.post(() -> listener.onUploadProgress(mbps));
                    lastUpdate = now;
                }
            }

            if (cancelled) {
                output.close();
                connection.disconnect();
                return;
            }

            output.flush();
            connection.getResponseCode(); // Wait for server to acknowledge

            double finalMbps = calculateMbps(totalBytesWritten, System.currentTimeMillis() - startTime);
            if (!cancelled) mainHandler.post(() -> listener.onUploadFinished(finalMbps));

            output.close();
            connection.disconnect();

        } catch (Exception e) {
            postError(listener, "Upload Error: " + e.getMessage());
        }
    }

    private double calculateMbps(long bytes, long durationMs) {
        if (durationMs <= 0) return 0;
        double seconds = durationMs / 1000.0;
        return (bytes * 8.0) / (seconds * 1_000_000.0);
    }

    private void postError(SpeedTestListener listener, String message) {
        if (cancelled) return;
        mainHandler.post(() -> listener.onError(message));
    }
}
