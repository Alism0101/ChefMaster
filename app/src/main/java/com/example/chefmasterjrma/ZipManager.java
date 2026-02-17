package com.example.chefmasterjrma;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipManager {

    // Bundles the recipe text and media into a temporary .zip file
    public static File createRecipeZip(Context context, Recipe recipe) {
        try {
            // 1. Create a temporary ZIP file
            File zipFile = new File(context.getCacheDir(), "Export_" + recipe.getTitle().replaceAll("\\s+", "_") + ".zip");
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            // 2. Write the Recipe Text as a .txt file inside the ZIP
            ZipEntry textEntry = new ZipEntry("recipe_text.txt");
            zos.putNextEntry(textEntry);
            String recipeData = "TITLE: " + recipe.getTitle() + "\n\nINGREDIENTS:\n" + recipe.getIngredients();
            zos.write(recipeData.getBytes());
            zos.closeEntry();

            // 3. Add all Photos and Videos to the ZIP
            List<String> mediaUrls = recipe.getMediaUrls();
            if (mediaUrls != null) {
                for (int i = 0; i < mediaUrls.size(); i++) {
                    File mediaFile = new File(mediaUrls.get(i));
                    if (mediaFile.exists()) {
                        FileInputStream fis = new FileInputStream(mediaFile);
                        // Name files sequentially (e.g., media_1.jpg)
                        String extension = mediaFile.getName().endsWith(".mp4") ? ".mp4" : ".jpg";
                        ZipEntry mediaEntry = new ZipEntry("media_" + (i + 1) + extension);
                        zos.putNextEntry(mediaEntry);

                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, length);
                        }
                        zos.closeEntry();
                        fis.close();
                    }
                }
            }

            zos.close();
            fos.close();
            return zipFile;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}