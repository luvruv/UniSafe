package com.example.unisafe.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unisafe.R;
import com.example.unisafe.models.Complaint;
import com.example.unisafe.utils.AppConstants;

import java.util.List;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Complaint complaint);
    }

    private final Context              context;
    private final List<Complaint>      complaints;
    private final OnItemClickListener  listener;

    public ComplaintAdapter(Context context, List<Complaint> complaints,
                            OnItemClickListener listener) {
        this.context    = context;
        this.complaints = complaints;
        this.listener   = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_complaint, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Complaint c = complaints.get(position);

        // ── Subject ─────────────────────────────────────────────────────────
        holder.tvSubject.setText(
                TextUtils.isEmpty(c.getSubject()) ? "—" : c.getSubject());

        // ── Category emoji ────────────────────────────────────────────────
        holder.tvCategory.setText(getCategoryEmoji(c.getCategory()));
        // ✅ Dynamic background colour per category (no more hardcoded electricity-yellow)
        holder.tvCategory.setBackgroundColor(
                ContextCompat.getColor(context, getCategoryBg(c.getCategory())));

        // ── Timestamp ─────────────────────────────────────────────────────
        holder.tvTime.setText(
                TextUtils.isEmpty(c.getTimestamp()) ? "—" : c.getTimestamp());

        // ── Complaint ID — null-safe + length-safe ─────────────────────────
        String id = c.getId();
        if (!TextUtils.isEmpty(id)) {
            holder.tvId.setText("#" + id.substring(0, Math.min(6, id.length())).toUpperCase());
        } else {
            holder.tvId.setText("#------");
        }

        // ── Status badge — ContextCompat (no deprecated calls) ────────────
        String status = c.getStatus();
        holder.tvStatus.setText(getStatusLabel(status));
        holder.tvStatus.setBackground(
                ContextCompat.getDrawable(context, getStatusBg(status)));
        holder.tvStatus.setTextColor(
                ContextCompat.getColor(context, getStatusColor(status)));

        // ── Click ─────────────────────────────────────────────────────────
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(c);
        });
    }

    @Override
    public int getItemCount() {
        return complaints == null ? 0 : complaints.size();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private int getCategoryBg(String category) {
        if (TextUtils.isEmpty(category)) return R.color.light_blue;
        switch (category.toLowerCase()) {
            case "electricity":        return R.color.electricity_bg;
            case "wifi":
            case "wifi & internet":    return R.color.wifi_bg;
            case "water":
            case "water supply":       return R.color.water_bg;
            case "cleaning":
            case "cleaning & hygiene": return R.color.cleaning_bg;
            case "maintenance":        return R.color.maintenance_bg;
            default:                   return R.color.light_blue;
        }
    }

    private String getCategoryEmoji(String category) {
        if (TextUtils.isEmpty(category)) return "📋";
        switch (category.toLowerCase()) {
            case "electricity":        return "⚡";
            case "wifi":
            case "wifi & internet":    return "📶";
            case "water":
            case "water supply":       return "💧";
            case "cleaning":
            case "cleaning & hygiene": return "🧹";
            case "maintenance":        return "🔧";
            default:                   return "📋";
        }
    }

    private String getStatusLabel(String status) {
        if (TextUtils.isEmpty(status)) return "Pending";
        switch (status) {
            case AppConstants.STATUS_IN_PROGRESS: return "In Progress";
            case AppConstants.STATUS_COMPLETED:   return "Completed";
            case AppConstants.STATUS_REJECTED:    return "Rejected";
            default:                              return "Pending";
        }
    }

    private int getStatusBg(String status) {
        if (TextUtils.isEmpty(status)) return R.drawable.bg_status_pending;
        switch (status) {
            case AppConstants.STATUS_IN_PROGRESS: return R.drawable.bg_status_progress;
            case AppConstants.STATUS_COMPLETED:   return R.drawable.bg_status_completed;
            default:                              return R.drawable.bg_status_pending;
        }
    }

    private int getStatusColor(String status) {
        if (TextUtils.isEmpty(status)) return R.color.status_pending;
        switch (status) {
            case AppConstants.STATUS_IN_PROGRESS: return R.color.status_progress;
            case AppConstants.STATUS_COMPLETED:   return R.color.status_completed;
            case AppConstants.STATUS_REJECTED:    return R.color.status_rejected;
            default:                              return R.color.status_pending;
        }
    }

    // ─── ViewHolder ───────────────────────────────────────────────────────────

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvCategory, tvSubject, tvTime, tvId, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView   = itemView.findViewById(R.id.card_complaint);
            tvCategory = itemView.findViewById(R.id.tv_category_icon);
            tvSubject  = itemView.findViewById(R.id.tv_subject);
            tvTime     = itemView.findViewById(R.id.tv_time);
            tvId       = itemView.findViewById(R.id.tv_complaint_id);
            tvStatus   = itemView.findViewById(R.id.tv_status);
        }
    }
}