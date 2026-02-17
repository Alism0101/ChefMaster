package com.example.chefmasterjrma;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class FullScreenMediaAdapter extends RecyclerView.Adapter<FullScreenMediaAdapter.ViewHolder> {

    private Context context;
    private List<String> mediaUrls;

    public FullScreenMediaAdapter(Context context, List<String> mediaUrls) {
        this.context = context;
        this.mediaUrls = mediaUrls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_fullscreen_media, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String url = mediaUrls.get(position);

        // Robust video check
        if (url.endsWith(".mp4") || url.contains("video")) {
            holder.fullscreenImage.setVisibility(View.GONE);
            holder.fullscreenVideo.setVisibility(View.VISIBLE);

            holder.fullscreenVideo.setVideoPath(url);
            MediaController mediaController = new MediaController(context);
            mediaController.setAnchorView(holder.fullscreenVideo);
            holder.fullscreenVideo.setMediaController(mediaController);
            holder.fullscreenVideo.start();
        } else {
            holder.fullscreenVideo.setVisibility(View.GONE);
            holder.fullscreenImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(url).into(holder.fullscreenImage);
        }
    }

    @Override
    public int getItemCount() {
        return mediaUrls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        com.github.chrisbanes.photoview.PhotoView fullscreenImage; // CHANGED THIS LINE
        VideoView fullscreenVideo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fullscreenImage = itemView.findViewById(R.id.fullscreenImage);
            fullscreenVideo = itemView.findViewById(R.id.fullscreenVideo);
        }
    }
}