package com.zoffcc.applications.trifa;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.zoffcc.applications.sorm.Message;
import java.util.List;

public class MediaGalleryAdapter extends RecyclerView.Adapter<MediaGalleryAdapter.GalleryViewHolder> {

    private List<Message_for_img> imageMessages;
    private MediaGalleryActivity activity;

    public MediaGalleryAdapter(MediaGalleryActivity activity, List<Message_for_img> imageMessages) {
        this.activity = activity;
        this.imageMessages = imageMessages;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_tile, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        Message_for_img msg = imageMessages.get(position);
        holder.bind(msg, activity);

        // Handle click to open full viewer
        holder.itemView.setOnClickListener(v -> {
            activity.openImageInViewer(msg);
        });
    }

    @Override
    public int getItemCount() {
        return imageMessages != null ? imageMessages.size() : 0;
    }

    static class GalleryViewHolder extends RecyclerView.ViewHolder {
        SquareImageView imageView;

        public GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.gallery_image);
        }

        void bind(Message_for_img msg, MediaGalleryActivity activity) {
            if (msg == null || msg.filename_fullpath == null) {
                imageView.setImageResource(R.drawable.round_loading_animation);
                return;
            }

            try {
                RequestOptions req_options = new RequestOptions();

                if (msg.direction == TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING.value) {
                    if (msg.storage_frame_work) {
                        Uri uri = Uri.parse(msg.getfile_uri());
                        GlideApp.with(activity)
                                .load(uri)
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .skipMemoryCache(false)
                                .apply(req_options)
                                .placeholder(R.drawable.round_loading_animation)
                                .into(imageView);
                    } else {
                        java.io.File f2 = new java.io.File(msg.filename_fullpath);
                        GlideApp.with(activity)
                                .load(f2)
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .skipMemoryCache(false)
                                .apply(req_options)
                                .placeholder(R.drawable.round_loading_animation)
                                .into(imageView);
                    }
                } else {
                    info.guardianproject.iocipher.File f2 = new info.guardianproject.iocipher.File(msg.filename_fullpath);
                    GlideApp.with(activity)
                            .load(f2)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .skipMemoryCache(false)
                            .apply(req_options)
                            .placeholder(R.drawable.round_loading_animation)
                            .into(imageView);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // --- VFS Indicator Logic ---
            android.widget.TextView vfsIndicator = itemView.findViewById(R.id.vfs_indicator);
            if (msg.direction != TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING.value) {
                vfsIndicator.setVisibility(android.view.View.VISIBLE);
            } else {
                vfsIndicator.setVisibility(android.view.View.GONE);
            }
        }
    }
}
