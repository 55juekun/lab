package com.downloadvideo;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 55珏坤 on 2018/5/23.
 */

public class FileDownloadered {
    private static final String TAG = "文件下载类";  //设置一个查log时的一个标志
    private FileService fileService;        //获取本地数据库的业务Bean
    private boolean exited;             //停止下载的标志
    private Context context;            //程序的上下文对象
    private int downloadedSize = 0;               //已下载的文件长度
    private int fileSize = 0;           //开始的文件长度
    private DownLoadThread[] threads;        //根据线程数设置下载的线程池
    private File saveFile;              //数据保存到本地的文件中
    private Map<Integer, Integer> data = new ConcurrentHashMap<>();  //缓存个条线程的下载的长度
    private int block;                            //每条线程下载的长度
    private String downloadUrl;                   //下载的路径

    /**
     * 获取线程数
     */
    public int getThreadSize()
    {
        return threads.length;
    }

    /**
     * 退出下载
     * */
    public void exit()
    {
        this.exited = true;    //将退出的标志设置为true;
    }

    public boolean getExited()
    {
        return this.exited;
    }

    /**
     * 获取文件的大小
     * */
    public int getFileSize()
    {
        return fileSize;
    }

    public String getFileName(){
        return this.saveFile.getName();
    }

    /**
     * 累计已下载的大小
     * 使用同步锁来解决并发的访问问题
     * */
    protected synchronized void append(int size)
    {
        //把实时下载的长度加入到总的下载长度中
        downloadedSize += size;
    }

    /**
     * 更新指定线程最后下载的位置
     * @param threadId 线程id
     * @param pos 最后下载的位置
     * */
    protected synchronized void update(int threadId,int pos)
    {
        //把指定线程id的线程赋予最新的下载长度,以前的值会被覆盖掉
        this.data.put(threadId, pos);
        //更新数据库中制定线程的下载长度
        this.fileService.update(this.downloadUrl, threadId, pos);
    }

    public FileDownloadered(Context context,String downloadUrl,File fileSavedDir,int threadNum){
        try{
            this.context=context;
            this.downloadUrl=downloadUrl;
            fileService=new FileService(context);
            URL url=new URL(this.downloadUrl);
            if (!fileSavedDir.exists()) fileSavedDir.mkdirs();
            this.threads=new DownLoadThread[threadNum];
            HttpURLConnection httpURLConnection= (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Accept", " */*");
            httpURLConnection.setRequestProperty("Accept-Language", "zh-CN");  //设置用户语言
            httpURLConnection.setRequestProperty("Accept-Encoding", "identity");
            String refer=downloadUrl.substring(0,downloadUrl.lastIndexOf('/'));
            httpURLConnection.setRequestProperty("Referer",refer);    //设置请求的来源页面,便于服务端进行来源统计
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36" +
                    " (KHTML, like Gecko) Chrome/66.0.3359.139 Safari/537.36");
            httpURLConnection.setRequestProperty("Connection","Keep-Alive");
            httpURLConnection.connect();
            printResponseHeader(httpURLConnection);
            if (httpURLConnection.getResponseCode()==200||httpURLConnection.getResponseCode()==206){

                this.fileSize=httpURLConnection.getContentLength();
                if (this.fileSize<=0)throw new RuntimeException("不知道文件大小");
                String filename=getFileName(httpURLConnection);
                this.saveFile=new File(fileSavedDir,filename);
                Map<Integer,Integer>logdata=fileService.getData(downloadUrl);
                if (logdata.size()>0){
                    for (Map.Entry<Integer, Integer> entry : logdata.entrySet()) {
                        data.put(entry.getKey(),entry.getValue());
                    }
                }
                if (this.data.size()==this.threads.length){
                    for (int i = 0; i <this.threads.length ; i++) {
                        this.downloadedSize+=this.data.get(i+1);
                    }
                    print("已下载的长度" + this.downloadedSize + "个字节");
                }
                this.block = (this.fileSize % this.threads.length) == 0?
                        this.fileSize / this.threads.length:
                        this.fileSize / this.threads.length + 1;
            }else{
                //打印错误信息
                print("服务器响应错误:" + httpURLConnection.getResponseCode() + httpURLConnection.getResponseMessage());
                throw new RuntimeException("服务器反馈出错");
            }
        } catch (Exception e) {
            print(e.toString());   //打印错误
            throw new RuntimeException("无法连接URL");
        }
    }

    private String getFileName(HttpURLConnection conn)
    {
        //从下载的路径的字符串中获取文件的名称
        String filename = this.downloadUrl.substring(this.downloadUrl.lastIndexOf('/') + 1);
        if(filename == null || "".equals(filename.trim())){     //如果获取不到文件名称
            for(int i = 0;;i++)  //使用无限循环遍历
            {
                String mine = conn.getHeaderField(i);     //从返回的流中获取特定索引的头字段的值
                if (mine == null) break;          //如果遍历到了返回头末尾则退出循环
                //获取content-disposition返回字段,里面可能包含文件名
                if("content-disposition".equals(conn.getHeaderFieldKey(i).toLowerCase())){
                    //使用正则表达式查询文件名
                    Matcher m = Pattern.compile(".*filename=(.*)").matcher(mine.toLowerCase());
                    Log.e(TAG, "getFileName: "+m.group(1));
                    if(m.find()) return m.group(1);    //如果有符合正则表达式规则的字符串,返回
                }
            }
            filename = UUID.randomUUID()+ ".tmp";//如果都没找到的话,默认取一个文件名
            //由网卡标识数字(每个网卡都有唯一的标识号)以及CPU时间的唯一数字生成的一个16字节的二进制作为文件名
        }
        return filename;
    }

    public int download(DownloadProgressListener listener) throws Exception{
        try {
            RandomAccessFile randOut = new RandomAccessFile(this.saveFile, "rwd");
            //设置文件大小
            if(this.fileSize>0) randOut.setLength(this.fileSize);
            randOut.close();    //关闭该文件,使设置生效
            URL url = new URL(this.downloadUrl);
            if(this.data.size() != this.threads.length){
                print("未下载过");
                //如果原先未曾下载或者原先的下载线程数与现在的线程数不一致
                this.data.clear();
                //遍历线程池
                for (int i = 0; i < this.threads.length; i++) {
                    this.data.put(i+1, 0);//初始化每条线程已经下载的数据长度为0
                }
                this.downloadedSize = 0;   //设置已经下载的长度为0
            }


            for (int i = 0; i < this.threads.length; i++) {//开启线程进行下载
                int downLength = this.data.get(i+1);
                //通过特定的线程id获取该线程已经下载的数据长度
                //判断线程是否已经完成下载,否则继续下载
                if(downLength < this.block && this.downloadedSize<this.fileSize){
                    //初始化特定id的线程
                    this.threads[i] = new DownLoadThread(this, url, this.saveFile, this.block, this.data.get(i+1), i+1,fileSize);
                    //设置线程优先级,Thread.NORM_PRIORITY = 5;
                    //Thread.MIN_PRIORITY = 1;Thread.MAX_PRIORITY = 10,数值越大优先级越高
                    this.threads[i].setPriority(7);
                    this.threads[i].start();    //启动线程
                }else{
                    this.threads[i] = null;   //表明线程已完成下载任务
                }
            }

            //如果存在下载记录，删除它们，然后重新添加
            fileService.save(this.downloadUrl, this.data);
            //把下载的实时数据写入数据库中
            boolean notFinish = true;
            //下载未完成
            while (notFinish) {
                // 循环判断所有线程是否完成下载
                Thread.sleep(1000);
                notFinish = false;
                //假定全部线程下载完成
                for (int i = 0; i < this.threads.length; i++){
                    if (this.threads[i] != null && !this.threads[i].isFinish()) {
                        //如果发现线程未完成下载
                        notFinish = true;
                        //设置标志为下载没有完成
                        if(this.threads[i].getDownLength() == -1){
                            //如果下载失败,再重新在已下载的数据长度的基础上下载
                            //重新开辟下载线程,设置线程的优先级
                            this.threads[i] = new DownLoadThread(this, url, this.saveFile, this.block, this.data.get(i+1), i+1,fileSize);
                            this.threads[i].setPriority(7);
                            this.threads[i].start();
                        }
                    }
                }
                if(listener!=null) listener.onDownloadSize(this.downloadedSize);
                //通知目前已经下载完成的数据长度
            }
            if(downloadedSize == this.fileSize) {
                Log.e(TAG, "download: ok" );
                fileService.delete(this.downloadUrl);
            }

            //下载完成删除记录
        } catch (Exception e) {
            print(e.toString());
            throw new Exception("文件下载异常");
        }
        return this.downloadedSize;
    }

    public static Map<String, String> getHttpResponseHeader(HttpURLConnection http) {
        //使用LinkedHashMap保证写入和遍历的时候的顺序相同,而且允许空值
        Map<String, String> header = new LinkedHashMap<String, String>();
        //此处使用无限循环,因为不知道头字段的数量
        for (int i = 0;; i++) {
            String mine = http.getHeaderField(i);  //获取第i个头字段的值
            if (mine == null) break;      //没值说明头字段已经循环完毕了,使用break跳出循环
            header.put(http.getHeaderFieldKey(i), mine); //获得第i个头字段的键
        }
        return header;
    }

    public static void printResponseHeader(HttpURLConnection http){
        //获取http响应的头字段
        Map<String, String> header = getHttpResponseHeader(http);
        //使用增强for循环遍历取得头字段的值,此时遍历的循环顺序与输入树勋相同
        for(Map.Entry<String, String> entry : header.entrySet()){
            //当有键的时候则获取值,如果没有则为空字符串
            String key = entry.getKey()!=null ? entry.getKey()+ ":" : "";
            print(key+ entry.getValue());      //打印键和值得组合
        }
    }


    private static void print(String msg) {
        Log.i(TAG, msg);
    }
}
