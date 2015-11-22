package com.example.myapplication.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

/**
 * AssetCopyer类
 * 实现将assets下的文件按目录结构拷贝到sdcard中
 * modified; from http://blog.csdn.net/gf771115/article/details/29868587
 *
 * @author ticktick
 * @Email lujun.hust@gmail.com
 */
public class AssetCopyer {
    public static final String DEFAULT_ASSET_LIST = "assets.lst";
    public static final int DEFAULT_BUFFER_LENGTH = 128 * 1024;

    public String asset_list;
    public int buffer_length;
    public File mAppDirectory;

    private final Context mContext;
    private final AssetManager mAssetManager;

    public AssetCopyer(Context context) {
        mContext = context;
        mAssetManager = context.getAssets();
        init(null, null, 0);
    }

    public void init(String list_name, File targetDir, int buffer_size) {
        asset_list = list_name != null ? list_name : DEFAULT_ASSET_LIST;
        buffer_length = buffer_size > 0 ? buffer_size : DEFAULT_BUFFER_LENGTH;

        if (null == targetDir) {
            //获取系统在SDCard中为app分配的目录，eg:/sdcard/Android/data/$(app's package)
            //该目录存放app相关的各种文件(如cache，配置文件等)，unstall app后该目录也会随之删除
            targetDir = mContext.getExternalFilesDir(null);
        }
        mAppDirectory = targetDir;
    }

    public List<String> ensure() {
        return ensure(null);
    }

    /**
     * 将assets目录下指定的文件拷贝到sdcard中
     *
     * @return 目标文件的绝对路径列表, List 成功；null 失败
     */
    public List<String> ensure(List<String> assets) {
        if (assets == null) {
            //读取assets/目录下的$(asset_list)文件，得到需要copy的文件列表
            assets = getAssetsList();
            if (assets == null){
                return null;
            }
        }

        List<String> destFiles = new ArrayList<String>(assets.size());
        String todo = "";
        try {
            for (String asset : assets) {
                todo = asset;
                boolean force = asset.charAt(0) == '!';
                asset = force ? asset.substring(1) : asset;
                File destFile = new File(mAppDirectory, asset);
                if (!force && destFile.exists()) {
                    destFiles.add(destFile.getCanonicalPath());
                    continue;
                }
                //依次拷贝到$(mAppDirectory)/的目录下
                Log.d("Assets", "to be copied: " + asset);
                destFile = copyFile(asset);
                destFiles.add(destFile.getCanonicalPath());
            }
        } catch (IOException e) {
            Log.e("Assets", "fail to copy: " + todo);
            e.printStackTrace();
            return null;
        }
        
        return destFiles;
    }

    /**
     * 获取需要拷贝的文件列表（记录在~~/assets.lst文件中的相对路径会被转为绝对路径）
     *
     * @return 文件路径列表
     */
    public List<String> getAssetsList() {
        ArrayList<String> files = new ArrayList<String>();

        File file = new File(asset_list);
        InputStream ListStream;
        try {
            ListStream = mAssetManager.open(file.getPath());
            String parent = file.getParent();
            BufferedReader br = new BufferedReader(new InputStreamReader(ListStream));
            String path;
            while (null != (path = br.readLine())) {
                if (!path.isEmpty()) {
                    boolean force = path.charAt(0) == '!';
                    path = new File(parent, force ? path.substring(1) : path).getPath();
                    files.add(force ? ('!' + path) : path);
                }
            }
            br.close();
            ListStream.close();
        } catch (IOException e) {
            Log.e("Assets", "fail to get list: " + asset_list);
            e.printStackTrace();
        }

        return files;
    }

    /**
     * 执行拷贝任务
     *
     * @param asset 需要拷贝的asset文件路径
     * @return 拷贝成功后的目标文件句柄
     * @throws IOException
     */
    public File copyFile(String asset) throws IOException {
        InputStream source = mAssetManager.open(new File(asset).getPath());
        File destinationFile = new File(mAppDirectory, asset);
        destinationFile.getParentFile().mkdirs();
        OutputStream destination = new FileOutputStream(destinationFile);
        byte[] buffer = new byte[buffer_length];
        int nread;

        while ((nread = source.read(buffer)) != -1) {
            if (nread == 0) {
                nread = source.read();
                if (nread < 0)
                    break;
                destination.write(nread);
                continue;
            }
            destination.write(buffer, 0, nread);
        }
        destination.close();

        return destinationFile;
    }
}