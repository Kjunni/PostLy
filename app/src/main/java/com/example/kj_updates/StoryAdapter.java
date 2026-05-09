package com.example.kj_updates;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kj_updates.databinding.ItemStoryBinding;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<StoryItem> items;
    private static final int VIEW_TYPE_ADD = 0;
    private static final int VIEW_TYPE_STORY = 1;

    public StoryAdapter(List<StoryItem> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getUserId() == null ? VIEW_TYPE_ADD : VIEW_TYPE_STORY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStoryBinding binding = ItemStoryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new StoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((StoryViewHolder) holder).bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class StoryViewHolder extends RecyclerView.ViewHolder {

        private final ItemStoryBinding binding;

        StoryViewHolder(ItemStoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(StoryItem item) {
            if (item.getUserId() == null) {
                // Add Story button
                binding.textStoryName.setText("Add Story");
                binding.textStoryAvatar.setText("+");
                GradientDrawable background = new GradientDrawable();
                background.setShape(GradientDrawable.OVAL);
                background.setColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.feed_primary));
                binding.textStoryAvatar.setBackground(background);

                binding.getRoot().setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), AddStoryActivity.class);
                    v.getContext().startActivity(intent);
                });
            } else {
                // Regular Story
                binding.textStoryName.setText(item.getUsername());
                binding.textStoryAvatar.setText(item.getInitials());

                GradientDrawable background = new GradientDrawable();
                background.setShape(GradientDrawable.OVAL);
                background.setColor(ContextCompat.getColor(binding.getRoot().getContext(), item.getColorRes()));
                binding.textStoryAvatar.setBackground(background);

                binding.getRoot().setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), StoryViewerActivity.class);
                    intent.putExtra("username", item.getUsername());
                    intent.putExtra("storyText", item.getStoryText());
                    intent.putExtra("colorRes", item.getColorRes());
                    intent.putExtra("time", "Today");
                    v.getContext().startActivity(intent);
                });
            }
        }
    }
}
