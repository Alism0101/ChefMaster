package com.example.chefmasterjrma;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TrashActivity extends AppCompatActivity {

    private RecipeAdapter adapter;
    private List<Recipe> trashedRecipes = new ArrayList<>(); // Initialize to empty list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_trash);

            RecyclerView recyclerView = findViewById(R.id.recyclerViewTrash);

            if (recyclerView == null) {
                Log.e("CHEF_DEBUG", "RecyclerView is null! Check XML ID.");
                Toast.makeText(this, "Internal Layout Error", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Initialize adapter first with an empty list to prevent NullPointer
            adapter = new RecipeAdapter(trashedRecipes, true);
            recyclerView.setAdapter(adapter);

            // Now load the actual data
            loadTrashData();

        } catch (Exception e) {
            Log.e("CHEF_DEBUG", "Crash in onCreate: " + e.getMessage());
            Toast.makeText(this, "Activity failed to start", Toast.LENGTH_LONG).show();
        }
    }

    private void loadTrashData() {
        try {
            List<Recipe> data = AppDatabase.getDatabase(this).recipeDao().getTrashRecipes();
            if (data != null) {
                trashedRecipes.clear();
                trashedRecipes.addAll(data);
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Log.e("CHEF_DEBUG", "Database Error: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTrashData(); // Refresh when returning to screen
    }
}