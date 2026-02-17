package com.example.chefmasterjrma;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters; // Add this import

@Database(entities = {Recipe.class}, version = 3, exportSchema = false) // Bump version to 3
@TypeConverters({Converters.class}) // NEW: Tells Room how to handle Lists
public abstract class AppDatabase extends RoomDatabase {
    // ... existing code ...

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