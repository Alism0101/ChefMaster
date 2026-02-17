package com.example.chefmasterjrma;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecipeListActivity extends AppCompatActivity {
    private RecipeAdapter adapter;
    private CheckBox favCheckBox;
    private SearchView searchView;
    private Spinner sortSpinner;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

// Assign the casted view to your variable
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewRecipes);
        // Initialize UI components
        favCheckBox = findViewById(R.id.checkFavoritesOnly);
        searchView = findViewById(R.id.searchRecipes);
        sortSpinner = findViewById(R.id.spinnerSort);

        // FIX: Casting to RecyclerView to resolve 'cannot find symbol method setAdapter'
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewRecipes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Sorting Logic
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshData();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Search Logic
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                refreshData();
                return true;
            }
        });

        favCheckBox.setOnCheckedChangeListener((v, isChecked) -> refreshData());
    }

    /**
     * Refreshes the data by querying the DAO based on search, favorites, and sort order.
     */
    private void refreshData() {
        String query = searchView.getQuery().toString();
        int sortPosition = sortSpinner.getSelectedItemPosition(); // 0 = Newest, 1 = A-Z
        List<Recipe> result;

        if (favCheckBox.isChecked()) {
            result = AppDatabase.getDatabase(this).recipeDao().getFavoriteRecipes();
        } else if (!query.isEmpty()) {
            // FIX: Access search through recipeDao() instead of AppDatabase directly
            result = AppDatabase.getDatabase(this).recipeDao().searchRecipes("%" + query + "%");
        } else {
            // HANDLE SORTING: Fetch based on Spinner selection using DAO methods
            if (sortPosition == 1) {
                result = AppDatabase.getDatabase(this).recipeDao().getAllAlphabetical();
            } else {
                result = AppDatabase.getDatabase(this).recipeDao().getAllActiveRecipes();
            }
        }

        // Initialize or Update Adapter
        if (adapter == null) {
            adapter = new RecipeAdapter(result, false);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateList(result);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Crucial: Forces the list to refresh when returning from the editor
        refreshData();
    }
}