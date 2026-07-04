package com.example.finalapp_idan_andrei.ui.history;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalapp_idan_andrei.databinding.ItemHistoryResultBinding;
import com.example.finalapp_idan_andrei.logic.SpeedTestResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<SpeedTestResult> results = new ArrayList<>();
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault());

    public void submitList(List<SpeedTestResult> newResults) {
        results.clear();
        results.addAll(newResults);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryResultBinding binding = ItemHistoryResultBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SpeedTestResult result = results.get(position);
        holder.binding.timestampText.setText(dateFormat.format(new Date(result.getTimestamp())));
        holder.binding.pingText.setText(result.getPingMs() + " ms");
        holder.binding.downloadText.setText(
                String.format(Locale.getDefault(), "%.1f Mbps", result.getDownloadMbps()));
        holder.binding.uploadText.setText(
                String.format(Locale.getDefault(), "%.1f Mbps", result.getUploadMbps()));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemHistoryResultBinding binding;

        ViewHolder(ItemHistoryResultBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
