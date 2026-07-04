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

        binding.btnStart.setOnClickListener(v -> startSpeedTest());

        return binding.getRoot();
    }

    private void startSpeedTest() {
        binding.btnStart.setEnabled(false);
        binding.btnStart.setText("TESTING...");

        speedTestManager.startTest(new SpeedTestManager.SpeedTestListener() {
            @Override
            public void onPingResult(long pingMs, double jitterMs) {
                if (binding == null) return;
                lastPing = pingMs;
                binding.pingText.setText(pingMs + " ms");
            }

            @Override
            public void onDownloadProgress(double mbps) {
                updateSpeedDisplay(mbps);
            }

            @Override
            public void onDownloadFinished(double finalMbps) {
                if (binding == null) return;
                lastDownload = finalMbps;
                binding.downloadText.setText(String.format(Locale.getDefault(), "%.1f", finalMbps));
            }

            @Override
            public void onUploadProgress(double mbps) {
                updateSpeedDisplay(mbps);
            }

            @Override
            public void onUploadFinished(double finalMbps) {
                saveResult(lastPing, lastDownload, finalMbps);
                if (binding == null) return;
                binding.uploadText.setText(String.format(Locale.getDefault(), "%.1f", finalMbps));
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
