package com.example.unisafe.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.unisafe.R;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    private Context context;
    private List<String[]> categories; // [name, emoji, description]
    private OnCategoryClickListener listener;

    public CategoryAdapter(Context context, List<String[]> categories, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] cat = categories.get(position);
        holder.tvName.setText(cat[0]);
        holder.tvEmoji.setText(cat[1]);
        holder.tvDescription.setText(cat[2]);
        holder.cardView.setOnClickListener(v -> listener.onCategoryClick(cat[0]));
    }

    @Override
    public int getItemCount() { return categories.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvEmoji, tvName, tvDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_category);
            tvEmoji = itemView.findViewById(R.id.tv_emoji);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDescription = itemView.findViewById(R.id.tv_description);
        }
    }
}