package com.sh1r0.caffe_android_demo;

@SuppressWarnings("JniMissingFunction")
public class CaffeMobile {
    public native void enableLog(boolean enabled);
    public native int loadModel(String modelPath, String weightsPath);
    public native int predictImage(String imgPath);

    public static CaffeMobile Get() {
        if (m_singleton == null) {
            m_singleton = new CaffeMobile();
        }
        return m_singleton;
    }
    
    protected CaffeMobile() {}
    protected static CaffeMobile m_singleton;

    static {
        System.loadLibrary("caffe");
        System.loadLibrary("caffe_jni");
    }
}