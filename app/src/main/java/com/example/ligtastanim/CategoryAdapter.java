package com.example.ligtastanim;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private Context context;

    public CategoryAdapter(List<Category> categories, Context context) {
        this.categories = categories;
        this.context = context;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, final int position) {
        Category category = categories.get(position);
        holder.categoryName.setText(category.getName());
        holder.categoryIcon.setImageResource(category.getIconResId());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String categoryName = categories.get(position).getName();
                switch (categoryName) {
                    case "Crops":

                        Intent cropsIntent = new Intent(context, CropsList.class);
                        context.startActivity(cropsIntent);
                        break;
                    case "Pests":

                        Intent pestsIntent = new Intent(context, PestList.class);
                        context.startActivity(pestsIntent);
                        break;
                    case "Fertilizers":

                        Intent fertilizersIntent = new Intent(context, FertilizerList.class);
                        context.startActivity(fertilizersIntent);
                        break;
                    case "Insecticide":

                        Intent insecticidesIntent = new Intent(context, InsecticideList.class);
                        context.startActivity(insecticidesIntent);
                        break;
                    case "Disease":

                        Intent diseaseIntent = new Intent(context, DiseaseList.class);
                        context.startActivity(diseaseIntent);
                        break;
                    default:

                        Toast.makeText(context, "Clicked: " + categoryName, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryIcon;
        TextView categoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIcon = itemView.findViewById(R.id.category_icon);
            categoryName = itemView.findViewById(R.id.category_name);
        }
    }
}
