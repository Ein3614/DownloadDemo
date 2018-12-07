package com.whut.ein3614.downloaddemo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import java.io.File;

public class DownloadService extends Service {
    private DownloadTask downloadTask;
    private String downloadUrl;
    private DownloadListener downloadListener = new DownloadListener() {
        /**
         * 发送一个用于显示下载进度的通知
         * */
        @Override
        public void onProgress(int progress) {
            Util.getNotificationManager(DownloadService.this)
                    .notify(1,Util.getNotification(DownloadService.this,MainActivity.class,"download","Downloading...",progress));
        }

        /**
         * 关闭正在下载的前台通知，并发送下载完成的通知
         * */
        @Override
        public void onSuccess() {
            downloadTask = null;
            //关闭前台服务通知
            stopForeground(true);
            Util.getNotificationManager(DownloadService.this)
                    .notify(1,Util.getNotification(DownloadService.this,MainActivity.class,"download","Download Success",-1));
        }
        /**
         * 关闭正在下载的前台通知，并发送下载失败的通知
         * */
        @Override
        public void onFailed() {
            downloadTask = null;
            stopForeground(true);
            Util.getNotificationManager(DownloadService.this)
                    .notify(1,Util.getNotification(DownloadService.this,MainActivity.class,"download","Download Failed",-1));
        }

        @Override
        public void onPaused() {
            downloadTask = null;
        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
            //删除文件
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            File file = new File(directory+fileName);
            if(file.exists())
                file.delete();
        }
    };
    private DownloadBinder mBinder = new DownloadBinder();
    public DownloadService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    class DownloadBinder extends Binder{
        /**
         * 获取Service实例
         * */
        public DownloadService getService(){
            return DownloadService.this;
        }
    }
    /**
     * 开始下载
     * */
    public void startDownload(String url){
        //当前没有下载任务时才能开始下载
        if(downloadTask == null){
            downloadUrl = url;
            downloadTask = new DownloadTask(downloadListener);
            downloadTask.execute(downloadUrl);
            //使用前台服务
            startForeground(1,Util.getNotification(this,MainActivity.class,"download","Downloading...",0));
        }
    }
    /**
     * 暂停下载
     * */
    public void pauseDownload(){
        if(downloadTask != null){
            downloadTask.pauseDownload();
        }
    }
    /**
     * 取消下载
     * */
    public void cancelDownload(){
        if(downloadTask != null){
            downloadTask.cancelDownload();
        }
    }
}
