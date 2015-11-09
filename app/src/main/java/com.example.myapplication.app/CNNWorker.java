package com.example.myapplication.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.IOException;

/**
 * @author gdh1995
 */
public class CNNWorker implements Runnable {
    private Handler handler;
    private File file;

    public CNNWorker(Handler handler, File file) {
        this.handler = handler;
        this.file = file;
    }

    public void run() {
        int [] result = work();
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
    public static int[] work() {
        int [] result = null;
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
