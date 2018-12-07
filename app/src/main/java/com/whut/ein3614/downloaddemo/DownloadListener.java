package com.whut.ein3614.downloaddemo;

/**
 * 类描述：用于对下载过程中各个状态进行监听的接口
 * 创建人：Created by Administrator on 2018/8/21.
 * 修改人：
 * 修改时间：
 */
public interface DownloadListener {
    void onProgress(int progress);
    void onSuccess();
    void onFailed();
    void onPaused();
    void onCanceled();
}
