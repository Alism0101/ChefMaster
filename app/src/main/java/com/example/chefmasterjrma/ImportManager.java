package com.example.chefmasterjrma;

import android.content.Context;
import android.net.Uri;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ImportManager {

    public interface ImportCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public static void importRecipeFromZip(Context context, Uri zipUri, ImportCallback callback) {
        new Thread(() -> {
            try {
                InputStream is = context.getContentResolver().openInputStream(zipUri);
                ZipInputStream zis = new ZipInputStream(is);
                ZipEntry entry;

                Recipe newRecipe = new Recipe();
                List<String> mediaPaths = new ArrayList<>();
                String recipeText = "";

                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().equals("recipe_text.txt")) {
                        recipeText = readTextFromStream(zis);
                    } else if (entry.getName().startsWith("media_")) {
                        String localPath = saveMediaToInternal(context, zis, entry.getName());
                        if (localPath != null) mediaPaths.add(localPath);
                    }
                    zis.closeEntry();
                }
                zis.close();

                if (recipeText.isEmpty()) {
                    callback.onFailure("Invalid ZIP: recipe_text.txt not found.");
                    return;
                }

                parseRecipeText(newRecipe, recipeText);
                newRecipe.setMediaUrls(mediaPaths);
                newRecipe.setLastEdited(System.currentTimeMillis());

                AppDatabase.getDatabase(context).recipeDao().insert(newRecipe);
                callback.onSuccess();

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFailure("Import failed: " + e.getMessage());
            }
        }).start();
    }

    private static String readTextFromStream(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    private static String saveMediaToInternal(Context context, InputStream is, String fileName) {
        try {
            String extension = fileName.endsWith(".mp4") ? ".mp4" : ".jpg";
            File file = new File(context.getFilesDir(), "media_" + System.currentTimeMillis() + "_" + fileName);
            FileOutputStream fos = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            fos.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    private static void parseRecipeText(Recipe recipe, String text) {
        // Simple parsing based on ZipManager format
        // TITLE: ... \n\nINGREDIENTS:\n ...
        int titleIndex = text.indexOf("TITLE: ");
        int ingredientsIndex = text.indexOf("\n\nINGREDIENTS:\n");

        if (titleIndex != -1 && ingredientsIndex != -1) {
            String title = text.substring(titleIndex + 7, ingredientsIndex).trim();
            String ingredients = text.substring(ingredientsIndex + 15).trim();
            recipe.setTitle(title);
            recipe.setIngredients(ingredients);
        } else {
            // Fallback
            recipe.setTitle("Imported Recipe " + System.currentTimeMillis());
            recipe.setIngredients(text);
        }
    }
}
