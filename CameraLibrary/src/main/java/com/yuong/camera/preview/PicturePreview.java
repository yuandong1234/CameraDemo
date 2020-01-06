package com.yuong.camera.preview;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

public class PicturePreview implements IPreview {
    private ImageView mImageView;

    private PicturePreview() {
    }

    private static class Holder {
        private static final PicturePreview INSTANCE = new PicturePreview();
    }

    public static PicturePreview getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void startPreview(View view, Object result) {
        if (view != null && result != null) {
            this.mImageView = (ImageView) view;
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setImageBitmap((Bitmap) result);
        }
    }

    @Override
    public void stopPreview() {
        if (mImageView != null) {
            mImageView.setVisibility(View.GONE);
            mImageView.setImageResource(0);
        }
    }
}
