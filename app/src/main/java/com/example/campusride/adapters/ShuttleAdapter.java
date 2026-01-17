package com.example.campusride.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.example.campusride.R;
import com.example.campusride.models.Shuttle;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying shuttles in RecyclerView
 */
public class ShuttleAdapter extends RecyclerView.Adapter<ShuttleAdapter.ShuttleViewHolder> {

    private final List<Shuttle> shuttles;
    private final OnShuttleClickListener listener;

    public interface OnShuttleClickListener {
        void onShuttleClick(Shuttle shuttle);
    }

    public ShuttleAdapter(List<Shuttle> shuttles, OnShuttleClickListener listener) {
        this.shuttles = shuttles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShuttleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shuttle_card, parent, false);
        return new ShuttleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShuttleViewHolder holder, int position) {
        holder.bind(shuttles.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return shuttles.size();
    }

    static class ShuttleViewHolder extends RecyclerView.ViewHolder {
        private final View viewStatusBorder;
        private final TextView tvShuttleName;
        private final TextView tvRouteName;
        private final TextView tvStatusBadge;
        private final TextView tvEta;
        private final TextView tvDistance;
        private final TextView tvCapacity;
        private final MaterialButton btnNotify;

        ShuttleViewHolder(View itemView) {
            super(itemView);
            viewStatusBorder = itemView.findViewById(R.id.view_status_border);
            tvShuttleName = itemView.findViewById(R.id.tv_shuttle_name);
            tvRouteName = itemView.findViewById(R.id.tv_route_name);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            tvEta = itemView.findViewById(R.id.tv_eta);
            tvDistance = itemView.findViewById(R.id.tv_distance);
            tvCapacity = itemView.findViewById(R.id.tv_capacity);
            btnNotify = itemView.findViewById(R.id.btn_notify);
        }

        void bind(Shuttle shuttle, OnShuttleClickListener listener) {
            // Shuttle name and route
            tvShuttleName.setText(shuttle.getShuttleName());
            tvRouteName.setText(shuttle.getCurrentRoute() != null ?
                    shuttle.getCurrentRoute() : "No active route");

            // Status badge
            String statusText = getStatusText(shuttle.getStatus());
            tvStatusBadge.setText(statusText);

            // Status border and badge color
            int statusColor = getStatusColor(shuttle.getStatus(), itemView);
            viewStatusBorder.setBackgroundColor(statusColor);
            tvStatusBadge.setBackgroundResource(getStatusBackground(shuttle.getStatus()));

            // ETA
            if (shuttle.getEtaMinutes() > 0) {
                if (shuttle.getEtaMinutes() < 1) {
                    tvEta.setText("Arriving now");
                } else {
                    tvEta.setText(String.format(Locale.getDefault(), "%d min", shuttle.getEtaMinutes()));
                }
            } else {
                tvEta.setText("--");
            }

            // Distance
            if (shuttle.getDistanceToStop() > 0) {
                tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", shuttle.getDistanceToStop()));
            } else {
                tvDistance.setText("--");
            }

            // Capacity
            tvCapacity.setText(shuttle.getCapacityString());

            // Click listeners
            itemView.setOnClickListener(v -> listener.onShuttleClick(shuttle));

            btnNotify.setOnClickListener(v -> {
                // TODO: Setup notification for this shuttle
                android.widget.Toast.makeText(itemView.getContext(),
                        "Notification enabled for " + shuttle.getShuttleName(),
                        android.widget.Toast.LENGTH_SHORT).show();
            });
        }

        private String getStatusText(Shuttle.ShuttleStatus status) {
            if (status == null) return "Offline";
            switch (status) {
                case ACTIVE: return "Active";
                case ON_BREAK: return "On Break";
                case BREAKDOWN: return "Breakdown";
                default: return "Offline";
            }
        }

        private int getStatusColor(Shuttle.ShuttleStatus status, View view) {
            if (status == null) {
                return androidx.core.content.ContextCompat.getColor(view.getContext(), R.color.danger);
            }
            switch (status) {
                case ACTIVE:
                    return androidx.core.content.ContextCompat.getColor(view.getContext(), R.color.success);
                case ON_BREAK:
                    return androidx.core.content.ContextCompat.getColor(view.getContext(), R.color.warning);
                case BREAKDOWN:
                    return androidx.core.content.ContextCompat.getColor(view.getContext(), R.color.danger);
                default:
                    return androidx.core.content.ContextCompat.getColor(view.getContext(), R.color.text_hint);
            }
        }

        private int getStatusBackground(Shuttle.ShuttleStatus status) {
            if (status == null) return R.drawable.badge_danger_background;
            switch (status) {
                case ACTIVE: return R.drawable.badge_active_background;
                case ON_BREAK: return R.drawable.badge_warning_background;
                case BREAKDOWN: return R.drawable.badge_danger_background;
                default: return R.drawable.badge_danger_background;
            }
        }
    }
}