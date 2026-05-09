package com.example.kj_updates;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.kj_updates.databinding.FragmentProfileBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private FeedAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupRecyclerView();
        loadProfile();
        loadMyPosts();

        binding.buttonEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ProfileSetupActivity.class));
        });
    }

    private void setupRecyclerView() {
        adapter = new FeedAdapter(new ArrayList<>(), this::toggleLike, new FeedAdapter.OnPostActionListener() {
            @Override
            public void onEditPost(FeedItem item) {
                // We can reuse logic or just show a message. 
                // For simplicity, let's just implement Delete for now.
            }

            @Override
            public void onDeletePost(FeedItem item) {
                deletePost(item);
            }
        });
        binding.recyclerMyPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerMyPosts.setAdapter(adapter);
    }

    private void deletePost(FeedItem item) {
        firestore.collection("posts").document(item.getPostId()).delete()
                .addOnSuccessListener(unused -> {
                    loadMyPosts(); // Refresh
                    Snackbar.make(binding.getRoot(), "Post deleted", Snackbar.LENGTH_SHORT).show();
                });
    }

    private void toggleLike(FeedItem item) {
        // Like logic can be copied from FirstFragment if needed
    }

    private void loadProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        firestore.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (binding == null) return;
                    if (doc.exists()) {
                        String username = doc.getString("username");
                        String bio = doc.getString("bio");
                        binding.textProfileUsername.setText(username);
                        binding.textProfileBio.setText(bio);
                        binding.textProfileEmail.setText(user.getEmail());
                        binding.textProfileAvatar.setText(initialsFor(username));
                    }
                });
    }

    private void loadMyPosts() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        firestore.collection("posts")
                .whereEqualTo("userId", user.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (binding == null) return;
                    List<FeedItem> items = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        items.add(toFeedItem(doc));
                    }
                    adapter.replaceItems(items);
                    binding.textProfileStatus.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    if (binding != null) {
                        Snackbar.make(binding.getRoot(), e.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private FeedItem toFeedItem(DocumentSnapshot document) {
        String username = document.getString("username");
        String postText = document.getString("postText");
        Long likesCount = document.getLong("likesCount");

        return new FeedItem(
                document.getId(),
                document.getString("userId"),
                FeedItem.Type.SOCIAL,
                username,
                "Just now", // Simplified
                "",
                postText,
                "My Post",
                initialsFor(username),
                likesCount == null ? 0 : likesCount.intValue(),
                0, 0, 
                R.color.feed_story_blue,
                false, true
        );
    }

    private String initialsFor(String name) {
        if (name == null || name.isEmpty()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase(Locale.US);
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase(Locale.US);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
