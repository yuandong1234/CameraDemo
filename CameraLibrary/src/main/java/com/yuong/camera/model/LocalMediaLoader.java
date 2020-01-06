package com.yuong.camera.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.yuong.camera.R;
import com.yuong.camera.entity.LocalMedia;
import com.yuong.camera.entity.LocalMediaFolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocalMediaLoader implements Handler.Callback {
    private static final int MSG_QUERY_MEDIA_SUCCESS = 0;
    private static final int MSG_QUERY_MEDIA_ERROR = -1;

    private static String DURATION = "duration";
    private static String BUCKET_DISPLAY_NAME = "bucket_display_name";
    private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");
    private static final String SELECTION = MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
            + " AND " + MediaStore.MediaColumns.SIZE + ">0";
    private static final String[] SELECTION_ARGS = new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)};
    private static final String ORDER_BY = MediaStore.Files.FileColumns._ID + " DESC";

    private Context mContext;
    private Handler mHandler;
    private LocalMediaLoadListener mLoadListener;

    /**
     * 媒体文件数据库字段
     */
    private static final String[] MEDIA_COLUMNS = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            DURATION,
            MediaStore.MediaColumns.SIZE,
            BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DISPLAY_NAME};

    public LocalMediaLoader(Context context) {
        this.mContext = context.getApplicationContext();
        this.mHandler = new Handler(Looper.getMainLooper(), this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (mLoadListener == null) return false;
        switch (msg.what) {
            case MSG_QUERY_MEDIA_SUCCESS:
                mLoadListener.loadComplete((List<LocalMediaFolder>) msg.obj);
                break;
            case MSG_QUERY_MEDIA_ERROR:
                mLoadListener.loadMediaDataError();
                break;
        }
        return false;
    }

    public void loadAllMedia() {
        AsyncTask.SERIAL_EXECUTOR.execute(() -> {
            Cursor data = mContext.getContentResolver().query(QUERY_URI, MEDIA_COLUMNS,
                    SELECTION,
                    SELECTION_ARGS,
                    ORDER_BY);
            try {
                if (data != null) {
                    List<LocalMediaFolder> imageFolders = new ArrayList<>();
                    LocalMediaFolder allImageFolder = new LocalMediaFolder();
                    List<LocalMedia> latelyImages = new ArrayList<>();
                    int count = data.getCount();
                    if (count > 0) {
                        data.moveToFirst();
                        do {
                            long id = data.getLong(data.getColumnIndexOrThrow(MEDIA_COLUMNS[0]));

                            String path = isAndroidQ() ? getRealPathAndroid_Q(id) : data.getString
                                    (data.getColumnIndexOrThrow(MEDIA_COLUMNS[1]));

                            String mimeType = data.getString
                                    (data.getColumnIndexOrThrow(MEDIA_COLUMNS[2]));

                            int width = data.getInt
                                    (data.getColumnIndexOrThrow(MEDIA_COLUMNS[3]));

                            int height = data.getInt
                                    (data.getColumnIndexOrThrow(MEDIA_COLUMNS[4]));

                            long duration = data.getLong
                                    (data.getColumnIndexOrThrow(MEDIA_COLUMNS[5]));

                            long size = data.getLong
                                    (data.getColumnIndexOrThrow(MEDIA_COLUMNS[6]));

                            String folderName = data.getString
                                    (data.getColumnIndexOrThrow(MEDIA_COLUMNS[7]));

                            String fileName = data.getString
                                    (data.getColumnIndexOrThrow(MEDIA_COLUMNS[8]));


                            LocalMedia image = new LocalMedia
                                    (id, path, fileName, duration, 1, mimeType, width, height, size);
                            LocalMediaFolder folder = getImageFolder(path, folderName, imageFolders);
                            List<LocalMedia> images = folder.getImages();
                            images.add(image);
                            folder.setImageNum(folder.getImageNum() + 1);
                            latelyImages.add(image);
                            int imageNum = allImageFolder.getImageNum();
                            allImageFolder.setImageNum(imageNum + 1);

                        } while (data.moveToNext());

                        if (latelyImages.size() > 0) {
                            sortFolder(imageFolders);
                            imageFolders.add(0, allImageFolder);
                            allImageFolder.setFirstImagePath(latelyImages.get(0).getPath());
                            allImageFolder.setName(mContext.getString(R.string.picture_image));
                            allImageFolder.setOfAllType(1);
                            allImageFolder.setCameraFolder(true);
                            allImageFolder.setImages(latelyImages);
                        }
                    }
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_QUERY_MEDIA_SUCCESS, imageFolders));
                } else {
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_QUERY_MEDIA_ERROR));
                }
            } catch (Exception e) {
                if (mHandler != null) {
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_QUERY_MEDIA_ERROR));
                }
                e.printStackTrace();
            }
        });
    }

    /**
     * 创建相应文件夹
     *
     * @param path
     * @param imageFolders
     * @param folderName
     * @return
     */
    private LocalMediaFolder getImageFolder(String path, String folderName, List<LocalMediaFolder> imageFolders) {
        for (LocalMediaFolder folder : imageFolders) {
            // 同一个文件夹下，返回自己，否则创建新文件夹
            String name = folder.getName();
            if (TextUtils.isEmpty(name)) {
                continue;
            }
            if (name.equals(folderName)) {
                return folder;
            }
        }
        LocalMediaFolder newFolder = new LocalMediaFolder();
        newFolder.setName(folderName);
        newFolder.setFirstImagePath(path);
        imageFolders.add(newFolder);
        return newFolder;
    }

    /**
     * 文件夹数量进行排序
     */
    private void sortFolder(List<LocalMediaFolder> imageFolders) {
        // 文件夹按图片数量排序
        Collections.sort(imageFolders, (lhs, rhs) -> {
            if (lhs.getImages() == null || rhs.getImages() == null) {
                return 0;
            }
            int lsize = lhs.getImageNum();
            int rsize = rhs.getImageNum();
            return lsize == rsize ? 0 : (lsize < rsize ? 1 : -1);
        });
    }

    private boolean isAndroidQ() {
        return Build.VERSION.SDK_INT >= 29;
    }

    /**
     * 适配Android Q
     */
    private String getRealPathAndroid_Q(long id) {
        return QUERY_URI.buildUpon().appendPath(String.valueOf(id)).build().toString();
    }

    public void setCompleteListener(LocalMediaLoadListener loadListener) {
        this.mLoadListener = loadListener;
    }

    public interface LocalMediaLoadListener {

        void loadComplete(List<LocalMediaFolder> folders);

        void loadMediaDataError();
    }
}
