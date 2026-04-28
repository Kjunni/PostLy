package com.example.kj_updates;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.kj_updates.databinding.FragmentFirstBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private FeedAdapter feedAdapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private ListenerRegistration postListener;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupStories();
        setupFeed();

        binding.buttonPost.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddPostActivity.class)));

        binding.buttonOpenTrending.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (SessionManager.isDemoMode(requireContext())) {
            showDemoFeed();
        } else {
            observePosts();
        }
    }

    private void setupStories() {
        List<StoryItem> stories = Arrays.asList(
                new StoryItem("Killer Junaid", "KJ", R.color.feed_story_blue),
                new StoryItem("Jamsheed", "JA", R.color.feed_story_gold),
                new StoryItem("Arbeen", "AR", R.color.feed_story_rose),
                new StoryItem("Sakib Bot", "SB", R.color.feed_story_green)
        );

        binding.recyclerStories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        binding.recyclerStories.setAdapter(new StoryAdapter(stories));
    }

    private void setupFeed() {
        feedAdapter = new FeedAdapter(new ArrayList<>(), this::toggleLike);
        binding.recyclerFeed.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerFeed.setAdapter(feedAdapter);
        binding.textFeedStatus.setText(R.string.feed_loading);
        binding.textFeedStatus.setVisibility(View.VISIBLE);
    }

    private void observePosts() {
        if (postListener != null) {
            postListener.remove();
        }

        binding.textFeedStatus.setText(R.string.feed_loading);
        binding.textFeedStatus.setVisibility(View.VISIBLE);

        postListener = firestore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (binding == null) {
                        return;
                    }

                    if (error != null) {
                        showDemoFeed();
                        Snackbar.make(binding.getRoot(), error.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    if (value == null || value.isEmpty()) {
                        feedAdapter.replaceItems(new ArrayList<>());
                        binding.textFeedStatus.setText(R.string.feed_empty);
                        binding.textFeedStatus.setVisibility(View.VISIBLE);
                        return;
                    }

                    List<DocumentSnapshot> documents = value.getDocuments();
                    FirebaseUser user = auth.getCurrentUser();
                    if (user == null) {
                        publishFeedItems(documents, new ArrayList<>());
                        return;
                    }

                    List<Task<DocumentSnapshot>> likeTasks = new ArrayList<>();
                    for (DocumentSnapshot document : documents) {
                        likeTasks.add(document.getReference()
                                .collection("likes")
                                .document(user.getUid())
                                .get());
                    }

                    Tasks.whenAllSuccess(likeTasks)
                            .addOnSuccessListener(results -> {
                                List<String> likedPostIds = new ArrayList<>();
                                for (Object result : results) {
                                    DocumentSnapshot likeDocument = (DocumentSnapshot) result;
                                    if (likeDocument.exists() && likeDocument.getReference().getParent() != null) {
                                        likedPostIds.add(likeDocument.getReference().getParent().getParent().getId());
                                    }
                                }
                                publishFeedItems(documents, likedPostIds);
                            })
                            .addOnFailureListener(e -> publishFeedItems(documents, new ArrayList<>()));
                });
    }

    private void publishFeedItems(List<DocumentSnapshot> documents, List<String> likedPostIds) {
        if (binding == null) {
            return;
        }

        List<FeedItem> liveItems = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            liveItems.add(toFeedItem(document, likedPostIds.contains(document.getId())));
        }
        feedAdapter.replaceItems(liveItems);
        binding.textFeedStatus.setVisibility(View.GONE);
    }

    private FeedItem toFeedItem(DocumentSnapshot document, boolean likedByCurrentUser) {
        String username = document.getString("username");
        String postText = document.getString("postText");
        String postImage = document.getString("postImage");
        Long likesCount = document.getLong("likesCount");
        Timestamp timestamp = document.getTimestamp("timestamp");

        String safeUsername = username == null || username.trim().isEmpty() ? "User" : username.trim();
        String safePostText = postText == null ? "" : postText.trim();
        String safeImage = postImage == null ? "" : postImage.trim();
        int safeLikes = likesCount == null ? 0 : likesCount.intValue();

        String title = safePostText.length() > 42 ? safePostText.substring(0, 42) + "..." : safePostText;
        String body = safePostText;
        if (title.equals(body)) {
            title = "";
        }

        return new FeedItem(
                document.getId(),
                FeedItem.Type.SOCIAL,
                safeUsername,
                buildRelativeTime(timestamp),
                title,
                body,
                safeImage.isEmpty() ? "Fresh post" : "Photo update",
                safeImage,
                initialsFor(safeUsername),
                safeLikes,
                0,
                0,
                colorForAuthor(safeUsername),
                likedByCurrentUser
        );
    }

    private void toggleLike(FeedItem item) {
        if (SessionManager.isDemoMode(requireContext())) {
            Snackbar.make(binding.getRoot(), R.string.message_demo_like_blocked, Snackbar.LENGTH_LONG).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Snackbar.make(binding.getRoot(), R.string.message_like_auth_required, Snackbar.LENGTH_LONG).show();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            return;
        }

        DocumentReference postRef = firestore.collection("posts").document(item.getPostId());
        DocumentReference likeRef = postRef.collection("likes").document(user.getUid());

        firestore.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot postSnapshot = transaction.get(postRef);
            DocumentSnapshot likeSnapshot = transaction.get(likeRef);

            long currentLikes = 0;
            Long storedLikes = postSnapshot.getLong("likesCount");
            if (storedLikes != null) {
                currentLikes = storedLikes;
            }

            if (likeSnapshot.exists()) {
                transaction.delete(likeRef);
                transaction.update(postRef, "likesCount", Math.max(0, currentLikes - 1));
            } else {
                transaction.set(likeRef, new LikeRecord(user.getUid()).toMap(), SetOptions.merge());
                transaction.update(postRef, "likesCount", currentLikes + 1);
            }
            transaction.update(postRef, "updatedAt", FieldValue.serverTimestamp());
            return null;
        }).addOnFailureListener(e -> {
            if (binding != null) {
                Snackbar.make(binding.getRoot(), e.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void showDemoFeed() {
        if (binding == null) {
            return;
        }

        if (postListener != null) {
            postListener.remove();
            postListener = null;
        }

        List<FeedItem> feedItems = Arrays.asList(
                new FeedItem(
                        "demo-kj",
                        FeedItem.Type.SOCIAL,
                        "Killer Junaid",
                        "@killerjunaid • 12m",
                        "Late-night mockups turned into a campus event kit",
                        "We tested the new volunteer poster set at the student center and the QR check-ins were smooth. Uploading the template pack tonight.",
                        "Design Sprint",
                        "",
                        "KJ",
                        84,
                        17,
                        6,
                        R.color.feed_story_rose,
                        false
                ),
                new FeedItem(
                        "demo-ja",
                        FeedItem.Type.SOCIAL,
                        "Jamsheed",
                        "@jamsheed • 28m",
                        "Evening bus route changes are driving a spike in live community updates",
                        "Commuters are sharing stop-by-stop delays, alternate pickup points, and rider photos. The transport desk says a formal schedule refresh is expected before 8 PM.",
                        "Transit Watch",
                        "",
                        "JA",
                        210,
                        48,
                        31,
                        R.color.feed_story_gold,
                        false
                ),
                new FeedItem(
                        "demo-ar",
                        FeedItem.Type.SOCIAL,
                        "Arbeen",
                        "@arbeen • 43m",
                        "We just opened beta signups for the neighborhood repair map",
                        "The idea is simple: pin a streetlight, crossing, or water issue and let nearby residents confirm status in-thread instead of sending ten separate messages.",
                        "Beta Launch",
                        "",
                        "AR",
                        126,
                        22,
                        14,
                        R.color.feed_story_blue,
                        false
                ),
                new FeedItem(
                        "demo-sb",
                        FeedItem.Type.SOCIAL,
                        "Sakib Bot",
                        "@sakibbot • 1h",
                        "Student creators are turning short-form updates into a local news habit",
                        "Mini explainers, eyewitness clips, and one-paragraph summaries are outperforming long posts across several campus and district circles this afternoon.",
                        "Creator Economy",
                        "",
                        "SB",
                        173,
                        39,
                        24,
                        R.color.feed_story_green,
                        false
                )
        );

        feedAdapter.replaceItems(feedItems);
        binding.textFeedStatus.setVisibility(View.GONE);
    }

    private String buildRelativeTime(Timestamp timestamp) {
        if (timestamp == null) {
            return "just now";
        }

        long diff = System.currentTimeMillis() - timestamp.toDate().getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (minutes < 1) {
            return "just now";
        }
        if (minutes < 60) {
            return minutes + "m";
        }
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        if (hours < 24) {
            return hours + "h";
        }
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        return days + "d";
    }

    private String initialsFor(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase(Locale.US);
        }
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase(Locale.US);
    }

    private int colorForAuthor(String name) {
        int bucket = Math.abs(name.hashCode()) % 4;
        switch (bucket) {
            case 0:
                return R.color.feed_story_blue;
            case 1:
                return R.color.feed_story_gold;
            case 2:
                return R.color.feed_story_rose;
            default:
                return R.color.feed_story_green;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (postListener != null) {
            postListener.remove();
            postListener = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
