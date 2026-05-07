package com.example.kj_updates;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kj_updates.databinding.ItemFeedCardBinding;

import java.util.ArrayList;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    public interface OnLikeClickListener {
        void onLikeClicked(FeedItem item);
    }

    public interface OnPostActionListener {
        void onEditPost(FeedItem item);

        void onDeletePost(FeedItem item);
    }

    private final List<FeedItem> items = new ArrayList<>();
    private final OnLikeClickListener onLikeClickListener;
    private final OnPostActionListener onPostActionListener;

    public FeedAdapter(
            List<FeedItem> items,
            OnLikeClickListener onLikeClickListener,
            OnPostActionListener onPostActionListener
    ) {
        this.onLikeClickListener = onLikeClickListener;
        this.onPostActionListener = onPostActionListener;
        replaceItems(items);
    }

    public void replaceItems(List<FeedItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFeedCardBinding binding = ItemFeedCardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FeedViewHolder(binding, onLikeClickListener, onPostActionListener);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FeedViewHolder extends RecyclerView.ViewHolder {

        private final ItemFeedCardBinding binding;
        private final OnLikeClickListener onLikeClickListener;
        private final OnPostActionListener onPostActionListener;

        FeedViewHolder(
                ItemFeedCardBinding binding,
                OnLikeClickListener onLikeClickListener,
                OnPostActionListener onPostActionListener
        ) {
            super(binding.getRoot());
            this.binding = binding;
            this.onLikeClickListener = onLikeClickListener;
            this.onPostActionListener = onPostActionListener;
        }

        void bind(FeedItem item) {
            binding.textAuthor.setText(item.getAuthor());
            binding.textMeta.setText(item.getMeta());
            binding.textTitle.setText(item.getTitle());
            binding.textBody.setText(item.getBody());
            binding.textAvatar.setText(item.getInitials());
            binding.textComments.setText(binding.getRoot().getContext().getString(R.string.stat_comments, item.getComments()));
            binding.textShares.setText(binding.getRoot().getContext().getString(R.string.stat_shares, item.getShares()));
            binding.textLikes.setText(binding.getRoot().getContext().getString(
                    item.isLikedByCurrentUser() ? R.string.stat_unlike_action : R.string.stat_like_action,
                    item.getLikes()
            ));

            int likeColor = item.isLikedByCurrentUser()
                    ? ContextCompat.getColor(binding.getRoot().getContext(), R.color.feed_accent)
                    : ContextCompat.getColor(binding.getRoot().getContext(), R.color.feed_text_secondary);
            binding.textLikes.setTextColor(likeColor);
            binding.textLikes.setOnClickListener(v -> onLikeClickListener.onLikeClicked(item));
            binding.buttonEditPost.setVisibility(item.isOwnedByCurrentUser() ? View.VISIBLE : View.GONE);
            binding.buttonDeletePost.setVisibility(item.isOwnedByCurrentUser() ? View.VISIBLE : View.GONE);
            binding.buttonEditPost.setOnClickListener(v -> onPostActionListener.onEditPost(item));
            binding.buttonDeletePost.setOnClickListener(v -> onPostActionListener.onDeletePost(item));

            binding.textTitle.setVisibility(item.getTitle().isEmpty() ? View.GONE : View.VISIBLE);
            binding.textBody.setVisibility(item.getBody().isEmpty() ? View.GONE : View.VISIBLE);
            binding.textBanner.setVisibility(item.getBannerText().isEmpty() ? View.GONE : View.VISIBLE);

            if (!item.getBannerText().isEmpty()) {
                binding.textBanner.setText(item.getBannerText());
                binding.textBanner.setContentDescription(
                        binding.getRoot().getContext().getString(
                                R.string.content_description_feed_banner,
                                item.getBannerText()
                        )
                );
            }

            binding.imagePost.setVisibility(View.GONE);

            int surfaceColor = ContextCompat.getColor(binding.getRoot().getContext(), item.getColorRes());
            int badgeColor = item.getType() == FeedItem.Type.NEWS
                    ? ContextCompat.getColor(binding.getRoot().getContext(), R.color.feed_accent_soft)
                    : ContextCompat.getColor(binding.getRoot().getContext(), R.color.feed_story_green);

            binding.textBadge.setText(item.getType() == FeedItem.Type.NEWS
                    ? R.string.feed_badge_news
                    : R.string.feed_badge_social);

            binding.textAvatar.setBackground(createRoundedShape(surfaceColor, GradientDrawable.OVAL, 0));
            binding.textBanner.setBackground(createRoundedShape(surfaceColor, GradientDrawable.RECTANGLE, 28));
            binding.textBadge.setBackground(createRoundedShape(badgeColor, GradientDrawable.RECTANGLE, 999));
        }

        private GradientDrawable createRoundedShape(int color, int shape, int radiusDp) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(shape);
            drawable.setColor(color);
            if (shape == GradientDrawable.RECTANGLE) {
                float density = binding.getRoot().getResources().getDisplayMetrics().density;
                drawable.setCornerRadius(radiusDp * density);
            }
            return drawable;
        }
    }
}
