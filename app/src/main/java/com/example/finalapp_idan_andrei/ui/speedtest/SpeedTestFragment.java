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

public class SpeedTestFragment extends Fragment {

    private FragmentSpeedTestBinding binding;
    private SpeedTestManager speedTestManager;
    private long lastPing;
    private double lastDownload;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentSpeedTestBinding.inflate(inflater, container, false);
        speedTestManager = new SpeedTestManager();

        loadSettings();
        binding.btnStart.setOnClickListener(v -> startSpeedTest());

        return binding.getRoot();
    }

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

    private void updateSpeedDisplay(double mbps) {
        if (binding == null) return;
        binding.mainSpeedValue.setText(String.format(Locale.getDefault(), "%.1f", mbps));
    }

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
        if (speedTestManager != null) {
            speedTestManager.cancel();
        }
        binding = null;
    }
}
