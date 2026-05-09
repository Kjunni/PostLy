package com.example.kj_updates;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.kj_updates.databinding.ActivityStoryViewerBinding;

public class StoryViewerActivity extends AppCompatActivity {

    private ActivityStoryViewerBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int progress = 0;
    private static final int STORY_DURATION_MS = 5000;
    private static final int UPDATE_INTERVAL_MS = 50;

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            progress += (100 * UPDATE_INTERVAL_MS) / STORY_DURATION_MS;
            binding.progressStoryView.setProgress(progress);
            if (progress < 100) {
                handler.postDelayed(this, UPDATE_INTERVAL_MS);
            } else {
                finish();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStoryViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String username = getIntent().getStringExtra("username");
        String storyText = getIntent().getStringExtra("storyText");
        String time = getIntent().getStringExtra("time");
        int colorRes = getIntent().getIntExtra("colorRes", R.color.feed_primary);

        binding.textStoryViewUsername.setText(username);
        binding.textStoryViewTime.setText(time);
        binding.textStoryContent.setText(storyText);
        binding.textStoryContent.setBackgroundColor(ContextCompat.getColor(this, colorRes));

        binding.buttonCloseStory.setOnClickListener(v -> finish());

        handler.postDelayed(progressRunnable, UPDATE_INTERVAL_MS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(progressRunnable);
    }
}
