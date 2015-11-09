package com.example.myapplication.app;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends Activity {
    private static final int TAKE_PHOTO = 1;
    private static final int READ_LOCAL = 2;
    Button open_camera, local_image;

    // 获取sd卡根目录地址,并创建图片父目录文件对象和文件的对象;
    String file_str = Environment.getExternalStorageDirectory().getPath();
    String file_path = file_str + "/ccamera";
    File mars_file = new File(file_path);
    File file_go = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        open_camera = (Button) findViewById(R.id.my_camera_button);
        local_image = (Button) findViewById(R.id.local_image);


        // 拍照
        open_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            // 验证sd卡是否正确安装：
            // 先创建父目录，如果新创建一个文件的时候，父目录没有存在，那么必须先创建父目录，再新建文件。
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                    && (mars_file.exists() || mars_file.mkdirs())) {
                // 跳转到系统拍照的activity
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                file_go = new File(file_path + "/" + System.currentTimeMillis() + ".jpg");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file_go));
                startActivityForResult(intent, TAKE_PHOTO);
            } else {
                Toast.makeText(MainActivity.this, "请先安装好sd卡", Toast.LENGTH_LONG).show();
            }
            }
        });

        // 选择本地文件
        local_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, READ_LOCAL);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_cancel) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_exit:
                finish();
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    // 拍照/选择结束后处理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 判断请求码和结果码是否正确;
        if (requestCode == TAKE_PHOTO && resultCode == RESULT_OK) {
            Intent intent = new Intent(MainActivity.this, PictureActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("pic_path", file_go.getPath());
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (requestCode == READ_LOCAL) {
            Cursor cursor = null;
            try {
                Uri uri = data.getData();
                String[] pojo = {MediaStore.Images.Media.DATA};
                cursor = getContentResolver().query(uri, pojo, null, null, null);
                if (cursor != null) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String path = cursor.getString(column_index);
                    if (path.endsWith(".jpg") || path.endsWith(".png") || path.endsWith(".bmp")) {
                        Intent intent = new Intent(MainActivity.this, PictureActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("pic_path", path);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "图片格式有误", Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception ignored) {
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
