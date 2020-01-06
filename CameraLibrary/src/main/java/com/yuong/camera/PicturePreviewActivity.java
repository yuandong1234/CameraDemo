package com.yuong.camera;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;

public class PicturePreviewActivity extends AppCompatActivity {
    private static final String PICTURE_PATH = "picture_path";
    private ImageView mPicture;

    public static void actionActivity(Context context, String path) {
        Intent intent = new Intent(context, PicturePreviewActivity.class);
        intent.putExtra(PICTURE_PATH, path);
        context.startActivity(intent);
    }

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_preview);
        initView();
        loadPicture();
    }

    private void initView() {
        ImageView iv_back = findViewById(R.id.iv_back);
        mPicture = findViewById(R.id.iv_picture);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PicturePreviewActivity.this.finish();
            }
        });
    }

    private void loadPicture() {
        String path = getIntent().getStringExtra(PICTURE_PATH);
        if (!TextUtils.isEmpty(path)) {
            loadImage(this, path, mPicture);
        }
    }

    private void loadImage(@NonNull Context context, @NonNull String url, @NonNull ImageView imageView) {
        DrawableCrossFadeFactory drawableCrossFadeFactory =
                new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .centerCrop();
//                .placeholder(R.mipmap.icon_picture_placeholder);
        Glide.with(context).load(url)
                .transition(DrawableTransitionOptions.withCrossFade(drawableCrossFadeFactory))
                .apply(options)
                .into(imageView);
    }


}
