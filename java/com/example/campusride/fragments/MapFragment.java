package com.example.campusride.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.example.campusride.R;
import com.example.campusride.adapters.ShuttleAdapter;
import com.example.campusride.models.Shuttle;
import com.example.campusride.models.Stop;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Map Fragment - Display real-time shuttle locations
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final LatLng UCC_LOCATION = new LatLng(5.1053, -1.2882); // UCC coordinates
    private static final float DEFAULT_ZOOM = 15f;

    // Views
    private Spinner spinnerStops;
    private RecyclerView rvShuttles;
    private FloatingActionButton fabMyLocation;
    private View layoutEmptyState;
    private View loadingOverlay;

    // Map
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private Map<String, Marker> shuttleMarkers = new HashMap<>();

    // Data
    private FirebaseFirestore db;
    private List<Shuttle> shuttleList = new ArrayList<>();
    private List<Stop> stopList = new ArrayList<>();
    private ShuttleAdapter shuttleAdapter;
    private Stop selectedStop;

    // Listeners
    private ListenerRegistration shuttlesListener;
    private ListenerRegistration stopsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews(view);

        // Setup map
        setupMap();

        // Setup RecyclerView
        setupRecyclerView();

        // Load stops
        loadStops();

        // Setup listeners
        setupListeners();

        return view;
    }

    /**
     * Initialize all views
     */
    private void initializeViews(View view) {
        spinnerStops = view.findViewById(R.id.spinner_stops);
        rvShuttles = view.findViewById(R.id.rv_shuttles);
        fabMyLocation = view.findViewById(R.id.fab_my_location);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        loadingOverlay = view.findViewById(R.id.loading_overlay);
    }

    /**
     * Setup Google Map
     */
    private void setupMap() {
        mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Setup RecyclerView
     */
    private void setupRecyclerView() {
        shuttleAdapter = new ShuttleAdapter(shuttleList, shuttle -> {
            // Handle shuttle click - show on map
            if (mMap != null && shuttle.getCurrentLocation() != null) {
                LatLng location = new LatLng(
                        shuttle.getCurrentLocation().getLatitude(),
                        shuttle.getCurrentLocation().getLongitude()
                );
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 17f));
            }
        });

        rvShuttles.setLayoutManager(new LinearLayoutManager(getContext()));
        rvShuttles.setAdapter(shuttleAdapter);
    }

    /**
     * Setup click listeners
     */
    private void setupListeners() {
        fabMyLocation.setOnClickListener(v -> {
            if (mMap != null) {
                if (checkLocationPermission()) {
                    mMap.setMyLocationEnabled(true);
                    // Center on UCC campus
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UCC_LOCATION, DEFAULT_ZOOM));
                } else {
                    requestLocationPermission();
                }
            }
        });
    }

    /**
     * Load stops from Firestore
     */
    private void loadStops() {
        stopsListener = db.collection("stops")
                .whereEqualTo("isActive", true)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Error loading stops", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        stopList.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Stop stop = doc.toObject(Stop.class);
                            if (stop != null) {
                                stopList.add(stop);
                            }
                        }
                        updateStopsSpinner();
                    }
                });
    }

    /**
     * Update stops spinner
     */
    private void updateStopsSpinner() {
        if (getContext() == null) return;

        List<String> stopNames = new ArrayList<>();
        stopNames.add("Select your stop");
        for (Stop stop : stopList) {
            stopNames.add(stop.getStopName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                stopNames
        );
        spinnerStops.setAdapter(adapter);

        spinnerStops.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position <= stopList.size()) {
                    selectedStop = stopList.get(position - 1);
                    loadShuttles();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    /**
     * Load shuttles from Firestore
     */
    private void loadShuttles() {
        showLoading(true);

        shuttlesListener = db.collection("shuttles")
                .addSnapshotListener((snapshots, error) -> {
                    showLoading(false);

                    if (error != null) {
                        Toast.makeText(getContext(), "Error loading shuttles", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        shuttleList.clear();

                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Shuttle shuttle = doc.toObject(Shuttle.class);
                            if (shuttle != null && shuttle.isAvailable()) {
                                // Calculate distance and ETA if stop is selected
                                if (selectedStop != null && shuttle.getCurrentLocation() != null) {
                                    double distance = selectedStop.distanceTo(
                                            shuttle.getCurrentLocation().getLatitude(),
                                            shuttle.getCurrentLocation().getLongitude()
                                    );
                                    shuttle.setDistanceToStop(distance);

                                    // Estimate ETA (assuming 30 km/h average speed)
                                    int etaMinutes = (int) Math.ceil((distance / 30.0) * 60);
                                    shuttle.setEtaMinutes(etaMinutes);
                                }

                                shuttleList.add(shuttle);
                            }
                        }

                        // Sort by distance/ETA
                        shuttleList.sort((s1, s2) ->
                                Double.compare(s1.getDistanceToStop(), s2.getDistanceToStop()));

                        updateUI();
                        updateMapMarkers();
                    }
                });
    }

    /**
     * Update UI based on data
     */
    private void updateUI() {
        if (shuttleList.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvShuttles.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvShuttles.setVisibility(View.VISIBLE);
            shuttleAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Update shuttle markers on map
     */
    private void updateMapMarkers() {
        if (mMap == null) return;

        // Clear old markers
        for (Marker marker : shuttleMarkers.values()) {
            marker.remove();
        }
        shuttleMarkers.clear();

        // Add new markers
        for (Shuttle shuttle : shuttleList) {
            if (shuttle.getCurrentLocation() != null) {
                LatLng location = new LatLng(
                        shuttle.getCurrentLocation().getLatitude(),
                        shuttle.getCurrentLocation().getLongitude()
                );

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(location)
                        .title(shuttle.getShuttleName())
                        .snippet(shuttle.getCurrentRoute() + " - " + shuttle.getStatusString())
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                shuttle.isActive() ? BitmapDescriptorFactory.HUE_GREEN :
                                        BitmapDescriptorFactory.HUE_ORANGE
                        ));

                Marker marker = mMap.addMarker(markerOptions);
                if (marker != null) {
                    shuttleMarkers.put(shuttle.getShuttleId(), marker);
                }
            }
        }

        // Add stop markers
        if (selectedStop != null && selectedStop.getLocation() != null) {
            LatLng stopLocation = new LatLng(
                    selectedStop.getLocation().getLatitude(),
                    selectedStop.getLocation().getLongitude()
            );

            mMap.addMarker(new MarkerOptions()
                    .position(stopLocation)
                    .title(selectedStop.getStopName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Configure map
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // Move camera to UCC
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UCC_LOCATION, DEFAULT_ZOOM));

        // Check location permission
        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
        }
    }

    /**
     * Check location permission
     */
    private boolean checkLocationPermission() {
        if (getContext() == null) return false;
        return ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request location permission
     */
    private void requestLocationPermission() {
        if (getActivity() != null) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    /**
     * Show/hide loading overlay
     */
    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove listeners
        if (shuttlesListener != null) {
            shuttlesListener.remove();
        }
        if (stopsListener != null) {
            stopsListener.remove();
        }
    }
}