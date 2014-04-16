package com.example.myapplication.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Message;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;


public class UploadActivity extends ActionBarActivity {

    private static final int TIME_OUT = 3000;   //超时时间
    private static final String CHARSET = "utf-8"; //设置编码
    private static final String REQUEST_URL = "http://166.111.80.233:3333/upload";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        ImageView img = (ImageView) findViewById(R.id.result_pic);
        Bundle bundle = this.getIntent().getExtras();
        String pic_path = bundle.getString("pic_path");
        File file_pic = new File(pic_path);

         /*创建一个BitmapFactory.Options类用来处理bitmap；*/
        BitmapFactory.Options myoptions = new BitmapFactory.Options();
        myoptions.inJustDecodeBounds=false;
        myoptions.inPurgeable=true;
        myoptions.inInputShareable=true;
        myoptions.inPreferredConfig=Bitmap.Config.ARGB_4444;
        Bitmap bitmat = BitmapFactory.decodeFile(file_pic.getAbsolutePath(),myoptions);
        bitmat = zoomImage(bitmat,100,100);
        img.setImageBitmap(bitmat);

        UploadThread uThread = new UploadThread(file_pic,REQUEST_URL);
        new Thread(uThread).start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.upload, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * android上传文件到服务器
     * @param file  需要上传的文件
     * @param RequestURL  请求的rul
     * @return  返回响应的内容
     */
    public  String uploadFile(File file,String RequestURL)
    {
        String result = null;
        String  BOUNDARY =  UUID.randomUUID().toString();  //边界标识   随机生成
        String PREFIX = "--" , LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data";   //内容类型

        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true);  //允许输入流
            conn.setDoOutput(true); //允许输出流
            conn.setUseCaches(false);  //不允许使用缓存
            conn.setRequestMethod("POST");  //请求方式
            conn.setRequestProperty("Charset", CHARSET);  //设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            conn.connect();
            if(file!=null)
            {
                /**
                 * 当文件不为空，把文件包装并且上传
                 */
                StringBuffer sb = new StringBuffer();
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);
                /**
                 * 这里重点注意：
                 * name里面的值为服务器端需要key   只有这个key 才可以得到对应的文件
                 * filename是文件的名字，包含后缀名的   比如:abc.png
                 */
                sb.append("Content-Disposition: form-data; name=\"image\"; filename=\""+file.getName()+"\""+LINE_END);
                sb.append("Content-Type: image/pjpeg; charset="+CHARSET+LINE_END);
                sb.append(LINE_END);

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(sb.toString().getBytes());
                InputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                int len = 0;
                while((len=is.read(bytes))!=-1)
                {
                    dos.write(bytes, 0, len);
                }
                is.close();
                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX+BOUNDARY+PREFIX+LINE_END).getBytes();
                dos.write(end_data);
                dos.flush();
                /**
                 * 获取响应码  200=成功
                 * 当响应成功，获取响应的流
                 */
                int res = conn.getResponseCode();
                if(res==200)
                {
                    InputStream input =  conn.getInputStream();
                    StringBuffer  strBuf =  new StringBuffer();
                    int ss ;
                    while((ss=input.read())!=-1)
                    {
                        strBuf.append((char)ss);
                    }
                    result = strBuf.toString();
                }
                else if(res == 400)
                {
                    result =  "错误400";
                }
                else{
                    result =  "服务器没有返回数据";
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

    class UploadThread implements Runnable {
        private File file;
        private String RequestURL;
        public UploadThread(File file, String RequestURL){
            this.file = file;
            this.RequestURL = RequestURL;
        }
        public void run() {
            String result = uploadFile(file, RequestURL);
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("result",result);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            TextView desView = (TextView)findViewById(R.id.description);
            Bundle data = msg.getData();
            String result = data.getString("result");
            String description = "";
            if(result.indexOf("gender") > -1)
            {
                if(result.indexOf("female") > -1)
                {
                    description += "性别：女\n";
                }
                else
                {
                    description += "性别：男\n";
                }
            }
            if(description == "")
            {
                description = "没有识别任何结果";
            }
            desView.setText(description);
        }
    };

    /***
     * 图片的缩放方法
     * @param srcImage ：源图片资源
     * @param newWidth ：缩放后宽度
     * @param newHeight ：缩放后高度
     * @return
     */
    public static Bitmap zoomImage(Bitmap srcImage, double newWidth,
                                   double newHeight) {
        // 获取这个图片的宽和高
        float width = srcImage.getWidth();
        float height = srcImage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(srcImage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }

    static boolean  saveBitmap2file(Bitmap bmp,String filename){
        Bitmap.CompressFormat format= Bitmap.CompressFormat.JPEG;
        int quality = 100;
        OutputStream stream = null;
        try {
            stream = new FileOutputStream("/sdcard/ccamera/" + filename);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bmp.compress(format, quality, stream);
    }
}
