package com.example.chefmasterjrma;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "recipe_table")
public class Recipe {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String ingredients;
    private String instructions;
    private boolean isFavorite;
    private boolean isInTrash;
    private long lastEdited; // Added timestamp field

    public Recipe() {
        this.lastEdited = System.currentTimeMillis();
    }

    // Helper for displaying the date in the list
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, HH:mm", Locale.getDefault());
        return sdf.format(new Date(lastEdited));
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public boolean isInTrash() { return isInTrash; }
    public void setInTrash(boolean inTrash) { isInTrash = inTrash; }
    public long getLastEdited() { return lastEdited; }
    public void setLastEdited(long lastEdited) { this.lastEdited = lastEdited; }
}