package com.example.myapplication.app;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.caffe.android.CaffeMobile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author gdh1995
 */
public class CNNWorker implements Runnable {
    private Handler handler;
    private File file;
    private Context context;

    public CNNWorker(Handler handler, Context context, File file) {
        this.handler = handler;
        this.context = context;
        this.file = file;
    }

    public void run() {
        int [] result = work(context);
        Bundle data = new Bundle();
        data.putIntArray("result", result);
        Message msg = new Message();
        msg.setData(data);
        handler.sendMessage(msg);
    }

    /**
     * 使用caffe-android-lib进行分类
     *
     * @return 分类结果的top-K
     */
    public int[] work(Context context) {
        int [] result = null, testCategories = null;
        AssetCopyer copy_manager = new AssetCopyer(context);
        List<String> model_files;
        File storedDir = context.getExternalCacheDir();
        storedDir = storedDir != null ? storedDir : context.getCacheDir();
        storedDir = storedDir != null ? storedDir : context.getFilesDir();
        copy_manager.init("zipped_caffenet/caffe.lst", storedDir, 0);
        model_files = copy_manager.ensure();
        if (model_files == null) {
            Log.e("CNNWorker", "fail in releasing default model");
            return null;
        }
        try {
            CaffeMobile caffeMobile = CaffeMobile.Get();
            if (!caffeMobile.hasInit()) {
                caffeMobile.enableLog(true);
                caffeMobile.loadModel(model_files.get(0), model_files.get(1));
            }
            // if (model_files.size() >= 3) {
            //     testCategories = caffeMobile.predictTopK(model_files.get(2), 3);
            // }
            result = caffeMobile.predictTopK(file.getCanonicalPath(), 10);
            Log.d("CNN Run", String.format("%d * %d", caffeMobile.getOutputNum(), caffeMobile.getOutputHeight()));
        } catch (Exception e) {
            Log.e("CNN Run", e.getMessage());
            e.printStackTrace();
        }
        // if (testCategories != null) {
        //     String testLog = new File(model_files.get(2)).getName();
        //     for (int category: testCategories) {
        //         testLog += "," + String.valueOf(category);
        //     }
        //     Log.d("CNN Test when initing", testLog);
        // }
        return result;
    }
}
