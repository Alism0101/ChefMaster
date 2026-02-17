package com.example.chefmasterjrma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText titleInput = findViewById(R.id.editRecipeTitle);
        EditText contentInput = findViewById(R.id.editRecipeContent);
        Button saveBtn = findViewById(R.id.btnSave);
        Button viewVaultBtn = findViewById(R.id.btnViewVault);
        Button viewTrashBtn = findViewById(R.id.btnViewTrash);

        saveBtn.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String ingredients = contentInput.getText().toString().trim();

            if (title.isEmpty() || ingredients.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Using the setter approach to avoid constructor confusion
            Recipe newRecipe = new Recipe();
            newRecipe.setTitle(title);
            newRecipe.setIngredients(ingredients);
            newRecipe.setInstructions("Standard Prep");
            newRecipe.setFavorite(false);
            newRecipe.setInTrash(false);

            try {
                AppDatabase.getDatabase(this).recipeDao().insert(newRecipe);
                Toast.makeText(this, "Recipe Saved!", Toast.LENGTH_SHORT).show();
                titleInput.setText("");
                contentInput.setText("");
            } catch (Exception e) {
                // Catching database errors to prevent the app from closing
                Toast.makeText(this, "Database Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        viewVaultBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RecipeListActivity.class);
            startActivity(intent);
        });

        viewTrashBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TrashActivity.class);
            startActivity(intent);
        });
    }
}