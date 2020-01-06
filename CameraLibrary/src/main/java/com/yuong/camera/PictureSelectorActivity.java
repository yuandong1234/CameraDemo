package com.yuong.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yuong.camera.adapter.PictureFolderAdapter;
import com.yuong.camera.adapter.PictureImageGridAdapter;
import com.yuong.camera.constant.CameraConfig;
import com.yuong.camera.dialog.PictureFolderDialog;
import com.yuong.camera.dialog.PictureLoadingDialog;
import com.yuong.camera.entity.LocalMedia;
import com.yuong.camera.entity.LocalMediaFolder;
import com.yuong.camera.model.LocalMediaLoader;
import com.yuong.camera.utils.DensityUtil;
import com.yuong.camera.view.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class PictureSelectorActivity extends AppCompatActivity implements View.OnClickListener,
        PictureFolderAdapter.OnFolderSelectListener, PictureImageGridAdapter.OnImageSelectListener {
    private static final int SHOW_DIALOG = 0;
    private static final int DISMISS_DIALOG = 1;

    private TextView tvConfirm;
    private TextView tvTitle;
    private ImageView ivArrow;
    private RecyclerView mPictureRecyclerView;
    private TextView mTvEmpty;

    private LocalMediaLoader mediaLoader;
    private List<LocalMediaFolder> foldersList = new ArrayList<>();
    private List<LocalMedia> images = new ArrayList<>();
    private PictureImageGridAdapter mAdapter;

    private PictureLoadingDialog mLoadingDialog;
    private PictureFolderDialog mFolderDialog;

    private int mMaxNum;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_DIALOG:
                    showPleaseDialog();
                    break;
                case DISMISS_DIALOG:
                    dismissDialog();
                    break;
                default:
                    break;
            }
        }
    };

    public static void actionStart(Activity activity, int maxNum) {
        Intent intent = new Intent(activity, PictureSelectorActivity.class);
        intent.putExtra(CameraConfig.PICTURE_MAX_SIZE, maxNum);
        activity.startActivityForResult(intent, CameraConfig.PICTURE_SELECTOR_REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_selector);
        mMaxNum = getIntent().getIntExtra(CameraConfig.PICTURE_MAX_SIZE, 1);
        initView();
        readLocalMedia();
    }

    private void initView() {
        ImageView iv_back = findViewById(R.id.iv_back);
        tvConfirm = findViewById(R.id.tv_confirm);
        LinearLayout layout_title = findViewById(R.id.layout_title);
        tvTitle = findViewById(R.id.tv_title);
        ivArrow = findViewById(R.id.iv_arrow);

        mPictureRecyclerView = findViewById(R.id.picture_recycler);
        mTvEmpty = findViewById(R.id.tv_empty);

        iv_back.setOnClickListener(this);
        tvConfirm.setOnClickListener(this);
        layout_title.setOnClickListener(this);

        mPictureRecyclerView.setHasFixedSize(true);
        mPictureRecyclerView.addItemDecoration(new GridSpacingItemDecoration(4,
                DensityUtil.dp2px(this, 2), false));
        mPictureRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        // 解决调用 notifyItemChanged 闪烁问题,取消默认动画
        ((SimpleItemAnimator) mPictureRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        mAdapter = new PictureImageGridAdapter(this);
        mAdapter.setMaxNum(mMaxNum);
        mAdapter.setSelectListener(this);
        mPictureRecyclerView.setAdapter(mAdapter);

        mFolderDialog = new PictureFolderDialog(this);
        mFolderDialog.setFolderSelectListener(this);
        mFolderDialog.setArrowImageView(ivArrow);
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_back) {
            finish();
        } else if (i == R.id.tv_confirm) {
            if (tvConfirm.getText().toString().trim().equals(getString(R.string.cancel))) {
                finish();
            } else {
                complete();
            }
        } else if (i == R.id.layout_title) {
            showFolderDialog();
        }
    }

    protected void readLocalMedia() {
        mHandler.sendEmptyMessage(SHOW_DIALOG);
        if (mediaLoader == null) {
            mediaLoader = new LocalMediaLoader(this);
        }
        mediaLoader.loadAllMedia();
        mediaLoader.setCompleteListener(new LocalMediaLoader.LocalMediaLoadListener() {
            @Override
            public void loadComplete(List<LocalMediaFolder> folders) {
                if (folders.size() > 0) {
                    foldersList = folders;
                    LocalMediaFolder folder = folders.get(0);
                    folder.setChecked(true);
                    List<LocalMedia> result = folder.getImages();
                    images = result;
                    foldersList = folders;
                }
                setImageList();
                mHandler.sendEmptyMessage(DISMISS_DIALOG);
            }

            @Override
            public void loadMediaDataError() {
                mHandler.sendEmptyMessage(DISMISS_DIALOG);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    mTvEmpty.setCompoundDrawablesRelativeWithIntrinsicBounds
                            (0, R.mipmap.icon_data_error, 0, 0);
                }
                mTvEmpty.setText(getString(R.string.picture_data_exception));
                mTvEmpty.setVisibility(images.size() > 0 ? View.INVISIBLE : View.VISIBLE);
            }
        });
    }

    private void setImageList() {
        if (mAdapter != null) {
            mAdapter.setData(images);
            boolean isEmpty = images.size() > 0;
            if (!isEmpty) {
                mTvEmpty.setText(getString(R.string.picture_empty));
                mTvEmpty.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.mipmap.icon_no_data, 0, 0);
            }
            mTvEmpty.setVisibility(isEmpty ? View.INVISIBLE : View.VISIBLE);
        }
    }


    @Override
    public void onItemClick(String folderName, List<LocalMedia> images) {
        this.images = images;
        tvTitle.setText(folderName);
        setImageList();
    }

    @Override
    public void OnImageSelect(LocalMedia image, boolean isCheck, int selectCount) {
        if (mMaxNum > 1 && selectCount > 0) {
            tvConfirm.setText(String.format(getString(R.string.next2), selectCount, mMaxNum));
        } else if (mMaxNum == 1 && selectCount > 0) {
            tvConfirm.setText(getString(R.string.next));
        } else {
            tvConfirm.setText(getString(R.string.cancel));
        }
    }

    //完成选择
    private void complete() {
        List<LocalMedia> selectImages = mAdapter.getSelectedImages();
        if (selectImages.size() > 0) {
            Intent data = new Intent();
            data.putParcelableArrayListExtra(CameraConfig.PICTURE_SELECTED_RESULT, (ArrayList<? extends Parcelable>) selectImages);
            setResult(CameraConfig.PICTURE_SELECTOR_RESULT_CODE, data);
            finish();
        } else {
            Toast.makeText(this, getString(R.string.picture_select_tip), Toast.LENGTH_SHORT).show();
        }
    }

    private void showPleaseDialog() {
        if (!isFinishing()) {
            if (mLoadingDialog == null) {
                mLoadingDialog = new PictureLoadingDialog(this);
            }
            if (mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
            }
            mLoadingDialog.show();
        }
    }

    private void dismissDialog() {
        if (!isFinishing()) {
            try {
                if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                    mLoadingDialog.dismiss();
                }
            } catch (Exception e) {
                mLoadingDialog = null;
                e.printStackTrace();
            }
        }
    }

    private void showFolderDialog() {
        if (!isFinishing()) {
            if (mFolderDialog.isShowing()) {
                mFolderDialog.dismiss();
            }
            mFolderDialog.show();
            mFolderDialog.setPictureFolders(foldersList);
        }
    }

    private void dismissFolderDialog() {
        if (!isFinishing()) {
            try {
                if (mFolderDialog != null && mFolderDialog.isShowing()) {
                    mFolderDialog.dismiss();
                }
            } catch (Exception e) {
                mFolderDialog = null;
                e.printStackTrace();
            }
        }
    }
}
