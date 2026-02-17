package com.example.chefmasterjrma;

import android.content.Intent;
import android.graphics.Color; // Import for colors
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipeList;
    private boolean isTrashMode;

    public RecipeAdapter(List<Recipe> recipeList, boolean isTrashMode) {
        this.recipeList = recipeList;
        this.isTrashMode = isTrashMode;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);

        holder.title.setText((position + 1) + ". " + recipe.getTitle());
        holder.ingredients.setText(recipe.getIngredients());
        holder.textLastEdited.setText("Edited: " + recipe.getFormattedDate());

        // --- ICON LOGIC: Fix for "Looks the same" issue ---
        if (isTrashMode) {
            holder.imgAction.setImageResource(android.R.drawable.ic_menu_rotate);
            holder.imgAction.setColorFilter(Color.BLACK); // Restore icon is Black
        } else {
            if (recipe.isFavorite()) {
                holder.imgAction.setImageResource(android.R.drawable.btn_star_big_on);
                holder.imgAction.setColorFilter(Color.parseColor("#FFD700")); // Gold for Favorites
            } else {
                holder.imgAction.setImageResource(android.R.drawable.btn_star_big_off);
                holder.imgAction.setColorFilter(Color.LTGRAY); // Light Grey for non-favorites
            }
        }

        // --- CLICK: Open Details ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), RecipeDetailActivity.class);
            intent.putExtra("RECIPE_ID", recipe.getId());
            intent.putExtra("RECIPE_TITLE", recipe.getTitle());
            intent.putExtra("RECIPE_CONTENT", recipe.getIngredients());
            v.getContext().startActivity(intent);
        });

        // --- CLICK: Toggle Favorite / Restore ---
        holder.imgAction.setOnClickListener(v -> {
            if (isTrashMode) {
                // Restore Logic
                recipe.setInTrash(false);
                recipe.setLastEdited(System.currentTimeMillis());
                AppDatabase.getDatabase(v.getContext()).recipeDao().update(recipe);
                recipeList.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(v.getContext(), "Restored to Vault", Toast.LENGTH_SHORT).show();
            } else {
                // Favorite Toggle Logic
                boolean newStatus = !recipe.isFavorite();
                recipe.setFavorite(newStatus);
                recipe.setLastEdited(System.currentTimeMillis());
                AppDatabase.getDatabase(v.getContext()).recipeDao().update(recipe);

                // Update the icon immediately without reloading the whole list
                notifyItemChanged(position);
            }
        });

        // --- LONG CLICK: Delete ---
        holder.itemView.setOnLongClickListener(v -> {
            if (recipe.isFavorite()) {
                Toast.makeText(v.getContext(), "Un-star this recipe before deleting!", Toast.LENGTH_SHORT).show();
                return true;
            }
            if (!isTrashMode) {
                recipe.setInTrash(true);
                recipe.setLastEdited(System.currentTimeMillis());
                AppDatabase.getDatabase(v.getContext()).recipeDao().update(recipe);
                recipeList.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(v.getContext(), "Moved to Trash", Toast.LENGTH_SHORT).show();
            } else {
                AppDatabase.getDatabase(v.getContext()).recipeDao().deleteForever(recipe);
                recipeList.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(v.getContext(), "Deleted Permanently", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public void updateList(List<Recipe> newList) {
        this.recipeList = newList;
        notifyDataSetChanged();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView title, ingredients, textLastEdited;
        ImageView imgAction;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textRecipeTitle);
            ingredients = itemView.findViewById(R.id.textRecipeIngredients);
            textLastEdited = itemView.findViewById(R.id.textLastEdited); // Ensure this ID exists in XML
            imgAction = itemView.findViewById(R.id.imgFavorite);
        }
    }
}