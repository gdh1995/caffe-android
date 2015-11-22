package com.caffe.android;

import android.util.Log;

@SuppressWarnings("JniMissingFunction")
public class CaffeMobile {
    public native void enableLog(boolean enabled);

    public boolean hasInit() { return m_inited; }

    // not thread-safe
    public int loadModel(String modelPath, String weightsPath) {
        if (this.hasInit()) {
            Log.e("Caffe Mobile", "init more than one time");
            return 0;
        }
        int ret = loadModelOnce(modelPath, weightsPath);
        m_inited = true;
        return ret;
    }

    public native int setImages(String imgPaths);

    public native float[] predict();

    public native int getOutputHeight();

    public native int getOutputNum();

    public native double getTestTime();

    public native int[] predictTopK(String imgPath, int K);

    public static CaffeMobile Get() {
        if (m_singleton == null) {
            m_singleton = new CaffeMobile();
        }
        return m_singleton;
    }

    private boolean m_inited = false;

    protected CaffeMobile() {}

    protected static CaffeMobile m_singleton;

    protected native int loadModelOnce(String modelPath, String weightsPath);

    static {
        System.loadLibrary("caffe");
        System.loadLibrary("caffe_jni");
    }
}