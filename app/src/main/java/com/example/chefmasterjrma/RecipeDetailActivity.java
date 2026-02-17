package com.example.chefmasterjrma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RecipeDetailActivity extends AppCompatActivity {

    private int recipeId;
    private EditText titleView;
    private EditText contentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // 1. Initialize Views
        titleView = findViewById(R.id.editDetailTitle);
        contentView = findViewById(R.id.editDetailContent);
        Button updateBtn = findViewById(R.id.btnUpdateRecipe);
        Button shareBtn = findViewById(R.id.btnShareRecipe); // Make sure ID matches XML

        // 2. Get Data passed from the List
        recipeId = getIntent().getIntExtra("RECIPE_ID", -1);
        String title = getIntent().getStringExtra("RECIPE_TITLE");
        String content = getIntent().getStringExtra("RECIPE_CONTENT");

        // 3. Populate the fields
        titleView.setText(title);
        contentView.setText(content);

        // 4. SAVE / UPDATE Logic
        updateBtn.setOnClickListener(v -> {
            String newTitle = titleView.getText().toString().trim();
            String newContent = contentView.getText().toString().trim();

            if (newTitle.isEmpty() || newContent.isEmpty()) {
                Toast.makeText(this, "Title and Ingredients cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Fetch existing recipe to keep "Favorite" status and ID intact
            Recipe existingRecipe = AppDatabase.getDatabase(this).recipeDao().getRecipeById(recipeId);

            if (existingRecipe != null) {
                existingRecipe.setTitle(newTitle);
                existingRecipe.setIngredients(newContent);
                existingRecipe.setLastEdited(System.currentTimeMillis()); // Update timestamp

                AppDatabase.getDatabase(this).recipeDao().update(existingRecipe);

                Toast.makeText(this, "Recipe Updated!", Toast.LENGTH_SHORT).show();
                finish(); // Go back to the list
            } else {
                Toast.makeText(this, "Error: Could not find recipe to update.", Toast.LENGTH_SHORT).show();
            }
        });

        // 5. SHARE Logic
        shareBtn.setOnClickListener(v -> {
            // Get the text currently in the input fields
            String currentTitle = titleView.getText().toString();
            String currentContent = contentView.getText().toString();

            if (currentTitle.isEmpty()) {
                Toast.makeText(this, "Nothing to share!", Toast.LENGTH_SHORT).show();
                return;
            }

            String shareBody = "Check out this recipe I found in Chef Vault!\n\n" +
                    "🍳 " + currentTitle + "\n\n" +
                    "📝 Ingredients:\n" + currentContent;

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Recipe: " + currentTitle);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

            try {
                startActivity(Intent.createChooser(shareIntent, "Share Recipe via"));
            } catch (Exception e) {
                Toast.makeText(this, "No app found to share.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}