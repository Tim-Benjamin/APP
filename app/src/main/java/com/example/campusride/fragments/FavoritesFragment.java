package com.example.campusride.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.example.campusride.R;
import com.example.campusride.models.Stop;
import com.example.campusride.models.User;
import java.util.ArrayList;
import java.util.List;

/**
 * Favorites Fragment - Manage favorite stops
 */
public class FavoritesFragment extends Fragment {

    // Views
    private RecyclerView rvFavoriteStops;
    private View layoutEmptyFavorites;

    // Data
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private User user;

    private List<Stop> favoriteStops = new ArrayList<>();
    private FavoriteStopsAdapter adapter;

    // Listeners
    private ListenerRegistration userListener;
    private ListenerRegistration stopsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize views
        initializeViews(view);

        // Setup RecyclerView
        setupRecyclerView();

        // Load user data
        if (currentUser != null) {
            loadUserData();
        } else {
            showEmptyState();
        }

        return view;
    }

    /**
     * Initialize all views
     */
    private void initializeViews(View view) {
        rvFavoriteStops = view.findViewById(R.id.rv_favorite_stops);
        layoutEmptyFavorites = view.findViewById(R.id.layout_empty_favorites);
    }

    /**
     * Setup RecyclerView
     */
    private void setupRecyclerView() {
        adapter = new FavoriteStopsAdapter(favoriteStops, new FavoriteStopsAdapter.OnStopClickListener() {
            @Override
            public void onStopClick(Stop stop) {
                // TODO: Navigate to map and show this stop
                Toast.makeText(getContext(), "Selected: " + stop.getStopName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRemoveClick(Stop stop) {
                removeFavoriteStop(stop);
            }
        });

        rvFavoriteStops.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFavoriteStops.setAdapter(adapter);
    }

    /**
     * Load user data
     */
    private void loadUserData() {
        String userId = currentUser.getUid();

        userListener = db.collection("users")
                .document(userId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        user = snapshot.toObject(User.class);
                        if (user != null && user.getFavoriteStops() != null && !user.getFavoriteStops().isEmpty()) {
                            loadFavoriteStops(user.getFavoriteStops());
                        } else {
                            showEmptyState();
                        }
                    } else {
                        showEmptyState();
                    }
                });
    }

    /**
     * Load favorite stops from Firestore
     */
    private void loadFavoriteStops(List<String> favoriteStopIds) {
        stopsListener = db.collection("stops")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (snapshots != null) {
                        favoriteStops.clear();

                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Stop stop = doc.toObject(Stop.class);
                            if (stop != null && favoriteStopIds.contains(stop.getStopId())) {
                                favoriteStops.add(stop);
                            }
                        }

                        updateUI();
                    }
                });
    }

    /**
     * Remove favorite stop
     */
    private void removeFavoriteStop(Stop stop) {
        if (user == null || currentUser == null) return;

        user.removeFavoriteStop(stop.getStopId());

        db.collection("users").document(currentUser.getUid())
                .update("favoriteStops", user.getFavoriteStops())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to remove", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Update UI
     */
    private void updateUI() {
        if (favoriteStops.isEmpty()) {
            showEmptyState();
        } else {
            layoutEmptyFavorites.setVisibility(View.GONE);
            rvFavoriteStops.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Show empty state
     */
    private void showEmptyState() {
        layoutEmptyFavorites.setVisibility(View.VISIBLE);
        rvFavoriteStops.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userListener != null) {
            userListener.remove();
        }
        if (stopsListener != null) {
            stopsListener.remove();
        }
    }

    /**
     * Adapter for favorite stops
     */
    private static class FavoriteStopsAdapter extends RecyclerView.Adapter<FavoriteStopsAdapter.ViewHolder> {

        private final List<Stop> stops;
        private final OnStopClickListener listener;

        interface OnStopClickListener {
            void onStopClick(Stop stop);
            void onRemoveClick(Stop stop);
        }

        FavoriteStopsAdapter(List<Stop> stops, OnStopClickListener listener) {
            this.stops = stops;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_favorite_stop, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(stops.get(position), listener);
        }

        @Override
        public int getItemCount() {
            return stops.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final android.widget.TextView tvStopName;
            private final android.widget.TextView tvNextShuttle;
            private final android.widget.ImageView ivRemoveFavorite;

            ViewHolder(View itemView) {
                super(itemView);
                tvStopName = itemView.findViewById(R.id.tv_stop_name);
                tvNextShuttle = itemView.findViewById(R.id.tv_next_shuttle);
                ivRemoveFavorite = itemView.findViewById(R.id.iv_remove_favorite);
            }

            void bind(Stop stop, OnStopClickListener listener) {
                tvStopName.setText(stop.getStopName());
                tvNextShuttle.setText("Tap to view shuttles");

                itemView.setOnClickListener(v -> listener.onStopClick(stop));
                ivRemoveFavorite.setOnClickListener(v -> listener.onRemoveClick(stop));
            }
        }
    }
}