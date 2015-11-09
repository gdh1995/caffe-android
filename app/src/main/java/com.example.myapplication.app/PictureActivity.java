package com.example.myapplication.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Iterator;


public class PictureActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        ImageView img = (ImageView) findViewById(R.id.result_pic);
        Bundle bundle = this.getIntent().getExtras();
        String pic_path = bundle.getString("pic_path");
        File file_pic = new File(pic_path);

         /*创建一个BitmapFactory.Options类用来显示bitmap；*/
        BitmapFactory.Options myoptions = new BitmapFactory.Options();
        myoptions.inJustDecodeBounds=false;
        myoptions.inPurgeable=true;
        myoptions.inInputShareable=true;
        myoptions.inPreferredConfig=Bitmap.Config.ARGB_4444;
        Bitmap bitmat = BitmapFactory.decodeFile(file_pic.getAbsolutePath(),myoptions);
        img.setImageBitmap(bitmat);

        CNNWorker uThread = new CNNWorker(new MyHandler(this), bitmat, file_pic);
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
        if (id == R.id.action_exit) {
            return true;
        }
        switch(item.getItemId())
        {
            case R.id.action_cancel:
                PictureActivity.this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public static class MyHandler extends Handler {
        WeakReference<PictureActivity> mActivity;

        MyHandler(PictureActivity activity) {
            mActivity = new WeakReference<PictureActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String result = data.getString("result");

            JSONObject jsonObj;
            String description = "";
            try{
                jsonObj  = new JSONObject(result);
                Iterator<?> keys = jsonObj.keys();

                while(keys.hasNext()){
                    String key = (String)keys.next();
                    try{
                        String value = (String)jsonObj.get(key);
                        description += key + "： "+ value +"\n";
                    }catch(Exception ignored){
                    }
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            if(description.equals(""))
            {
                description = "没有识别任何结果";
            }
            TextView desView = (TextView)mActivity.get().findViewById(R.id.description);
            desView.setText(description);
        }
    }
}
