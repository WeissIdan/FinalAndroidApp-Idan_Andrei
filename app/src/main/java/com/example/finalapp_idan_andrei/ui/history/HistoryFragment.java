package com.example.finalapp_idan_andrei.ui.history;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.finalapp_idan_andrei.databinding.FragmentHistoryBinding;
import com.example.finalapp_idan_andrei.logic.AppDatabase;
import com.example.finalapp_idan_andrei.logic.SpeedTestResult;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The History tab: shows every past speed test as a list, backed by Room.
 * Re-queries the database every time the tab becomes visible (onResume), so a
 * test just run on the Speed Test tab shows up immediately here.
 */
public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private HistoryAdapter adapter;
    // Room queries are blocking disk I/O, so they always run here, off the main thread.
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);

        adapter = new HistoryAdapter();
        binding.historyRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.historyRecyclerView.setAdapter(adapter);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    /** Reads all saved results on a background thread, then updates the list/empty state on the main thread. */
    private void loadHistory() {
        executorService.execute(() -> {
            List<SpeedTestResult> results =
                    AppDatabase.getInstance(requireContext()).speedTestDao().getAllResults();
            mainHandler.post(() -> {
                // The view may have been destroyed while this background query was running
                // (e.g. user switched tabs again quickly) - bail out instead of crashing.
                if (binding == null) return;
                adapter.submitList(results);
                boolean isEmpty = results.isEmpty();
                binding.emptyStateText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                binding.historyRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop any in-flight/queued DB query now that this Fragment instance is gone for good.
        executorService.shutdownNow();
    }
}
