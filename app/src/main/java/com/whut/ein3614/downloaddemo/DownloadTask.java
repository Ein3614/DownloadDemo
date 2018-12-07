package com.whut.ein3614.downloaddemo;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 类描述：下载功能类
 * 第一个泛型 String 表示在执行AsyncTask的时候需要传入一个字符串参数给后台任务
 * 第二个泛型 Integer 表示用整型数据来作为进度显示单位
 * 第三个泛型 Integer 表示用整型数据来反馈执行结果
 * 创建人：Created by Administrator on 2018/8/21.
 * 修改人：
 * 修改时间：
 */
public class DownloadTask extends AsyncTask<String,Integer,Integer> {
    //四个整型常量表示下载状态：成功，失败，暂停，取消
    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSED = 2;
    public static final int TYPE_CANCLED = 3;

    //监听下载状态的监听器
    private DownloadListener downloadListener;
    private boolean isCanceled = false;
    private boolean isPaused = false;
    private int lastProgress = 0;

    public DownloadTask(DownloadListener downloadListener){
        this.downloadListener = downloadListener;
    }

    /**
     * 后台执行下载任务
     * */
    @Override
    protected Integer doInBackground(String... strings) {
        RandomAccessFile savedFile = null;
        InputStream is = null;
        File file = null;
        //记录已下载的文件长度
        long downloadedFileLength = 0;
        //需要下载的文件总长度
        long contentLength = 0;
        try {
            //获取下载的URL地址
            String downloadUrl = strings[0];
            //获取下载文件名(例如：/example)
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            //获取SD卡Download路径
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            //下载文件路径
            file = new File(directory+fileName);
            //判断需要下载的文件是否已经存在
            if(file.exists()){
                downloadedFileLength = file.length();
            }
            //获取需要下载的文件总长度
            contentLength = getContentLength(downloadUrl);
            if(contentLength == 0){
                return TYPE_FAILED;
            } else if (contentLength == downloadedFileLength){
                return TYPE_SUCCESS;
            }
            //请求网络下载文件
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    //断点下载，指定从哪个字节开始下载
                    .addHeader("RANGE","bytes="+downloadedFileLength+"-")
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if(response!=null){
                is = response.body().byteStream();
                savedFile = new RandomAccessFile(file,"rw");
                //跳过已经下载的字节
                savedFile.seek(downloadedFileLength);
                //io流读写
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while((len = is.read(b)) != -1){
                    if(isCanceled){
                        return TYPE_CANCLED;
                    }else if(isPaused){
                        return TYPE_PAUSED;
                    }else{
                        total += len;
                        savedFile.write(b,0,len);
                        //计算已经下载的百分比
                        int progress = (int) ((total+downloadedFileLength)*100/contentLength);
                        publishProgress(progress);
                    }
                }
                //下载完成
                response.body().close();
                return TYPE_SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(is!=null){
                    is.close();
                }
                if(savedFile!=null){
                    savedFile.close();
                }
                if(isCanceled && file != null){
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }

    /**
     * 界面上更新下载进度
     * */
    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        //如果进度更新
        if(progress>lastProgress){
            //回调监听器onProgress方法更新进度
            downloadListener.onProgress(progress);
            lastProgress = progress;
        }
    }

    /**
     * 通知最终下载结果
     * */
    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer){
            case TYPE_SUCCESS:
                downloadListener.onSuccess();
                break;
            case TYPE_FAILED:
                downloadListener.onFailed();
                break;
            case TYPE_PAUSED:
                downloadListener.onPaused();
                break;
            case TYPE_CANCLED:
                downloadListener.onCanceled();
                break;
            default:
                break;
        }
    }
    public void pauseDownload(){
        isPaused = true;
    }
    public void cancelDownload(){
        isCanceled = true;
    }
    /**
     * 获取需要下载的文件总长度
     * */
    private long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = client.newCall(request).execute();
        if(request != null && response.isSuccessful()){
            long contentLength = response.body().contentLength();
            response.close();
            return contentLength;
        }
        return 0;
    }
}
