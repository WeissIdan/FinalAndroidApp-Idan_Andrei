package com.example.finalapp_idan_andrei;

import android.util.Log;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.finalapp_idan_andrei.logic.SpeedTestManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class SpeedTestLogicTest {
    private static final String TAG = "SpeedTestTest";

    @Test
    public void testSpeedTestLogic() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        SpeedTestManager manager = new SpeedTestManager();

        Log.d(TAG, "Starting Speed Test Logic Verification...");

        manager.startTest(new SpeedTestManager.SpeedTestListener() {
            @Override
            public void onPingResult(long pingMs, double jitterMs) {
                Log.d(TAG, ">>> RESULT - PING: " + pingMs + "ms, JITTER: " + jitterMs + "ms");
            }

            @Override
            public void onDownloadProgress(double mbps) {
                Log.d(TAG, "Progress - Download: " + String.format("%.2f", mbps) + " Mbps");
            }

            @Override
            public void onDownloadFinished(double finalMbps) {
                Log.d(TAG, ">>> RESULT - DOWNLOAD FINISHED: " + String.format("%.2f", finalMbps) + " Mbps");
            }

            @Override
            public void onUploadProgress(double mbps) {
                Log.d(TAG, "Progress - Upload: " + String.format("%.2f", mbps) + " Mbps");
            }

            @Override
            public void onUploadFinished(double finalMbps) {
                Log.d(TAG, ">>> RESULT - UPLOAD FINISHED: " + String.format("%.2f", finalMbps) + " Mbps");
                latch.countDown();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, ">>> ERROR: " + message);
                latch.countDown();
            }
        });

        // Wait up to 2 minutes for the test to complete
        latch.await(120, TimeUnit.SECONDS);
    }
}
