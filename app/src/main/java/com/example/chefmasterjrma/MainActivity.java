package com.example.chefmasterjrma;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btnToggleTheme, btnOpenTrash, btnImportZip;
    private FloatingActionButton fabAddRecipe;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private Spinner spinnerSort;
    private CheckBox checkFavoritesOnly;
    private RecipeAdapter adapter;
    private AppDatabase db;

    private ActivityResultLauncher<String[]> zipPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize Views from activity_main.xml
        btnToggleTheme = findViewById(R.id.btnToggleTheme);
        btnOpenTrash = findViewById(R.id.btnOpenTrash);
        btnImportZip = findViewById(R.id.btnImportZip);
        fabAddRecipe = findViewById(R.id.fabAddRecipe);
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);
        spinnerSort = findViewById(R.id.spinnerSort);
        checkFavoritesOnly = findViewById(R.id.checkFavoritesOnly);

        db = AppDatabase.getDatabase(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 2. Set the correct Theme button text immediately when the app opens
        updateThemeButton();

        // 3. Handle Theme Toggle Button Click
        btnToggleTheme.setOnClickListener(v -> toggleTheme());

        // 4. Handle FAB Click to Add a New Recipe
        fabAddRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RecipeDetailActivity.class);
            startActivity(intent);
        });

        // 5. Search Logic
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                refreshData();
                return true;
            }
        });

        // 6. Filter and Sort Logic
        checkFavoritesOnly.setOnCheckedChangeListener((buttonView, isChecked) -> refreshData());
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshData();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 7. Trash and Import
        btnOpenTrash.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TrashActivity.class);
            startActivity(intent);
        });

        zipPickerLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                ImportManager.importRecipeFromZip(this, uri, new ImportManager.ImportCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Recipe Imported!", Toast.LENGTH_SHORT).show();
                            refreshData();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show());
                    }
                });
            }
        });

        btnImportZip.setOnClickListener(v -> zipPickerLauncher.launch(new String[]{"application/zip"}));

        refreshData();
    }

    private void refreshData() {
        String query = searchView.getQuery().toString();
        int sortPosition = spinnerSort.getSelectedItemPosition(); // 0 = Newest, 1 = A-Z
        List<Recipe> result;

        if (checkFavoritesOnly.isChecked()) {
            result = db.recipeDao().getFavoriteRecipes();
        } else if (!query.isEmpty()) {
            result = db.recipeDao().searchRecipes("%" + query + "%");
        } else {
            if (sortPosition == 1) {
                result = db.recipeDao().getAllAlphabetical();
            } else {
                result = db.recipeDao().getAllActiveRecipes();
            }
        }

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
        refreshData();
    }

    // --- NEW METHODS FOR THEME HANDLING ---

    /**
     * Checks the current system theme and updates the button's text.
     */
    private void updateThemeButton() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // App is currently in Dark Mode -> Show "Light" to indicate switching to light
            btnToggleTheme.setText("Light");
        } else {
            // App is currently in Light Mode -> Show "Dark" to indicate switching to dark
            btnToggleTheme.setText("Dark");
        }
    }

    /**
     * Toggles the app's theme between Light and Dark mode.
     */
    private void toggleTheme() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int newMode;

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            newMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            newMode = AppCompatDelegate.MODE_NIGHT_YES;
        }

        // Save preference
        getSharedPreferences("THEME_PREFS", MODE_PRIVATE)
                .edit()
                .putInt("NIGHT_MODE", newMode)
                .apply();

        AppCompatDelegate.setDefaultNightMode(newMode);
    }
}
