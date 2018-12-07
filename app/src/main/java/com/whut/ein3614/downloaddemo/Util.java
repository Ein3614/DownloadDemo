package com.whut.ein3614.downloaddemo;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

/**
 * 类描述：
 * 创建人：Created by Administrator on 2018/8/21.
 * 修改人：
 * 修改时间：
 */
public class Util {
    public static NotificationManager getNotificationManager(Context context){
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
    public static Notification getNotification(Context context,Class target,String channel,String title,int progress){
        Intent intent = new Intent(context,target);
        PendingIntent pi = PendingIntent.getActivity(context,0,intent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,channel);
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher_round));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if(progress>0){
            builder.setContentText(progress+"%");
            //第一个参数为最大进度，第二个参数为当前进度，第三个参数为是否使用模糊进度条
            builder.setProgress(100,progress,false);
        }
        return builder.build();
    }
    @TargetApi(Build.VERSION_CODES.O)
    public static void createNotificationChannel(Context context, String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        getNotificationManager(context).createNotificationChannel(channel);
    }
}
