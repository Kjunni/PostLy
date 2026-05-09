package com.example.kj_updates;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kj_updates.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        showMessage("Google sign in failed: " + e.getMessage());
                    }
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Configure Google Sign In
        // Note: default_web_client_id is automatically generated if Google Auth is enabled in Firebase Console
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.buttonLogin.setOnClickListener(v -> attemptLogin());
        binding.buttonGoogleLogin.setOnClickListener(v -> signInWithGoogle());
        binding.textGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        setLoading(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> routeAuthenticatedUser(authResult.getUser().getUid()))
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showMessage(e.getLocalizedMessage());
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth == null ? null : auth.getCurrentUser();
        if (user != null) {
            routeAuthenticatedUser(user.getUid());
        }
    }

    private void attemptLogin() {
        String email = String.valueOf(binding.inputEmail.getText()).trim();
        String password = String.valueOf(binding.inputPassword.getText()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            showMessage(R.string.message_fill_all_fields);
            return;
        }

        setLoading(true);
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> routeAuthenticatedUser(result.getUser().getUid()))
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showMessage(e.getLocalizedMessage());
                });
    }

    private void routeAuthenticatedUser(String uid) {
        setLoading(true);
        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    setLoading(false);
                    if (documentSnapshot.exists()) {
                        startActivity(new Intent(this, MainActivity.class));
                    } else {
                        startActivity(new Intent(this, ProfileSetupActivity.class));
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showMessage(e.getLocalizedMessage());
                });
    }

    private void setLoading(boolean loading) {
        binding.progressLogin.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.buttonLogin.setEnabled(!loading);
        binding.buttonGoogleLogin.setEnabled(!loading);
    }

    private void showMessage(int resId) {
        Snackbar.make(binding.getRoot(), resId, Snackbar.LENGTH_LONG).show();
    }

    private void showMessage(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }
}
