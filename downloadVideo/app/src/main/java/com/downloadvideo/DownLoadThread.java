package com.downloadvideo;

import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by 55珏坤 on 2018/5/23.
 */

public class DownLoadThread extends Thread {
    private static final String TAG = "下载线程类";    //定义TAG,在打印log时进行标记
    private File saveFile;              //下载的数据保存到的文件
    private URL downUrl;              //下载的URL
    private int block;                //每条线程下载的大小
    private int threadId = -1;            //初始化线程id设置
    private int downLength;             //该线程已下载的数据长度
    private boolean finish = false;         //该线程是否完成下载的标志
    private FileDownloadered downloader;      //文件下载器
    private int fileSize;

    public DownLoadThread(FileDownloadered downloader, URL downUrl, File saveFile, int block, int downLength, int threadId,int fileSize) {
        this.downUrl = downUrl;
        this.saveFile = saveFile;
        this.block = block;
        this.downloader = downloader;
        this.threadId = threadId;
        this.downLength = downLength;
        this.fileSize=fileSize;
    }

    @Override
    public void run() {
        if (downLength < block) {//未下载完成
            try {
                HttpURLConnection http = (HttpURLConnection) downUrl.openConnection();
                http.setConnectTimeout(5 * 1000);
                http.setRequestMethod("GET");
                http.setRequestProperty("Accept", " */*");
                http.setRequestProperty("Accept-Language", "zh-CN");
                http.setRequestProperty("Accept-Encoding","identity");
                String refer=downUrl.toString().substring(0,downUrl.toString().lastIndexOf('/'));
                http.setRequestProperty("Referer",refer);//可以以自身为refer吗？
                http.setRequestProperty("Charset", "UTF-8");
                int startPos = block * (threadId - 1) + downLength;//开始位置
                int endPos = block * threadId - 1;//结束位置
                http.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);//设置获取实体数据的范围
                if (endPos>=fileSize){
                    http.setRequestProperty("Range", "bytes=" + startPos + "-");//设置获取实体数据的范围
                }
                Log.e(TAG, threadId+"   bytes=" + startPos + "-" + endPos);//打印该线程下载的范围
                http.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36" +
                        " (KHTML, like Gecko) Chrome/66.0.3359.139 Safari/537.36");
                http.setRequestProperty("Connection", "Keep-Alive");
                InputStream inStream = http.getInputStream();     //获得远程连接的输入流
                byte[] buffer = new byte[1024*4];           //设置本地数据的缓存大小为1MB
                int offset = 0;                   //每次读取的数据量
                RandomAccessFile threadfile = new RandomAccessFile(this.saveFile, "rwd");
                threadfile.seek(startPos);
                //用户没有要求停止下载,同时没有达到请求数据的末尾时会一直循环读取数据
                long time=System.currentTimeMillis(); int totaloffset=0;
                while (!downloader.getExited() && (offset = inStream.read(buffer, 0, 1024*4)) != -1) {
                    threadfile.write(buffer, 0, offset);          //直接把数据写入到文件中
                    downLength += offset;             //把新线程已经写到文件中的数据加入到下载长度中
                    totaloffset+=offset;
                    long nowtime=System.currentTimeMillis();
                    if (nowtime-time>1000) {
                        downloader.update(this.threadId, downLength); //把该线程已经下载的数据长度更新到数据库和内存哈希表中
                        downloader.append(totaloffset);            //把新下载的数据长度加入到已经下载的数据总长度中
                        totaloffset=0;
                        time=nowtime;
                    }
                }
                if (threadId==9||threadId==10)
                    Log.e(TAG, "run: "+threadId+"        "+downLength );
                downloader.update(this.threadId, downLength); //把该线程已经下载的数据长度更新到数据库和内存哈希表中
                downloader.append(totaloffset);
                threadfile.close();
                inStream.close();
                this.finish = true;                               //设置完成标记为true,无论下载完成还是用户主动中断下载
                print("Thread " + this.threadId + " download finish   ");
            } catch (Exception e) {
                this.downLength = -1;               //设置该线程已经下载的长度为-1
                Log.e(TAG, threadId+ "   ERROR" );
                print("Thread " + this.threadId + ":" + e);
            }
        }
    }

    private static void print(String msg) {
        Log.i(TAG, msg);
    }

    /**
     * 下载是否完成
     *
     * @return
     */
    public boolean isFinish() {
        return finish;
    }

    /**
     * 已经下载的内容大小
     *
     * @return 如果返回值为-1,代表下载失败
     */
    public long getDownLength() {
        return downLength;
    }
}
