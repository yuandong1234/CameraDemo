package com.yuong.camera.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.yuong.camera.PicturePreviewActivity;
import com.yuong.camera.R;
import com.yuong.camera.entity.LocalMedia;
import com.yuong.camera.view.SquareImageView;

import java.util.ArrayList;
import java.util.List;


public class PictureImageGridAdapter extends RecyclerView.Adapter<PictureImageGridAdapter.ViewHolder> {

    private Context context;
    private List<LocalMedia> images = new ArrayList<>();
    private List<LocalMedia> selectImages = new ArrayList<>();
    private Animation animation;
    private int maxNum = 9;
    private OnImageSelectListener selectListener;

    public void setMaxNum(int maxNum) {
        this.maxNum = maxNum;
    }

    public void setSelectListener(OnImageSelectListener selectListener) {
        this.selectListener = selectListener;
    }

    public PictureImageGridAdapter(Context context) {
        this.context = context;
        this.animation = AnimationUtils.loadAnimation(context, R.anim.anim_picture_modal_in);
    }

    public void setData(List<LocalMedia> images) {
        resetData();//恢复数据源
        this.images.clear();
        selectImages.clear();
        if (images != null && images.size() > 0) {
            this.images.addAll(images);
            notifyDataSetChanged();
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_picture_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocalMedia image = images.get(position);
        image.position = position;
        String path = image.getPath();
        loadImage(context, path, holder.ivPicture);
        if (image.isChecked()) {
            holder.tvNum.setSelected(true);
            holder.tvNum.setText(String.valueOf(image.getCheckedNum()));
        } else {
            holder.tvNum.setSelected(false);
            holder.tvNum.setText("");
        }
        holder.tvNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (image.isChecked()) {
                    setImageUnChecked(image);
                } else {
                    if (maxNum == 1) {//单选
                        selectImages.clear();
                        resetData();
                        setImageChecked(holder, image);
                    } else {//多选
                        if (selectImages.size() < maxNum) {
                            setImageChecked(holder, image);
                        } else {
                            Toast.makeText(context, String.format(context.getString(R.string.picture_size_max), maxNum), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        holder.contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PicturePreviewActivity.actionActivity(context, image.getPath());
            }
        });
    }


    @Override
    public int getItemCount() {
        return images.size();
    }

    private void setImageChecked(ViewHolder holder, LocalMedia localMedia) {
        selectImages.add(localMedia);
        localMedia.setChecked(true);
        localMedia.setCheckedNum(selectImages.size());
        holder.tvNum.setSelected(true);
        holder.tvNum.setText(String.valueOf(localMedia.getCheckedNum()));
        //notifyDataSetChanged();
        if (selectListener != null) {
            selectListener.OnImageSelect(localMedia, true, selectImages.size());
        }
    }

    private void setImageUnChecked(LocalMedia localMedia) {
        resetImageCheckedNum(localMedia);//重新设置选中图片的序列号
        localMedia.setChecked(false);
        localMedia.setCheckedNum(0);
        removeSelectImage(localMedia);
        notifyDataSetChanged();
        if (selectListener != null) {
            selectListener.OnImageSelect(localMedia, false, selectImages.size());
        }
    }

    private void removeSelectImage(LocalMedia localMedia) {
        LocalMedia resultMedia = null;
        for (LocalMedia media : selectImages) {
            if (localMedia.getPath().equals(media.getPath())) {
                resultMedia = media;
                break;
            }
        }

        if (resultMedia != null) {
            selectImages.remove(resultMedia);
        }
    }

    private void resetImageCheckedNum(LocalMedia media) {
        for (LocalMedia temp : selectImages) {
            if (temp.getCheckedNum() > media.getCheckedNum()) {
                temp.setCheckedNum(temp.getCheckedNum() - 1);
            }
        }
    }

    public List<LocalMedia> getSelectedImages() {
        return selectImages;
    }

    public void resetData() {
        for (LocalMedia temp : images) {
            temp.setChecked(false);
            temp.setCheckedNum(0);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private FrameLayout contentView;
        private SquareImageView ivPicture;
        private TextView tvNum;

        public ViewHolder(View itemView) {
            super(itemView);
            contentView = itemView.findViewById(R.id.contentView);
            ivPicture = itemView.findViewById(R.id.ivPicture);
            tvNum = itemView.findViewById(R.id.tv_num);
        }
    }

    private void loadImage(@NonNull Context context, @NonNull String url, @NonNull ImageView imageView) {
        DrawableCrossFadeFactory drawableCrossFadeFactory =
                new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .centerCrop()
                .placeholder(R.mipmap.icon_picture_placeholder);
        Glide.with(context).load(url)
                .transition(DrawableTransitionOptions.withCrossFade(drawableCrossFadeFactory))
                .apply(options)
                .into(imageView);
    }

    public interface OnImageSelectListener {
        void OnImageSelect(LocalMedia image, boolean isCheck, int selectCount);
    }
}
