package com.example.campusride.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.example.campusride.R;
import com.example.campusride.fragments.FavoritesFragment;
import com.example.campusride.fragments.MapFragment;
import com.example.campusride.fragments.ReportFragment;
import com.example.campusride.fragments.ScheduleFragment;

/**
 * Main Activity - Student Interface
 * Container for main fragments with bottom navigation
 */
public class MainActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigation;
    private FragmentManager fragmentManager;

    // Fragment instances
    private MapFragment mapFragment;
    private FavoritesFragment favoritesFragment;
    private ScheduleFragment scheduleFragment;
    private ReportFragment reportFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        initializeViews();

        // Setup toolbar
        setupToolbar();

        // Initialize fragments
        initializeFragments();

        // Setup bottom navigation
        setupBottomNavigation();

        // Show default fragment (Map)
        if (savedInstanceState == null) {
            showFragment(mapFragment);
        }
    }

    /**
     * Initialize all views
     */
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();
    }

    /**
     * Setup toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setSubtitle(R.string.app_subtitle);
        }
    }

    /**
     * Initialize all fragments
     */
    private void initializeFragments() {
        mapFragment = new MapFragment();
        favoritesFragment = new FavoritesFragment();
        scheduleFragment = new ScheduleFragment();
        reportFragment = new ReportFragment();

        // Add all fragments but hide them initially
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.fragment_container, mapFragment, "MAP");
        transaction.add(R.id.fragment_container, favoritesFragment, "FAVORITES").hide(favoritesFragment);
        transaction.add(R.id.fragment_container, scheduleFragment, "SCHEDULE").hide(scheduleFragment);
        transaction.add(R.id.fragment_container, reportFragment, "REPORT").hide(reportFragment);
        transaction.commit();

        activeFragment = mapFragment;
    }

    /**
     * Setup bottom navigation
     */
    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_map) {
                showFragment(mapFragment);
                updateToolbarTitle(getString(R.string.nav_map));
                return true;
            } else if (itemId == R.id.navigation_favorites) {
                showFragment(favoritesFragment);
                updateToolbarTitle(getString(R.string.nav_favorites));
                return true;
            } else if (itemId == R.id.navigation_schedule) {
                showFragment(scheduleFragment);
                updateToolbarTitle(getString(R.string.nav_schedule));
                return true;
            } else if (itemId == R.id.navigation_report) {
                showFragment(reportFragment);
                updateToolbarTitle(getString(R.string.nav_report));
                return true;
            }

            return false;
        });
    }

    /**
     * Show a fragment and hide the current one
     */
    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.hide(activeFragment);
        transaction.show(fragment);
        transaction.commit();
        activeFragment = fragment;
    }

    /**
     * Update toolbar title
     */
    private void updateToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_notifications) {
            // TODO: Show notifications
            return true;
        } else if (itemId == R.id.action_settings) {
            // TODO: Open settings
            return true;
        } else if (itemId == R.id.action_profile) {
            // Check if user is logged in
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                // TODO: Show profile
            } else {
                // Navigate to login
                startActivity(new Intent(this, LoginActivity.class));
            }
            return true;
        } else if (itemId == R.id.action_about) {
            // TODO: Show about dialog
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If not on map fragment, go back to map
        if (activeFragment != mapFragment) {
            bottomNavigation.setSelectedItemId(R.id.navigation_map);
        } else {
            super.onBackPressed();
        }
    }
}