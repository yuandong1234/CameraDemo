package com.yuong.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yuong.camera.CameraActivity;
import com.yuong.camera.constant.CameraConfig;
import com.yuong.camera.entity.LocalMedia;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tvFileType;
    private TextView tvFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        Button btnCamera = findViewById(R.id.btn_camera);
        tvFileType = findViewById(R.id.tv_file_type);
        tvFileName = findViewById(R.id.tv_file_name);
        btnCamera.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_camera:
                CameraActivity.actionStart(this);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CameraConfig.CAMERA_REQUEST_CODE) {
            if (data != null && resultCode == CameraConfig.CAMERA_RESULT_CODE) {
                List<LocalMedia> result = data.getParcelableArrayListExtra(CameraConfig.CAMERA_RESULT);
                int type = data.getIntExtra(CameraConfig.CAMERA_RESULT_TYPE, 0);
                setData(result, type);
            }
        }
    }

    private void setData(List<LocalMedia> data, int type) {
        if (type == CameraConfig.TYPE_PICTURE) {
            tvFileType.setText("文件类型：图片");
        } else if (type == CameraConfig.TYPE_VIDEO) {
            tvFileType.setText("文件类型：视频");
        } else {
            tvFileType.setText("文件类型：");
        }

        StringBuilder builder = new StringBuilder();
        builder.append("文件路径：");
        for (LocalMedia temp : data) {
            builder.append(temp.getPath());
            builder.append("\n");
        }
        tvFileName.setText(builder.toString());
    }
}
