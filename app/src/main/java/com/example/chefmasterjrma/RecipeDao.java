package com.example.chefmasterjrma;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface RecipeDao {

    @Query("SELECT * FROM recipe_table WHERE isInTrash = 0 ORDER BY lastEdited DESC")
    List<Recipe> getAllActiveRecipes();

    @Query("SELECT * FROM recipe_table WHERE isInTrash = 0 ORDER BY title ASC")
    List<Recipe> getAllAlphabetical();

    @Query("SELECT * FROM recipe_table WHERE isFavorite = 1 AND isInTrash = 0 ORDER BY lastEdited DESC")
    List<Recipe> getFavoriteRecipes();

    // Fixes the error in TrashActivity.java
    @Query("SELECT * FROM recipe_table WHERE isInTrash = 1 ORDER BY lastEdited DESC")
    List<Recipe> getTrashRecipes();

    // Fixes the error in RecipeDetailActivity.java
    @Query("SELECT * FROM recipe_table WHERE id = :id LIMIT 1")
    Recipe getRecipeById(int id);

    // Fixes the search error in RecipeListActivity.java
    @Query("SELECT * FROM recipe_table WHERE (title LIKE :query OR ingredients LIKE :query) AND isInTrash = 0")
    List<Recipe> searchRecipes(String query);

    @Insert
    void insert(Recipe recipe);

    @Update
    void update(Recipe recipe);

    @Delete
    void deleteForever(Recipe recipe);

    // ---> THIS IS THE FIX! We added the @Query annotation <---
    @Query("SELECT * FROM recipe_table WHERE isInTrash = 0")
    List<Recipe> getAllRecipes();
}