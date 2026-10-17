package com.zoffcc.applications.trifa;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import java.util.List;

import static com.zoffcc.applications.trifa.HelperGeneric.dp2px;

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
        TextView vfsIndicator;

        public GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.gallery_image);
            vfsIndicator = itemView.findViewById(R.id.vfs_indicator);
        }

        void bind(Message_for_img msg, MediaGalleryActivity activity) {
            if (msg == null || msg.filename_fullpath == null) {
                imageView.setImageResource(R.drawable.round_loading_animation);
                if (vfsIndicator != null) vfsIndicator.setVisibility(View.GONE);
                return;
            }

            try {
                RequestOptions req_options = new RequestOptions().centerCrop();
                String path = msg.filename_fullpath;
                String uriStr = msg.getfile_uri();
                if (uriStr == null || uriStr.isEmpty()) uriStr = path;

                // Strictly check if it is an actual URI scheme
                boolean is_uri = uriStr.startsWith("content://") || uriStr.startsWith("file://");

                if (msg.direction == TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING.value) {
                    if (is_uri) {
                        GlideApp.with(activity)
                                .load(Uri.parse(uriStr))
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .skipMemoryCache(false)
                                .apply(req_options)
                                .override((int) dp2px(150), (int) dp2px(150))
                                .placeholder(R.drawable.round_loading_animation)
                                .error(R.drawable.round_loading_animation)
                                .into(imageView);
                    } else {
                        // Raw local file path outgoing file
                        GlideApp.with(activity)
                                .load(new java.io.File(path))
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .skipMemoryCache(false)
                                .apply(req_options)
                                .override((int) dp2px(150), (int) dp2px(150))
                                .placeholder(R.drawable.round_loading_animation)
                                .error(R.drawable.round_loading_animation)
                                .into(imageView);
                    }
                } else {
                    if (is_uri) {
                        GlideApp.with(activity)
                                .load(Uri.parse(uriStr))
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .skipMemoryCache(false)
                                .apply(req_options)
                                .override((int) dp2px(150), (int) dp2px(150))
                                .placeholder(R.drawable.round_loading_animation)
                                .error(R.drawable.round_loading_animation)
                                .into(imageView);
                    } else {
                        GlideApp.with(activity)
                                .load(new info.guardianproject.iocipher.File(path))
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .skipMemoryCache(false)
                                .apply(req_options)
                                .override((int) dp2px(150), (int) dp2px(150))
                                .placeholder(R.drawable.round_loading_animation)
                                .error(R.drawable.round_loading_animation)
                                .into(imageView);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                imageView.setImageResource(R.drawable.round_loading_animation);
            }

            if (vfsIndicator != null) {
                if (msg.direction != TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING.value) {
                    vfsIndicator.setVisibility(View.VISIBLE);
                } else {
                    vfsIndicator.setVisibility(View.GONE);
                }
            }
        }
    }
}