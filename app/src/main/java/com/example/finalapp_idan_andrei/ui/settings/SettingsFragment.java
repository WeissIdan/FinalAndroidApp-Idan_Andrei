package com.example.finalapp_idan_andrei.ui.settings;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.finalapp_idan_andrei.databinding.FragmentSettingsBinding;
import com.example.finalapp_idan_andrei.logic.AppDatabase;
import com.example.finalapp_idan_andrei.logic.AppSettings;

/**
 * The Settings tab: lets the user change units (Mbps/MB per s), ping iteration count,
 * download test size, and clear saved history - all backed by the single {@link AppSettings}
 * row in Room. Every change is written back to Room immediately (no explicit "Save" button).
 */
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    // Never null: real values are loaded from Room in loadSettings() and overwrite this,
    // but listeners can fire before that finishes (e.g. bottom-nav view-state restore).
    private AppSettings currentSettings = new AppSettings();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        loadSettings();
        setupListeners();

        return binding.getRoot();
    }

    /** Reads the saved AppSettings row from Room on a background thread, then refreshes the UI. */
    private void loadSettings() {
        new Thread(() -> {
            AppSettings settings = AppDatabase.getInstance(requireContext()).speedTestDao().getSettings();
            if (settings == null) {
                settings = new AppSettings();
            }
            currentSettings = settings;

            mainHandler.post(this::updateUI);
        }).start();
    }

    /** Pushes currentSettings' values onto the switch/seekbars/labels. */
    private void updateUI() {
        if (binding == null) return;

        binding.switchUnits.setChecked(currentSettings.isUseMegabytes());

        // SeekBar progress is 0-based, but "5 pings" is the minimum we allow, hence the -5/+5 offset.
        int pingProgress = currentSettings.getPingIterations() - 5;
        binding.seekPingIterations.setProgress(pingProgress);
        binding.labelPingIterations.setText("Ping Iterations: " + currentSettings.getPingIterations());

        // Download size seekbar only has 3 discrete steps: 10MB / 50MB / 100MB.
        long size = currentSettings.getDownloadSizeBytes();
        int sizeProgress = 1; // Default 50MB
        if (size == 10000000) sizeProgress = 0;
        else if (size == 100000000) sizeProgress = 2;

        binding.seekDownloadSize.setProgress(sizeProgress);
        binding.labelDownloadSize.setText("Download Size: " + (size / 1000000) + " MB");
    }

    /** Wires each control to update currentSettings and persist it as soon as the user changes it. */
    private void setupListeners() {
        binding.switchUnits.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentSettings.setUseMegabytes(isChecked);
            saveSettings();
        });

        binding.seekPingIterations.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int iterations = progress + 5;
                binding.labelPingIterations.setText("Ping Iterations: " + iterations);
                // fromUser distinguishes a real user drag from a programmatic setProgress() call
                // (e.g. from updateUI()) - only persist on genuine user interaction.
                if (fromUser) {
                    currentSettings.setPingIterations(iterations);
                    saveSettings();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        binding.seekDownloadSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                long size;
                if (progress == 0) size = 10000000;      // 10MB
                else if (progress == 2) size = 100000000; // 100MB
                else size = 50000000;                    // 50MB

                binding.labelDownloadSize.setText("Download Size: " + (size / 1000000) + " MB");
                if (fromUser) {
                    currentSettings.setDownloadSizeBytes(size);
                    saveSettings();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        binding.btnClearHistory.setOnClickListener(v -> {
            new Thread(() -> {
                AppDatabase.getInstance(requireContext()).speedTestDao().deleteAll();
                mainHandler.post(() -> Toast.makeText(getContext(), "History Cleared", Toast.LENGTH_SHORT).show());
            }).start();
        });
    }

    /** Writes the current in-memory settings back to Room (REPLACEs the single settings row). */
    private void saveSettings() {
        new Thread(() -> {
            AppDatabase.getInstance(requireContext()).speedTestDao().saveSettings(currentSettings);
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
