package com.example.chefmasterjrma;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// 1. Increment version from 1 to 2 (or higher if you change it again)
@Database(entities = {Recipe.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract RecipeDao recipeDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "recipe_database")
                            // 2. Add this line: Allows Room to recreate the DB if tables change
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries() // Optional: Only for simple testing
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}