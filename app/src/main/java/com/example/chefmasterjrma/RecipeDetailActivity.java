package com.example.chefmasterjrma;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {

    private Uri scanUri; // For the high-res scanner
    private Uri currentCameraUri; // For taking photos/videos

    private EditText editDetailTitle, editDetailContent;
    private Button btnUpdateRecipe, btnShareRecipe, btnAddMedia, btnScanRecipe, btnTakePhoto, btnRecordVideo;
    private View loadingOverlay; // The new bulletproof loading screen

    private RecyclerView recyclerMedia;
    private MediaAdapter mediaAdapter;
    private AppDatabase db;
    private Recipe existingRecipe;
    private int recipeId = -1;

    // Launchers
    private List<Uri> selectedMediaUris = new ArrayList<>();

    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;
    private ActivityResultLauncher<Uri> scanCameraLauncher;
    private ActivityResultLauncher<Uri> takePhotoLauncher;
    private ActivityResultLauncher<Uri> recordVideoLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private Runnable pendingCameraAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // UI Initialization
        editDetailTitle = findViewById(R.id.editDetailTitle);
        editDetailContent = findViewById(R.id.editDetailContent);
        btnUpdateRecipe = findViewById(R.id.btnUpdateRecipe);
        btnShareRecipe = findViewById(R.id.btnShareRecipe);
        btnAddMedia = findViewById(R.id.btnAddMedia);
        btnScanRecipe = findViewById(R.id.btnScanRecipe);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnRecordVideo = findViewById(R.id.btnRecordVideo);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        recyclerMedia = findViewById(R.id.recyclerMedia);

        db = AppDatabase.getDatabase(this);

        // 0. Permission Launcher
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                if (pendingCameraAction != null) {
                    pendingCameraAction.run();
                    pendingCameraAction = null;
                }
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature.", Toast.LENGTH_SHORT).show();
            }
        });

        // 1. Fetch Database Data
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("RECIPE_ID")) {
            recipeId = intent.getIntExtra("RECIPE_ID", -1);
            editDetailTitle.setText(intent.getStringExtra("RECIPE_TITLE"));
            editDetailContent.setText(intent.getStringExtra("RECIPE_CONTENT"));

            new Thread(() -> {
                existingRecipe = db.recipeDao().getRecipeById(recipeId);
                runOnUiThread(() -> {
                    if (existingRecipe != null && existingRecipe.getMediaUrls() != null) {
                        mediaAdapter = new MediaAdapter(this, existingRecipe.getMediaUrls());
                        recyclerMedia.setAdapter(mediaAdapter);
                    }
                });
            }).start();
        } else {
            existingRecipe = new Recipe();
            existingRecipe.setMediaUrls(new ArrayList<>());
            mediaAdapter = new MediaAdapter(this, existingRecipe.getMediaUrls());
            recyclerMedia.setAdapter(mediaAdapter);
        }

        // 2. High-Resolution Scanner Launcher
        scanCameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success && scanUri != null) {
                try {
                    InputImage image = InputImage.fromFilePath(this, scanUri);
                    recognizeTextFromImage(image);
                } catch (Exception e) {
                    Toast.makeText(this, "Scan failed to load.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnScanRecipe.setOnClickListener(v -> checkPermissionAndExecute(() -> {
            try {
                File tempFile = new File(getExternalCacheDir(), "SCAN_HD_" + System.currentTimeMillis() + ".jpg");
                scanUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", tempFile);
                scanCameraLauncher.launch(scanUri);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error opening scanner: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));

        // 3. Direct Camera Capture Launchers
        takePhotoLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success && currentCameraUri != null) {
                selectedMediaUris.add(currentCameraUri);
                Toast.makeText(this, "📸 Photo staged! Please tap 'Save' at the bottom.", Toast.LENGTH_LONG).show();
            }
        });

        recordVideoLauncher = registerForActivityResult(new ActivityResultContracts.CaptureVideo(), success -> {
            if (success && currentCameraUri != null) {
                selectedMediaUris.add(currentCameraUri);
                Toast.makeText(this, "🎥 Video staged! Please tap 'Save' at the bottom.", Toast.LENGTH_LONG).show();
            }
        });

        btnTakePhoto.setOnClickListener(v -> checkPermissionAndExecute(() -> {
            try {
                File tempFile = new File(getExternalCacheDir(), "CAMERA_IMG_" + System.currentTimeMillis() + ".jpg");
                currentCameraUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", tempFile);
                takePhotoLauncher.launch(currentCameraUri);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error opening photo camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));

        btnRecordVideo.setOnClickListener(v -> checkPermissionAndExecute(() -> {
            try {
                File tempFile = new File(getExternalCacheDir(), "CAMERA_VID_" + System.currentTimeMillis() + ".mp4");
                currentCameraUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", tempFile);
                recordVideoLauncher.launch(currentCameraUri);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error opening video camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));

        // 4. Gallery Picker
        pickMultipleMedia = registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(10), uris -> {
            if (!uris.isEmpty()) {
                selectedMediaUris.addAll(uris);
                Toast.makeText(this, "📁 Media staged! Please tap 'Save' at the bottom.", Toast.LENGTH_LONG).show();
            }
        });

        btnAddMedia.setOnClickListener(v -> pickMultipleMedia.launch(
                new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE).build()
        ));

        // 5. Save & Show Bulletproof Loading Overlay
        btnUpdateRecipe.setOnClickListener(v -> {
            if (editDetailTitle.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
                return;
            }
            btnUpdateRecipe.setEnabled(false);
            loadingOverlay.setVisibility(View.VISIBLE);

            new Thread(this::processAndSaveMedia).start();
        });

        // 6. EXPORT RECIPE AS A .ZIP FOLDER TO DOWNLOADS
        btnShareRecipe.setText("Export Backup (.zip)");
        btnShareRecipe.setOnClickListener(v -> {
            if (existingRecipe == null || existingRecipe.getTitle() == null) {
                Toast.makeText(this, "Please save the recipe first before exporting!", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Generating ZIP Backup...", Toast.LENGTH_SHORT).show();
            loadingOverlay.setVisibility(View.VISIBLE);

            new Thread(() -> {
                File zipFile = ZipManager.createRecipeZip(this, existingRecipe);

                if (zipFile != null) {
                    try {
                        android.content.ContentResolver resolver = getContentResolver();
                        android.content.ContentValues contentValues = new android.content.ContentValues();
                        contentValues.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, zipFile.getName());
                        contentValues.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/zip");

                        Uri uri = null;

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            contentValues.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS);
                            uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                        } else {
                            // Fallback for Android 9 and below
                            File downloadDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
                            File destFile = new File(downloadDir, zipFile.getName());
                            uri = Uri.fromFile(destFile);
                        }

                        if (uri != null) {
                            OutputStream os = resolver.openOutputStream(uri);
                            FileInputStream fis = new FileInputStream(zipFile);
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = fis.read(buffer)) > 0) os.write(buffer, 0, length);
                            fis.close();
                            if (os != null) os.close();
                        }

                        runOnUiThread(() -> {
                            loadingOverlay.setVisibility(View.GONE);
                            Toast.makeText(this, "✅ ZIP saved to Downloads Folder!", Toast.LENGTH_LONG).show();
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            loadingOverlay.setVisibility(View.GONE);
                            Toast.makeText(this, "❌ Failed to save to Downloads.", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }).start();
        });
    }

    // --- HELPER METHODS ---

    private void checkPermissionAndExecute(Runnable action) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            action.run();
        } else {
            pendingCameraAction = action;
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void recognizeTextFromImage(InputImage image) {
        Toast.makeText(this, "Scanning high-res text...", Toast.LENGTH_SHORT).show();
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    StringBuilder formattedText = new StringBuilder();
                    for (com.google.mlkit.vision.text.Text.TextBlock block : visionText.getTextBlocks()) {
                        for (com.google.mlkit.vision.text.Text.Line line : block.getLines()) {
                            formattedText.append(line.getText()).append("\n");
                        }
                        formattedText.append("\n");
                    }

                    String cleanedText = formattedText.toString()
                            .replaceAll("(?m)^[\\*\\-\\•]\\s*", "• ")
                            .replaceAll("(?i)\\b(tbs|tbsp)\\b", "Tbsp")
                            .replaceAll("(?i)\\b(tsp)\\b", "tsp")
                            .replaceAll("(?m)^\\s+$", "")
                            .trim();

                    if (cleanedText.isEmpty()) {
                        Toast.makeText(this, "No text found. Try better lighting!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String currentText = editDetailContent.getText().toString();
                    editDetailContent.setText(currentText + "\n\n--- Scanned Recipe ---\n\n" + cleanedText);
                    Toast.makeText(this, "✨ Scan Complete!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Scan Failed", Toast.LENGTH_SHORT).show());
    }

    private void processAndSaveMedia() {
        List<String> finalSavedPaths = new ArrayList<>();
        for (Uri fileUri : selectedMediaUris) {
            String localPath = copyToInternalStorage(fileUri);
            if (localPath != null) finalSavedPaths.add(localPath);
        }
        updateRecipeInDatabase(finalSavedPaths);
    }

    private String copyToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            String mimeType = getContentResolver().getType(uri);

            String extension = ".jpg";
            if ((mimeType != null && mimeType.startsWith("video")) || uri.toString().contains(".mp4")) {
                extension = ".mp4";
            }

            File file = new File(getFilesDir(), "media_" + System.currentTimeMillis() + extension);
            OutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) outputStream.write(buffer, 0, length);

            outputStream.close();
            inputStream.close();
            return file.getAbsolutePath();
        } catch (Exception e) { return null; }
    }

    private void updateRecipeInDatabase(List<String> newMediaPaths) {
        existingRecipe.setTitle(editDetailTitle.getText().toString());
        existingRecipe.setIngredients(editDetailContent.getText().toString());
        existingRecipe.setLastEdited(System.currentTimeMillis());

        if (!newMediaPaths.isEmpty()) {
            List<String> currentUrls = existingRecipe.getMediaUrls();
            if (currentUrls == null) currentUrls = new ArrayList<>();
            currentUrls.addAll(newMediaPaths);
            existingRecipe.setMediaUrls(currentUrls);
        }

        if (recipeId == -1) db.recipeDao().insert(existingRecipe);
        else db.recipeDao().update(existingRecipe);

        runOnUiThread(() -> {
            loadingOverlay.setVisibility(View.GONE);
            btnUpdateRecipe.setEnabled(true);
            selectedMediaUris.clear();
            Toast.makeText(this, "✅ Recipe Saved Successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}