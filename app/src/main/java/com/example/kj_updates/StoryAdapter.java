package com.example.kj_updates;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kj_updates.databinding.ItemStoryBinding;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    private final List<StoryItem> items;

    public StoryAdapter(List<StoryItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStoryBinding binding = ItemStoryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new StoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        holder.bind(items.get(position));
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
            binding.textStoryName.setText(item.getName());
            binding.textStoryAvatar.setText(item.getInitials());
            binding.textStoryAvatar.setContentDescription(
                    binding.getRoot().getContext().getString(
                            R.string.content_description_story_avatar,
                            item.getName()
                    )
            );

            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.OVAL);
            background.setColor(ContextCompat.getColor(binding.getRoot().getContext(), item.getColorRes()));
            binding.textStoryAvatar.setBackground(background);
        }
    }
}
