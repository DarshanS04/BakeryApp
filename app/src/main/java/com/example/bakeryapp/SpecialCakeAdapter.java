package com.example.bakeryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SpecialCakeAdapter extends RecyclerView.Adapter<SpecialCakeAdapter.SpecialCakeViewHolder> {

    private Context context;
    private List<SpecialCake> specialCakes;

    public SpecialCakeAdapter(Context context, List<SpecialCake> specialCakes) {
        this.context = context;
        this.specialCakes = specialCakes;
    }

    @NonNull
    @Override
    public SpecialCakeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_special_cake, parent, false);
        return new SpecialCakeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpecialCakeViewHolder holder, int position) {
        SpecialCake cake = specialCakes.get(position);
        holder.cakeText.setText(cake.getCode() + " - ₹" + cake.getPrice());
        try {
            holder.cakeImage.setImageResource(cake.getImageResId());
        } catch (Exception e) {
            // Handle missing drawable gracefully
            holder.cakeImage.setImageResource(R.drawable.cake); // Fallback to default cake image
        }
    }

    @Override
    public int getItemCount() {
        return specialCakes.size();
    }

    static class SpecialCakeViewHolder extends RecyclerView.ViewHolder {
        ImageView cakeImage;
        TextView cakeText;

        SpecialCakeViewHolder(@NonNull View itemView) {
            super(itemView);
            cakeImage = itemView.findViewById(R.id.cakeImage);
            cakeText = itemView.findViewById(R.id.cakeText);
        }
    }
}