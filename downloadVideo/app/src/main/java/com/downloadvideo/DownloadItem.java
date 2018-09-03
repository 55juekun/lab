package com.downloadvideo;


import android.widget.ProgressBar;

public class DownloadItem {
    private int cameraID;
    private String downloadUrl;
    private String downloadstatus;
    private String title;
    private int imgId=R.mipmap.download;
    private boolean downloading=false;
    private ProgressBar progressBar;
    private Object task;

    public DownloadItem(int cameraID, String downloadUrl) {
        this.cameraID = cameraID;
        this.downloadUrl = downloadUrl;
    }

    public int getCameraID() {
        return cameraID;
    }
    public void setCameraID(int cameraID) {
        this.cameraID = cameraID;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getDownloadstatus() {
        return downloadstatus;
    }

    public void setDownloadstatus(String downloadstatus) {
        this.downloadstatus = downloadstatus;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }

    public boolean isDownloading() {
        return downloading;
    }

    public void setDownloading(boolean downloading) {
        this.downloading = downloading;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public Object getTask() {
        return task;
    }

    public void setTask(Object task) {
        this.task = task;
    }
}
