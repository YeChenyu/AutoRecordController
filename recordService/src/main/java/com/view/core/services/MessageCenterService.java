package com.view.core.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;

import com.view.core.MyApplication;

import Android.view.core.R;

/**
 * @author: xxx
 * @create: 2020/2/4 下午11:11
 * @email: xxxx.xxxx.xxxx
 * @version:
 * @descripe:
 **/
public class MessageCenterService extends Service {

    private static final String sTAG = "MessageCenterService";

    //9521 就是你的终身代号 :)
    static final int NOTIFY_ID = 9521;

    private static MessageCenterService instance;

    public static MessageCenterService getInstance() {
        return instance;
    }

    /**
     * 启动前台服务
     */
    public static void start() {
        try {
            Intent intent = new Intent(MyApplication.getContext(), MessageCenterService.class);
            MyApplication.getContext().startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 终止前台服务。包含{@link MessageCenterService.KernelService}
     */
    public static void stop() {
        try {
            Intent intent = new Intent(MyApplication.getContext(), MessageCenterService.class);
            MyApplication.getContext().stopService(intent);

            stopKernel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void startKernel() {
        try {
            Intent intent = new Intent(MyApplication.getContext(), KernelService.class);
            MyApplication.getContext().startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void stopKernel() {
        try {
            Intent intent = new Intent(MyApplication.getContext(), KernelService.class);
            MyApplication.getContext().stopService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private Notification getNotification() {
        //新增---------------------------------------------
        String CHANNEL_ONE_ID = "com.primedu.cn";
        String CHANNEL_ONE_NAME = "Channel One";
        NotificationChannel notificationChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }
        //--------------------------------------------------------新增

        Notification notification = new Notification.Builder(this)
                .setChannelId(CHANNEL_ONE_ID)
                .setTicker("Nature")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("PID: "+ NOTIFY_ID)
                .build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        return notification;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            startForeground(NOTIFY_ID, getNotification());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //启动真正的Service
        startForeground(NOTIFY_ID, getNotification());
        startKernel();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            stopForeground(true);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     *
     */
    public static class KernelService extends Service {
        private static KernelService instance;

        public static KernelService getInstance() {
            return instance;
        }

        @Override
        public void onCreate() {
            super.onCreate();
            instance = this;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            super.onStartCommand(intent, flags, startId);
            try {
                MessageCenterService fakeService = MessageCenterService.getInstance();
                startForeground(NOTIFY_ID, MessageCenterService.getInstance().getNotification());
                fakeService.stopSelf();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return START_STICKY;
        }

        @Override
        public void onDestroy() {
            MessageCenterService.getInstance().stopForeground(true);
            instance = null;
            super.onDestroy();
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}
