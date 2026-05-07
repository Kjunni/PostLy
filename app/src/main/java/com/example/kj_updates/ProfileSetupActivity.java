package com.example.kj_updates;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kj_updates.databinding.ActivityProfileSetupBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileSetupActivity extends AppCompatActivity {

    private ActivityProfileSetupBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileSetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            showMessage(R.string.message_auth_required);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding.inputUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.textProfileAvatar.setText(initialsFor(String.valueOf(s)));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        binding.buttonSaveProfile.setOnClickListener(v -> saveProfile(user));
    }

    private void saveProfile(FirebaseUser user) {
        String username = String.valueOf(binding.inputUsername.getText()).trim();
        String bio = String.valueOf(binding.inputBio.getText()).trim();

        if (username.isEmpty() || bio.isEmpty()) {
            showMessage(R.string.message_fill_all_fields);
            return;
        }

        setLoading(true);
        saveUserDocument(user, username, bio, "");
    }

    private void saveUserDocument(FirebaseUser user, String username, String bio, String profileImageUrl) {
        UserProfile profile = new UserProfile(
                username,
                user.getEmail() == null ? "" : user.getEmail(),
                bio,
                profileImageUrl,
                Timestamp.now()
        );

        firestore.collection("users")
                .document(user.getUid())
                .set(profile.toMap())
                .addOnSuccessListener(unused -> {
                    setLoading(false);
                    Snackbar.make(binding.getRoot(), R.string.message_profile_saved, Snackbar.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showMessage(e.getLocalizedMessage());
                });
    }

    private void setLoading(boolean loading) {
        binding.progressProfile.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.buttonSaveProfile.setEnabled(!loading);
    }

    private String initialsFor(String name) {
        String trimmedName = name == null ? "" : name.trim();
        if (trimmedName.isEmpty()) {
            return getString(R.string.avatar_preview);
        }

        String[] parts = trimmedName.split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(Character.toUpperCase(part.charAt(0)));
            }
            if (initials.length() == 2) {
                break;
            }
        }
        return initials.length() == 0 ? getString(R.string.avatar_preview) : initials.toString();
    }

    private void showMessage(int resId) {
        Snackbar.make(binding.getRoot(), resId, Snackbar.LENGTH_LONG).show();
    }

    private void showMessage(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }
}
