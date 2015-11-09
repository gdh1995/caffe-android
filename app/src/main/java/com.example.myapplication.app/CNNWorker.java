package com.example.myapplication.app;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * @author gdh1995
 */
public class CNNWorker implements Runnable {
    private File file;
    private Bitmap bitmap;
    private Handler handler;

    public CNNWorker(Handler handler, Bitmap bitmap, File file) {
        this.handler = handler;
        this.bitmap = bitmap;
        this.file = file;
    }

    public void run() {
        String result = uploadFile(bitmap, file);
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("result", result);
        msg.setData(data);
        handler.sendMessage(msg);
    }

    /**
     * android上传文件到服务器
     *
     * @param file 需要上传的文件
     * @return 返回响应的内容
     */
    public static String uploadFile(Bitmap bitmap, File file) {
        String result = null;
        String BOUNDARY = UUID.randomUUID().toString();  //边界标识   随机生成
        String PREFIX = "--", LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data";   //内容类型
        String RequestURL = "";

        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1000);
            conn.setDoInput(true);  //允许输入流
            conn.setDoOutput(true); //允许输出流
            conn.setUseCaches(false);  //不允许使用缓存
            conn.setRequestMethod("POST");  //请求方式
            conn.setRequestProperty("Charset", "utf-8");  //设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            conn.connect();
            if (file != null) {
                /**
                 * 当文件不为空，把文件包装并且上传
                 */
                StringBuffer strBuffer = new StringBuffer();
                strBuffer.append(PREFIX);
                strBuffer.append(BOUNDARY);
                strBuffer.append(LINE_END);
                /**
                 * 这里重点注意：
                 * name里面的值为服务器端需要key   只有这个key 才可以得到对应的文件
                 * filename是文件的名字，包含后缀名的   比如:abc.png
                 */
                strBuffer.append("Content-Disposition: form-data; name=\"image\"; filename=\"" + file.getName() + "\"" + LINE_END);
                strBuffer.append("Content-Type: image/pjpeg; charset=utf-8" + LINE_END);
                strBuffer.append(LINE_END);

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(strBuffer.toString().getBytes());
                /*InputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                int len = 0;
                while((len=is.read(bytes))!=-1)
                {
                    dos.write(bytes, 0, len);
                }
                is.close();*/
                final int maxSize = 500;
                int outWidth;
                int outHeight;
                int inWidth = bitmap.getWidth();
                int inHeight = bitmap.getHeight();
                if (inWidth > inHeight) {
                    outWidth = maxSize;
                    outHeight = (inHeight * maxSize) / inWidth;
                } else {
                    outHeight = maxSize;
                    outWidth = (inWidth * maxSize) / inHeight;
                }
                Bitmap bmpCompressed = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, true);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                // CompressFormat set up to JPG, you can change to PNG or whatever you want;
                bmpCompressed.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                byte[] data = bos.toByteArray();
                dos.write(data);

                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                dos.write(end_data);
                dos.flush();
                /**
                 * 获取响应码  200=成功
                 * 当响应成功，获取响应的流
                 */
                int res = conn.getResponseCode();
                if (res == 200) {
                    InputStream input = conn.getInputStream();
                    StringBuffer strBuf = new StringBuffer();
                    int ss;
                    while ((ss = input.read()) != -1) {
                        strBuf.append((char) ss);
                    }
                    result = strBuf.toString();
                } else if (res == 400) {
                    result = "错误400";
                } else {
                    result = "服务器没有返回数据";
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            result = "上传失败" + e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            result = "上传失败" + e.getMessage();
        }
        return result;
    }
}
