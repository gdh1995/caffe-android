package com.example.myapplication.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.lang.ref.WeakReference;

public class PictureActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        ImageView img = (ImageView) findViewById(R.id.result_pic);
        Bundle bundle = this.getIntent().getExtras();
        String pic_path = bundle.getString("pic_path");
        File file_pic = new File(pic_path);

        /* 创建一个BitmapFactory.Options类用来显示bitmap */
        BitmapFactory.Options my_options = new BitmapFactory.Options();
        my_options.inJustDecodeBounds = false;
        my_options.inPurgeable = true;
        my_options.inInputShareable = true;
        my_options.inPreferredConfig = Bitmap.Config.ARGB_4444;
        Bitmap bitmap = BitmapFactory.decodeFile(file_pic.getAbsolutePath(), my_options);
        img.setImageBitmap(bitmap);

        CNNWorker workThread = new CNNWorker(new MyHandler(this), file_pic);
        new Thread(workThread).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.picture, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_exit) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_cancel:
                PictureActivity.this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public static class MyHandler extends Handler {
        WeakReference<PictureActivity> mActivity;
        public String lastResult;

        MyHandler(PictureActivity activity) {
            mActivity = new WeakReference<PictureActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            int[] result = data.getIntArray("result");
            String description = "没有任何识别结果";

            if (result != null && result.length > 0) {
                String str = "";
                for (int i = 0; i < result.length; i++) {
                    str += String.valueOf(result[i]);
                }
                description = "输出为: " + str;
            }

            if (mActivity.get() != null) {
                TextView desView = (TextView) mActivity.get().findViewById(R.id.description);
                desView.setText(description);
            }
            lastResult = description;
            Log.i("CNN Result", description);
        }
    }
}
