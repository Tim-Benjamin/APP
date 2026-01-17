package com.example.campusride.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.campusride.R;
import com.example.campusride.models.Report;
import com.example.campusride.models.Shuttle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Fragment - Submit issue reports
 */
public class ReportFragment extends Fragment {

    // Views
    private AutoCompleteTextView actvShuttleSelector;
    private AutoCompleteTextView actvIssueType;
    private ChipGroup chipGroupIssues;
    private TextInputEditText etDescription;
    private MaterialButton btnSubmitReport;
    private View snackbarAnchor;

    // Data
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private List<Shuttle> shuttleList = new ArrayList<>();
    private Map<String, String> shuttleMap = new HashMap<>();
    private String selectedShuttleId;
    private Report.IssueType selectedIssueType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize views
        initializeViews(view);

        // Load shuttles
        loadShuttles();

        // Setup issue types
        setupIssueTypes();

        // Setup chip group
        setupChipGroup();

        // Setup submit button
        setupSubmitButton();

        return view;
    }

    /**
     * Initialize all views
     */
    private void initializeViews(View view) {
        actvShuttleSelector = view.findViewById(R.id.actv_shuttle_selector);
        actvIssueType = view.findViewById(R.id.actv_issue_type);
        chipGroupIssues = view.findViewById(R.id.chip_group_issues);
        etDescription = view.findViewById(R.id.et_description);
        btnSubmitReport = view.findViewById(R.id.btn_submit_report);
        snackbarAnchor = view.findViewById(R.id.snackbar_anchor);
    }

    /**
     * Load shuttles from Firestore
     */
    private void loadShuttles() {
        db.collection("shuttles")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> shuttleNames = new ArrayList<>();
                    shuttleMap.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Shuttle shuttle = doc.toObject(Shuttle.class);
                        if (shuttle != null) {
                            shuttleList.add(shuttle);
                            shuttleNames.add(shuttle.getShuttleName());
                            shuttleMap.put(shuttle.getShuttleName(), shuttle.getShuttleId());
                        }
                    }

                    if (getContext() != null) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                getContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                shuttleNames
                        );
                        actvShuttleSelector.setAdapter(adapter);

                        actvShuttleSelector.setOnItemClickListener((parent, view, position, id) -> {
                            String shuttleName = (String) parent.getItemAtPosition(position);
                            selectedShuttleId = shuttleMap.get(shuttleName);
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading shuttles", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Setup issue types dropdown
     */
    private void setupIssueTypes() {
        List<String> issueTypes = new ArrayList<>();
        for (Report.IssueType type : Report.IssueType.values()) {
            issueTypes.add(type.getDisplayName());
        }

        if (getContext() != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    getContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    issueTypes
            );
            actvIssueType.setAdapter(adapter);

            actvIssueType.setOnItemClickListener((parent, view, position, id) -> {
                selectedIssueType = Report.IssueType.values()[position];
                // Uncheck all chips when dropdown is used
                chipGroupIssues.clearCheck();
            });
        }
    }

    /**
     * Setup chip group for quick issue selection
     */
    private void setupChipGroup() {
        chipGroupIssues.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);

            if (checkedId == R.id.chip_delay) {
                selectedIssueType = Report.IssueType.DELAY;
                actvIssueType.setText(Report.IssueType.DELAY.getDisplayName(), false);
            } else if (checkedId == R.id.chip_breakdown) {
                selectedIssueType = Report.IssueType.BREAKDOWN;
                actvIssueType.setText(Report.IssueType.BREAKDOWN.getDisplayName(), false);
            } else if (checkedId == R.id.chip_overcrowding) {
                selectedIssueType = Report.IssueType.OVERCROWDING;
                actvIssueType.setText(Report.IssueType.OVERCROWDING.getDisplayName(), false);
            } else if (checkedId == R.id.chip_missed_stop) {
                selectedIssueType = Report.IssueType.MISSED_STOP;
                actvIssueType.setText(Report.IssueType.MISSED_STOP.getDisplayName(), false);
            } else if (checkedId == R.id.chip_other) {
                selectedIssueType = Report.IssueType.OTHER;
                actvIssueType.setText(Report.IssueType.OTHER.getDisplayName(), false);
            }
        });
    }

    /**
     * Setup submit button
     */
    private void setupSubmitButton() {
        btnSubmitReport.setOnClickListener(v -> submitReport());
    }

    /**
     * Validate and submit report
     */
    private void submitReport() {
        // Validate shuttle selection
        if (TextUtils.isEmpty(selectedShuttleId)) {
            actvShuttleSelector.setError("Please select a shuttle");
            actvShuttleSelector.requestFocus();
            return;
        }

        // Validate issue type
        if (selectedIssueType == null) {
            actvIssueType.setError("Please select an issue type");
            actvIssueType.requestFocus();
            return;
        }

        // Validate description
        String description = etDescription.getText() != null ?
                etDescription.getText().toString().trim() : "";

        if (TextUtils.isEmpty(description)) {
            etDescription.setError("Please describe the issue");
            etDescription.requestFocus();
            return;
        }

        if (description.length() < 10) {
            etDescription.setError("Please provide more details (at least 10 characters)");
            etDescription.requestFocus();
            return;
        }

        // Disable button while submitting
        btnSubmitReport.setEnabled(false);

        // Create report
        String userId = currentUser != null ? currentUser.getUid() : "anonymous";
        Report report = new Report(userId, selectedShuttleId, selectedIssueType, description);

        // Get shuttle name
        String shuttleName = getShuttleName(selectedShuttleId);
        report.setShuttleName(shuttleName);

        // Submit to Firestore
        db.collection("reports")
                .add(report)
                .addOnSuccessListener(documentReference -> {
                    // Update report ID
                    String reportId = documentReference.getId();
                    documentReference.update("reportId", reportId);

                    // Show success message
                    showSuccessMessage();

                    // Clear form
                    clearForm();

                    btnSubmitReport.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            getString(R.string.report_error),
                            Toast.LENGTH_LONG).show();
                    btnSubmitReport.setEnabled(true);
                });
    }

    /**
     * Get shuttle name from ID
     */
    private String getShuttleName(String shuttleId) {
        for (Shuttle shuttle : shuttleList) {
            if (shuttle.getShuttleId().equals(shuttleId)) {
                return shuttle.getShuttleName();
            }
        }
        return "Unknown Shuttle";
    }

    /**
     * Show success message
     */
    private void showSuccessMessage() {
        if (snackbarAnchor != null) {
            Snackbar.make(snackbarAnchor,
                            getString(R.string.report_success),
                            Snackbar.LENGTH_LONG)
                    .setAction("OK", v -> {})
                    .show();
        }
    }

    /**
     * Clear form after submission
     */
    private void clearForm() {
        actvShuttleSelector.setText("");
        actvIssueType.setText("");
        if (etDescription != null) {
            etDescription.setText("");
        }
        chipGroupIssues.clearCheck();
        selectedShuttleId = null;
        selectedIssueType = null;
    }
}