package com.zoffcc.applications.trifa;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;
import java.util.List;

public class ImageViewerPagerAdapter extends RecyclerView.Adapter<ImageViewerPagerAdapter.ImageViewHolder> {

    private List<Message_for_img> imageMessages;
    private ImageviewerActivity activity;

    public ImageViewerPagerAdapter(ImageviewerActivity activity, List<Message_for_img> imageMessages) {
        this.activity = activity;
        this.imageMessages = imageMessages;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_imageviewer_page, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Message_for_img msg = imageMessages.get(position);
        holder.bind(msg, activity);
    }

    @Override
    public int getItemCount() {
        return imageMessages != null ? imageMessages.size() : 0;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        PhotoView photoView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photo_view);
        }

        void bind(Message_for_img msg, ImageviewerActivity activity) {
            if (msg == null || msg.filename_fullpath == null) {
                photoView.setImageResource(R.drawable.round_loading_animation);
                return;
            }

            photoView.setImageResource(R.drawable.round_loading_animation);

            try {
                RequestOptions req_options = new RequestOptions();
                String path = msg.filename_fullpath;
                String uriStr = msg.getfile_uri();
                if (uriStr == null || uriStr.isEmpty()) uriStr = path;

                // Strictly check if it is an actual URI scheme
                boolean is_uri = uriStr.startsWith("content://") || uriStr.startsWith("file://");

                if (msg.direction == TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING.value) {
                    if (is_uri) {
                        // SAF / Content URI outgoing file
                        GlideApp.with(activity)
                                .load(Uri.parse(uriStr))
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .skipMemoryCache(false)
                                .apply(req_options)
                                .placeholder(R.drawable.round_loading_animation)
                                .error(R.drawable.round_loading_animation)
                                .into(photoView);
                    } else {
                        // Raw local file path outgoing file (Fixes the StringUri crash)
                        GlideApp.with(activity)
                                .load(new java.io.File(path))
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .skipMemoryCache(false)
                                .apply(req_options)
                                .placeholder(R.drawable.round_loading_animation)
                                .error(R.drawable.round_loading_animation)
                                .into(photoView);
                    }
                } else {
                    if (is_uri) {
                        // Rare case: incoming URI
                        GlideApp.with(activity)
                                .load(Uri.parse(uriStr))
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .skipMemoryCache(false)
                                .apply(req_options)
                                .placeholder(R.drawable.round_loading_animation)
                                .error(R.drawable.round_loading_animation)
                                .into(photoView);
                    } else {
                        // Encrypted VFS incoming file
                        GlideApp.with(activity)
                                .load(new info.guardianproject.iocipher.File(path))
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .skipMemoryCache(false)
                                .apply(req_options)
                                .placeholder(R.drawable.round_loading_animation)
                                .error(R.drawable.round_loading_animation)
                                .into(photoView);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // --- VFS Indicator Logic ---
            android.widget.TextView vfsIndicator = itemView.findViewById(R.id.vfs_indicator);
            if (vfsIndicator != null) {
                if (msg.direction != TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING.value) {
                    vfsIndicator.setVisibility(android.view.View.VISIBLE);
                } else {
                    vfsIndicator.setVisibility(android.view.View.GONE);
                }
            }
        }
    }
}