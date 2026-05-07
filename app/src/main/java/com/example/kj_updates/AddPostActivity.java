package com.example.kj_updates;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kj_updates.databinding.ActivityAddPostBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddPostActivity extends AppCompatActivity {

    private ActivityAddPostBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        binding.buttonSubmitPost.setOnClickListener(v -> submitPost());
    }

    private void submitPost() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            showMessage(R.string.message_auth_required);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String postText = String.valueOf(binding.inputPostText.getText()).trim();
        if (postText.isEmpty()) {
            showMessage(R.string.message_post_needs_content);
            return;
        }

        setLoading(true);
        firestore.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String username = documentSnapshot.getString("username");
                    if (username == null || username.trim().isEmpty()) {
                        username = user.getEmail() == null ? "User" : user.getEmail();
                    }
                    createPostRecord(user.getUid(), username, postText);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showMessage(e.getLocalizedMessage());
                });
    }

    private void createPostRecord(String userId, String username, String postText) {
        String postId = firestore.collection("posts").document().getId();
        savePostDocument(postId, userId, username, postText, "");
    }

    private void savePostDocument(
            String postId,
            String userId,
            String username,
            String postText,
            String postImageUrl
    ) {
        Post post = new Post(
                userId,
                username,
                postText,
                postImageUrl,
                Timestamp.now(),
                0
        );

        firestore.collection("posts")
                .document(postId)
                .set(post.toMap())
                .addOnSuccessListener(unused -> {
                    setLoading(false);
                    Snackbar.make(binding.getRoot(), R.string.message_post_created, Snackbar.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showMessage(e.getLocalizedMessage());
                });
    }

    private void setLoading(boolean loading) {
        binding.progressPost.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.buttonSubmitPost.setEnabled(!loading);
    }

    private void showMessage(int resId) {
        Snackbar.make(binding.getRoot(), resId, Snackbar.LENGTH_LONG).show();
    }

    private void showMessage(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }
}
