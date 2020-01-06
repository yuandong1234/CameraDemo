package com.yuong.camera.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.yuong.camera.R;
import com.yuong.camera.entity.LocalMedia;
import com.yuong.camera.entity.LocalMediaFolder;
import com.yuong.camera.view.SquareImageView;

import java.util.ArrayList;
import java.util.List;


public class PictureFolderAdapter extends RecyclerView.Adapter<PictureFolderAdapter.ViewHolder> {

    private Context context;
    private List<LocalMediaFolder> folders = new ArrayList<>();
    private OnFolderSelectListener folderSelectListener;

    public void setFolderSelectListener(OnFolderSelectListener listener) {
        this.folderSelectListener = listener;
    }

    public PictureFolderAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<LocalMediaFolder> folders) {
        this.folders = folders == null ? new ArrayList<LocalMediaFolder>() : folders;
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_picture_folder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        LocalMediaFolder folder = folders.get(position);
        String path = folder.getFirstImagePath();
        String name = folder.getName();
        int imageNum = folder.getImageNum();
        boolean isChecked = folder.isChecked();
        loadImage(context, path, holder.ivPicture);
        holder.tvFolderName.setText(name);
        holder.tvFileSize.setText(String.format(context.getResources().getString(R.string.picture_size), imageNum));
        holder.contentView.setSelected(isChecked);
        holder.contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (folderSelectListener != null) {
                    int size = folders.size();
                    for (int i = 0; i < size; i++) {
                        LocalMediaFolder mediaFolder = folders.get(i);
                        mediaFolder.setChecked(false);
                    }
                    folder.setChecked(true);
                    notifyDataSetChanged();
                    folderSelectListener.onItemClick(folder.getName(), folder.getImages());
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return folders.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout contentView;
        private SquareImageView ivPicture;
        private TextView tvFolderName;
        private TextView tvFileSize;

        public ViewHolder(View itemView) {
            super(itemView);
            ivPicture = itemView.findViewById(R.id.ivPicture);
            contentView = itemView.findViewById(R.id.contentView);
            ivPicture = itemView.findViewById(R.id.ivPicture);
            tvFolderName = itemView.findViewById(R.id.tv_folder_name);
            tvFileSize = itemView.findViewById(R.id.tv_file_size);
        }
    }

    private void loadImage(@NonNull Context context, @NonNull String url, @NonNull ImageView imageView) {
        Glide.with(context)
                .asBitmap()
                .load(url)
                .override(180, 180)
                .centerCrop()
                .apply(new RequestOptions().placeholder(R.mipmap.icon_picture_placeholder))
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCornerRadius(8);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });

    }

    public interface OnFolderSelectListener {
        void onItemClick(String folderName, List<LocalMedia> images);
    }
}
