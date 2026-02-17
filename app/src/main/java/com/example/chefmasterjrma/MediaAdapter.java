package com.example.chefmasterjrma;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

    private Context context;
    private List<String> mediaUrls;

    public MediaAdapter(Context context, List<String> mediaUrls) {
        this.context = context;
        this.mediaUrls = mediaUrls;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        String url = mediaUrls.get(position);

        Glide.with(context).load(url).centerCrop().into(holder.imageMedia);

        // Attach click directly to the image
        holder.imageMedia.setOnClickListener(v -> {
            android.app.Dialog dialog = new android.app.Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.setContentView(R.layout.dialog_media);

            androidx.viewpager2.widget.ViewPager2 viewPager = dialog.findViewById(R.id.viewPagerMedia);
            android.widget.ImageButton btnClose = dialog.findViewById(R.id.btnClose);

            FullScreenMediaAdapter fullScreenAdapter = new FullScreenMediaAdapter(context, mediaUrls);
            viewPager.setAdapter(fullScreenAdapter);
            viewPager.setCurrentItem(position, false);

            btnClose.setOnClickListener(view -> dialog.dismiss());
            dialog.show();
        });

        // DOWNLOAD BUTTON CLICK
        holder.btnDownloadMedia.setOnClickListener(v -> downloadMediaToGallery(url));

        // DELETE BUTTON CLICK
        holder.btnDeleteMedia.setOnClickListener(v -> {
            mediaUrls.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mediaUrls.size());
            Toast.makeText(context, "Removed! Please tap 'Save' below.", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public int getItemCount() {
        return mediaUrls != null ? mediaUrls.size() : 0;
    }

    // Modern Scoped-Storage compliant download method with backwards compatibility
    private void downloadMediaToGallery(String sourcePath) {
        try {
            File sourceFile = new File(sourcePath);
            android.content.ContentResolver resolver = context.getContentResolver();
            android.content.ContentValues contentValues = new android.content.ContentValues();

            contentValues.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "RecipeMedia_" + System.currentTimeMillis() + "_" + sourceFile.getName());
            contentValues.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, sourcePath.endsWith(".mp4") ? "video/mp4" : "image/jpeg");

            Uri uri = null;

            // Ask Android for secure permission to create the file
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // For Android 10 and above
                contentValues.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS);
                uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
            } else {
                // Fallback for Android 9 and below
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File destFile = new File(downloadDir, "RecipeMedia_" + System.currentTimeMillis() + "_" + sourceFile.getName());
                uri = Uri.fromFile(destFile);
            }

            if (uri != null) {
                java.io.OutputStream fos = resolver.openOutputStream(uri);
                java.io.FileInputStream fis = new java.io.FileInputStream(sourceFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                fis.close();
                if (fos != null) fos.close();
                Toast.makeText(context, "✅ Downloaded to your public Downloads folder!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "❌ Failed to download due to phone security.", Toast.LENGTH_SHORT).show();
        }
    }

    public static class MediaViewHolder extends RecyclerView.ViewHolder {
        ImageView imageMedia;
        android.widget.ImageButton btnDeleteMedia;
        android.widget.ImageButton btnDownloadMedia;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            imageMedia = itemView.findViewById(R.id.imageMedia);
            btnDeleteMedia = itemView.findViewById(R.id.btnDeleteMedia);
            btnDownloadMedia = itemView.findViewById(R.id.btnDownloadMedia);
        }
    }
}