package com.example.finalapp_idan_andrei.ui.speedtest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.finalapp_idan_andrei.databinding.FragmentSpeedTestBinding;

import com.example.finalapp_idan_andrei.logic.AppDatabase;
import com.example.finalapp_idan_andrei.logic.AppSettings;
import com.example.finalapp_idan_andrei.logic.SpeedTestManager;
import com.example.finalapp_idan_andrei.logic.SpeedTestResult;
import java.util.Locale;

/**
 * The Speed Test tab (the app's start destination). Shows the live gauge/stat cards
 * and drives a {@link SpeedTestManager} to actually run ping/download/upload against
 * the network, then saves the finished result to Room.
 */
public class SpeedTestFragment extends Fragment {

    private FragmentSpeedTestBinding binding;
    private SpeedTestManager speedTestManager;
    // Ping and download are needed again once the upload finishes, to save the full result row.
    private long lastPing;
    private double lastDownload;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentSpeedTestBinding.inflate(inflater, container, false);
        speedTestManager = new SpeedTestManager();

        loadSettings(); // just to show the correct unit label (Mbps/MB/s) before any test runs
        binding.btnStart.setOnClickListener(v -> startSpeedTest());

        return binding.getRoot();
    }

    /** Reads the saved unit preference from Room and updates the unit label shown under the gauge. */
    private void loadSettings() {
        new Thread(() -> {
            AppSettings settings = AppDatabase.getInstance(requireContext()).speedTestDao().getSettings();
            if (settings == null) settings = new AppSettings();

            final String unit = settings.isUseMegabytes() ? "MB/s" : "Mbps";
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (binding != null) {
                        binding.unitLabel.setText(unit);
                    }
                });
            }
        }).start();
    }

    /**
     * Re-reads the latest settings (in case they changed since loadSettings() ran) and then
     * starts the actual test via SpeedTestManager, wiring its callbacks to the UI.
     */
    private void startSpeedTest() {
        binding.btnStart.setEnabled(false);
        binding.btnStart.setText("TESTING...");

        new Thread(() -> {
            AppSettings settings = AppDatabase.getInstance(requireContext()).speedTestDao().getSettings();
            if (settings == null) {
                settings = new AppSettings(); // Use defaults
            }
            final AppSettings finalSettings = settings;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (binding != null) {
                        binding.unitLabel.setText(finalSettings.isUseMegabytes() ? "MB/s" : "Mbps");
                    }
                    speedTestManager.startTest(finalSettings, new SpeedTestManager.SpeedTestListener() {
                        @Override
                        public void onPingResult(long pingMs, double jitterMs) {
                            // binding may already be null if the view was torn down mid-test
                            // (e.g. the user switched tabs) - SpeedTestManager keeps running in
                            // the background regardless, so every callback must guard against this.
                            if (binding == null) return;
                            lastPing = pingMs;
                            binding.pingText.setText(pingMs + " ms");
                        }

                        @Override
                        public void onDownloadProgress(double val) {
                            updateSpeedDisplay(val);
                        }

                        @Override
                        public void onDownloadFinished(double finalVal) {
                            if (binding == null) return;
                            lastDownload = finalVal;
                            binding.downloadText.setText(String.format(Locale.getDefault(), "%.1f", finalVal));
                        }

                        @Override
                        public void onUploadProgress(double val) {
                            updateSpeedDisplay(val);
                        }

                        @Override
                        public void onUploadFinished(double finalVal) {
                            // Save first: this should persist even if the view is already gone.
                            saveResult(lastPing, lastDownload, finalVal);
                            if (binding == null) return;
                            binding.uploadText.setText(String.format(Locale.getDefault(), "%.1f", finalVal));
                            binding.btnStart.setEnabled(true);
                            binding.btnStart.setText("START TEST");
                        }

                        @Override
                        public void onError(String message) {
                            if (binding == null) return;
                            binding.btnStart.setEnabled(true);
                            binding.btnStart.setText("START TEST");
                        }
                    });
                });
            }
        }).start();
    }

    /** Updates the big center number - shared by both the download and upload progress callbacks. */
    private void updateSpeedDisplay(double mbps) {
        if (binding == null) return;
        binding.mainSpeedValue.setText(String.format(Locale.getDefault(), "%.1f", mbps));
    }

    /** Persists one finished test run to Room so it shows up in the History tab. */
    private void saveResult(long ping, double download, double upload) {
        SpeedTestResult result = new SpeedTestResult();
        result.setTimestamp(System.currentTimeMillis());
        result.setPingMs(ping);
        result.setDownloadMbps(download);
        result.setUploadMbps(upload);

        new Thread(() -> {
            AppDatabase.getInstance(requireContext()).speedTestDao().insert(result);
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop the background test and suppress any further callbacks - without this,
        // SpeedTestManager would keep hammering the network and eventually crash trying
        // to update a binding that no longer exists.
        if (speedTestManager != null) {
            speedTestManager.cancel();
        }
        binding = null;
    }
}
