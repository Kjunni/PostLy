package com.example.kj_updates;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kj_updates.databinding.ActivityProfileSetupBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileSetupActivity extends AppCompatActivity {

    private ActivityProfileSetupBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private Uri selectedImageUri;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileSetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        binding.imageProfile.setImageURI(uri);
                    } else {
                        showMessage(R.string.message_choose_image_failed);
                    }
                }
        );

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            showMessage(R.string.message_auth_required);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding.buttonPickImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
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
        if (selectedImageUri == null) {
            saveUserDocument(user, username, bio, "");
            return;
        }

        StorageReference imageRef = storage.getReference()
                .child("profile_images")
                .child(user.getUid() + ".jpg");

        imageRef.putFile(selectedImageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful() && task.getException() != null) {
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> saveUserDocument(user, username, bio, uri.toString()))
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showMessage(e.getLocalizedMessage());
                });
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
        binding.buttonPickImage.setEnabled(!loading);
    }

    private void showMessage(int resId) {
        Snackbar.make(binding.getRoot(), resId, Snackbar.LENGTH_LONG).show();
    }

    private void showMessage(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }
}
