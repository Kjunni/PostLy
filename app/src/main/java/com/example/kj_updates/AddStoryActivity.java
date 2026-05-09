package com.example.kj_updates;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kj_updates.databinding.ActivityAddStoryBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddStoryActivity extends AppCompatActivity {

    private ActivityAddStoryBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddStoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        binding.buttonPostStory.setOnClickListener(v -> attemptUpload());
    }

    private void attemptUpload() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String text = String.valueOf(binding.inputStoryText.getText()).trim();
        if (text.isEmpty()) {
            Snackbar.make(binding.getRoot(), "Please enter some text", Snackbar.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        // Fetch username from Firestore users collection instead of relying on FirebaseUser.getDisplayName()
        firestore.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String username = "User";
                    if (documentSnapshot.exists()) {
                        String fetchedUsername = documentSnapshot.getString("username");
                        if (fetchedUsername != null && !fetchedUsername.isEmpty()) {
                            username = fetchedUsername;
                        }
                    } else if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                        username = user.getDisplayName();
                    }
                    saveStoryToFirestore(user, text, username);
                })
                .addOnFailureListener(e -> {
                    // Fallback to DisplayName or "User" if fetch fails
                    String username = user.getDisplayName() != null ? user.getDisplayName() : "User";
                    saveStoryToFirestore(user, text, username);
                });
    }

    private void saveStoryToFirestore(FirebaseUser user, String text, String username) {
        StoryItem story = new StoryItem(
                null,
                user.getUid(),
                username,
                text,
                Timestamp.now()
        );

        firestore.collection("stories")
                .add(story.toMap())
                .addOnSuccessListener(documentReference -> {
                    setLoading(false);
                    Snackbar.make(binding.getRoot(), "Story posted!", Snackbar.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Snackbar.make(binding.getRoot(), e.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean loading) {
        binding.progressStory.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.buttonPostStory.setEnabled(!loading);
        binding.inputStoryText.setEnabled(!loading);
    }
}
