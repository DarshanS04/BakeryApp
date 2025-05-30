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
import java.util.Map;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private Context context;
    private List<Product> products;
    private Map<String, Integer> productPrices;
    private Map<String, Integer> productImageResIds;
    private boolean showSales = false;

    public MenuAdapter(Context context, List<Product> products, Map<String, Integer> productPrices, Map<String, Integer> productImageResIds) {
        this.context = context;
        this.products = products;
        this.productPrices = productPrices;
        this.productImageResIds = productImageResIds;
    }

    public MenuAdapter(Context context, List<Product> products, Map<String, Integer> productPrices, Map<String, Integer> productImageResIds, boolean showSales) {
        this.context = context;
        this.products = products;
        this.productPrices = productPrices;
        this.productImageResIds = productImageResIds;
        this.showSales = showSales;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        Product product = products.get(position);
        String productName = product.getName();

        // Set product details
        holder.productName.setText(productName);
        holder.productPrice.setText("₹" + productPrices.getOrDefault(productName, 0));
        holder.productQuantity.setText(showSales ? "Sold: " + product.getQuantity() : "Available: " + product.getQuantity());

        // Set image
        Integer imageResId = productImageResIds.get(productName);
        if (imageResId != null) {
            holder.productImage.setImageResource(imageResId);
            holder.productImage.setVisibility(View.VISIBLE);
        } else {
            holder.productImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productPrice;
        TextView productQuantity;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            productQuantity = itemView.findViewById(R.id.product_quantity);
        }
    }
}