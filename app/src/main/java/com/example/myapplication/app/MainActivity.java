package com.example.myapplication.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.hardware.Camera;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.EditText;
import android.content.Intent;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */

    private static final int TAKE_PICTURE = 1;
    private static final int LOCAL_UPLOAD = 2;
    Button open_camera,upload_image;

    // 获取sd卡根目录地址,并创建图片父目录文件对象和文件的对象;
    String file_str = Environment.getExternalStorageDirectory().getPath();
    File mars_file = new File(file_str + "/ccamera");
    File file_go = new File(file_str + "/ccamera/file.jpg");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        open_camera = (Button) findViewById(R.id.my_camera_button);
        upload_image=(Button)findViewById(R.id.upload_image);


        //拍照
        open_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                // 验证sd卡是否正确安装：
                if (Environment.MEDIA_MOUNTED.equals(Environment
                        .getExternalStorageState())) {
                    // 先创建父目录，如果新创建一个文件的时候，父目录没有存在，那么必须先创建父目录，再新建文件。
                    if (!mars_file.exists()) {
                        mars_file.mkdirs();
                    }

                    // 设置跳转的系统拍照的activity为：MediaStore.ACTION_IMAGE_CAPTURE ;
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file_go));
                    //跳转到拍照界面;
                    startActivityForResult(intent, TAKE_PICTURE);
                } else {
                    Toast.makeText(MainActivity.this, "请先安装好sd卡",Toast.LENGTH_LONG).show();
                }
            }
        });

        //上传
        upload_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, LOCAL_UPLOAD);
            }
        });

    }

    //拍照结束后显示图片;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        // 判断请求码和结果码是否正确，如果正确的话就在uploadActivity上显示刚刚所拍照的图片;
        if (requestCode == TAKE_PICTURE && resultCode == this.RESULT_OK) {
            Intent intent = new Intent(MainActivity.this,UploadActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("pic_path", file_go.getPath());
            intent.putExtras(bundle);
            startActivity(intent);
        } else if(requestCode == LOCAL_UPLOAD){
            try
            {
                Uri uri = data.getData();
                String[] pojo = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(uri, pojo, null, null,null);
                //Cursor cursor = managedQuery(uri, pojo, null, null,null);
                if(cursor != null)
                {
                    int colunm_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String path = cursor.getString(colunm_index);
                    if(path.endsWith("jpg")||path.endsWith("png"))
                    {
                        Intent intent = new Intent(MainActivity.this,UploadActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("pic_path", path);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "图片格式有误",Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "打开图片出错"+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
