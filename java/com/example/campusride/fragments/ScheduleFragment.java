package com.example.campusride.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.example.campusride.R;
import com.example.campusride.models.Route;
import java.util.ArrayList;
import java.util.List;

/**
 * Schedule Fragment - Display shuttle schedules and routes
 */
public class ScheduleFragment extends Fragment {

    // Views
    private TabLayout tabLayoutDays;
    private TextView tvOperatingHours;
    private RecyclerView rvRoutes;

    // Data
    private FirebaseFirestore db;
    private List<Route> routeList = new ArrayList<>();
    private RouteScheduleAdapter adapter;
    private boolean isWeekday = true;

    // Listener
    private ListenerRegistration routesListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews(view);

        // Setup tabs
        setupTabs();

        // Setup RecyclerView
        setupRecyclerView();

        // Load routes
        loadRoutes();

        return view;
    }

    /**
     * Initialize all views
     */
    private void initializeViews(View view) {
        tabLayoutDays = view.findViewById(R.id.tab_layout_days);
        tvOperatingHours = view.findViewById(R.id.tv_operating_hours);
        rvRoutes = view.findViewById(R.id.rv_routes);
    }

    /**
     * Setup tabs for weekday/weekend
     */
    private void setupTabs() {
        tabLayoutDays.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isWeekday = tab.getPosition() == 0;
                updateOperatingHours();
                filterRoutes();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        updateOperatingHours();
    }

    /**
     * Setup RecyclerView
     */
    private void setupRecyclerView() {
        adapter = new RouteScheduleAdapter(routeList);
        rvRoutes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRoutes.setAdapter(adapter);
    }

    /**
     * Update operating hours based on selected day
     */
    private void updateOperatingHours() {
        if (isWeekday) {
            tvOperatingHours.setText("6:00 AM - 10:00 PM");
        } else {
            tvOperatingHours.setText("8:00 AM - 8:00 PM");
        }
    }

    /**
     * Load routes from Firestore
     */
    private void loadRoutes() {
        routesListener = db.collection("routes")
                .whereEqualTo("isActive", true)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Error loading routes", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        routeList.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Route route = doc.toObject(Route.class);
                            if (route != null) {
                                routeList.add(route);
                            }
                        }
                        filterRoutes();
                    }
                });
    }

    /**
     * Filter routes based on selected day
     */
    private void filterRoutes() {
        List<Route> filteredRoutes = new ArrayList<>();
        for (Route route : routeList) {
            // If route is weekday only and we're viewing weekend, skip it
            if (route.isWeekdayOnly() && !isWeekday) {
                continue;
            }
            filteredRoutes.add(route);
        }

        adapter.updateRoutes(filteredRoutes);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (routesListener != null) {
            routesListener.remove();
        }
    }

    /**
     * Adapter for route schedules
     */
    private static class RouteScheduleAdapter extends RecyclerView.Adapter<RouteScheduleAdapter.ViewHolder> {

        private List<Route> routes;

        RouteScheduleAdapter(List<Route> routes) {
            this.routes = routes;
        }

        void updateRoutes(List<Route> newRoutes) {
            this.routes = newRoutes;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_route_schedule, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(routes.get(position));
        }

        @Override
        public int getItemCount() {
            return routes.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvRouteName;
            private final TextView tvRouteFrequency;
            private final TextView tvRouteStops;

            ViewHolder(View itemView) {
                super(itemView);
                tvRouteName = itemView.findViewById(R.id.tv_route_name);
                tvRouteFrequency = itemView.findViewById(R.id.tv_route_frequency);
                tvRouteStops = itemView.findViewById(R.id.tv_route_stops);
            }

            void bind(Route route) {
                tvRouteName.setText(route.getRouteName());
                tvRouteFrequency.setText(route.getFrequencyString());
                tvRouteStops.setText(route.getStopsString());
            }
        }
    }
}