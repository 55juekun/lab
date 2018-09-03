package com.downloadvideo;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.LinkedList;

public class DownloadAdapter extends BaseAdapter {
    private LinkedList<DownloadItem> mData;
    private Context context;
    ViewHolder holder=null;
    private TextView cameraId;


    public DownloadAdapter(LinkedList<DownloadItem> mData, Context context) {
        this.mData = mData;
        this.context = context;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private Handler handler = new UIHander();

    private final class UIHander extends Handler{
        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        public void handleMessage(Message msg) {
            if (msg.obj!=null){
                mData.get(msg.what).setTitle((String) msg.obj);
            }
            ProgressBar progressbar=mData.get(msg.what).getProgressBar();
            int size = msg.getData().getInt("size");     //从消息中获取已经下载的数据长度
            progressbar.setProgress(size);         //设置进度条的进度
            //计算已经下载的百分比,此处需要转换为浮点数计算
            double totalSize =(double)progressbar.getMax()/(double) (1024*1024);
            double currentSize=(double)progressbar.getProgress()/(double) (1024*1024);

            mData.get(msg.what).setDownloadstatus(String.format("%.2f",currentSize)+"MB/"+String.format("%.2f",totalSize)+"MB");   //把下载显示到界面控件上
            if(progressbar.getProgress() == progressbar.getMax()){ //下载完成时提示
                Toast.makeText(context, "文件"+(msg.what+1)+"下载成功", Toast.LENGTH_LONG).show();
                mData.get(msg.what).setImgId(R.mipmap.download);
                mData.remove(msg.what);
            }
            notifyDataSetChanged();
        }
    }
    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.download_item, parent, false);
            holder=new ViewHolder();
            holder.imageButton = convertView.findViewById(R.id.ib_downloadstatus);
            cameraId = convertView.findViewById(R.id.tv_camera_id);
            holder.textResult = convertView.findViewById(R.id.tv_downloadstatus);
            holder.title = convertView.findViewById(R.id.tv_title);
            mData.get(position).setProgressBar((ProgressBar) convertView.findViewById(R.id.pb_progress));
            convertView.setTag(holder);
        }else {
            holder= (ViewHolder) convertView.getTag();
        }
        holder.title.setText(mData.get(position).getTitle());
        holder.imageButton.setBackgroundResource(mData.get(position).getImgId());
        cameraId.setText(mData.get(position).getCameraID() + "");
        holder.textResult.setText(mData.get(position).getDownloadstatus());
        holder.imageButton.setTag(position);
        ButtonClickListener listener = new ButtonClickListener();
        holder.imageButton.setOnClickListener(listener);
        return convertView;
    }
    private class ViewHolder{
        TextView textResult;
        TextView title;
        ImageButton imageButton;
    }
    private class ButtonClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            int position= (int) v.getTag();
            if (!mData.get(position).isDownloading()) {
                mData.get(position).setImgId(R.mipmap.pause);
                mData.get(position).setDownloading(true);
                String url=mData.get(position).getDownloadUrl();
                File saveDir = new File("/storage/emulated/0/1111111") ;
                DownloadTask task= new DownloadTask(url, saveDir,position);
                new Thread(task).start();
                mData.get(position).setTask(task);
            }else {
                mData.get(position).setImgId(R.mipmap.download);
                mData.get(position).setDownloading(false);
                DownloadTask task= (DownloadTask) mData.get(position).getTask();
                task.exit();
            }
            notifyDataSetChanged();

        }

        private final class DownloadTask implements Runnable{
            private String path;
            private File saveDir;
            private FileDownloadered loader;
            private int position;
            public DownloadTask(String path, File saveDir,int position) {
                this.path = path;
                this.saveDir = saveDir;
                this.position=position;
            }
            /**
             * 退出下载
             */
            public void exit(){
                if(loader!=null) loader.exit();
            }

            public void run() {
                try {
                    loader = new FileDownloadered(context, path, saveDir, 10);
                    Message message=new Message();
                    message.what=position;
                    mData.get(position).getProgressBar().setMax(loader.getFileSize());//设置进度条的最大刻度
                    message.obj=loader.getFileName();
                    handler.sendMessage(message);
                    loader.download(new DownloadProgressListener() {
                        public void onDownloadSize(int size) {
                            Message msg=new Message();
                            msg.getData().putInt("size",size);
                            msg.what=position;
                            handler.sendMessage(msg);
                        }
                    });
                } catch (Exception e) {
                    Log.d("55juekun", "onDownloadSize: 下载失败" );
                    e.printStackTrace();
                }
            }
        }
    }
}
