package com.yuong.camera.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.yuong.camera.R;
import com.yuong.camera.adapter.PictureFolderAdapter;
import com.yuong.camera.entity.LocalMedia;
import com.yuong.camera.entity.LocalMediaFolder;

import java.util.List;

public class PictureFolderDialog extends BottomSheetDialog implements PictureFolderAdapter.OnFolderSelectListener {
    private RecyclerView mFolderRecyclerView;
    private PictureFolderAdapter mAdapter;
    private ImageView ivArrowView;
    private PictureFolderAdapter.OnFolderSelectListener folderSelectListener;

    public void setFolderSelectListener(PictureFolderAdapter.OnFolderSelectListener listener) {
        this.folderSelectListener = listener;
    }

    public PictureFolderDialog(@NonNull Context context) {
        super(context);
    }

    public PictureFolderDialog(@NonNull Context context, int theme) {
        super(context, theme);
    }

    public PictureFolderDialog(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dialog_folder);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public void setArrowImageView(ImageView ivArrowView) {
        this.ivArrowView = ivArrowView;
    }

    private void initView() {
        mFolderRecyclerView = findViewById(R.id.reyclerview_folder);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mFolderRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new PictureFolderAdapter(getContext());
        mAdapter.setFolderSelectListener(this);
        mFolderRecyclerView.setAdapter(mAdapter);
    }

    public void setPictureFolders(List<LocalMediaFolder> folders) {
        initView();
        mAdapter.setData(folders);
    }

    @Override
    public void onItemClick(String folderName, List<LocalMedia> images) {
        dismiss();
        if (folderSelectListener != null) {
            folderSelectListener.onItemClick(folderName, images);
        }
    }

    @Override
    public void show() {
        super.show();
        ivArrowView.setImageResource(R.mipmap.icon_arrow_up);
        rotateArrow(ivArrowView, true);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        ivArrowView.setImageResource(R.mipmap.icon_arrow_down);
        rotateArrow(ivArrowView, false);
    }

    private void rotateArrow(ImageView arrow, boolean flag) {
        float pivotX = arrow.getWidth() / 2f;
        float pivotY = arrow.getHeight() / 2f;
        float fromDegrees = flag ? 180f : 180f;
        float toDegrees = flag ? 360f : 0f;
        RotateAnimation animation = new RotateAnimation(fromDegrees, toDegrees, pivotX, pivotY);
        animation.setDuration(350);
        arrow.startAnimation(animation);
    }
}
