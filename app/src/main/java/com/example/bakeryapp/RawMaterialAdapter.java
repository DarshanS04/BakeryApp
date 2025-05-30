package com.example.bakeryapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RawMaterialAdapter extends RecyclerView.Adapter<RawMaterialAdapter.RawMaterialViewHolder> {

    private List<RawMaterial> rawMaterials = new ArrayList<>();

    public void setRawMaterials(List<RawMaterial> rawMaterials) {
        this.rawMaterials = rawMaterials;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RawMaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_raw_material, parent, false);
        return new RawMaterialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RawMaterialViewHolder holder, int position) {
        RawMaterial rawMaterial = rawMaterials.get(position);
        holder.nameTextView.setText(rawMaterial.getName());
        holder.priceTextView.setText(rawMaterial.getPrice());
    }

    @Override
    public int getItemCount() {
        return rawMaterials.size();
    }

    static class RawMaterialViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView priceTextView;

        public RawMaterialViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.material_name);
            priceTextView = itemView.findViewById(R.id.material_price);
        }
    }
}