package com.downloadvideo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.LinkedList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private List<DownloadItem> mData = null;
    private Context mContext;
    private DownloadAdapter mAdapter = null;
    private ListView listDownload;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isGrant(MainActivity.this);
        mContext=MainActivity.this;
        listDownload=findViewById(R.id.lv_download);
        mData=new LinkedList<>();
        mData.add(new DownloadItem(123,"https://as-5video.oss-cn-beijing.aliyuncs.com/record/as/5399010/2018-08-27-16-40-04_2018-08-27-16-41-37.mp4"));
        mData.add(new DownloadItem(124,"http://soft.duote.org/haozip_5.9.7.10871.exe"));
        mData.add(new DownloadItem(4545,"https://as-5video.oss-cn-beijing.aliyuncs.com/record/as/123/2018-08-17-15-27-43_2018-08-17-15-28-06.mp4"));
        mAdapter=new DownloadAdapter((LinkedList<DownloadItem>) mData,mContext);
        listDownload.setAdapter(mAdapter);
    }


    public static boolean isGrant(Activity activity) {//获取相关手机权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission( Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS }, 1);
            return false; }
        return true;
    }

}
