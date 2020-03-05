package com.view.core.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.view.core.MyApplication;
import com.view.core.activitys.MainActivity;
import com.view.core.activitys.PhoneRecordActivity;
import com.view.core.activitys.ScreenRecordActivity;
import com.view.core.thread.ClientThread;
import com.view.core.thread.Constant;
import com.view.core.thread.OnClientListener;
import com.view.core.utils.FloatViewUtil;
import com.view.core.utils.LocationUtil;

import Android.view.core.R;

/**
 * @author: xxx
 * @create: 2020/1/30 下午9:38
 * @email: xxx.xxx.xxx
 * @version:
 * @descripe:
 **/
public class SocketService extends Service {

    private static final String TAG = SocketService.class.getSimpleName();

    private Context mContext ;
    private Handler mHandler = new Handler();

    private ClientThread mClientThread = null;

    public static final int PID = android.os.Process.myPid();
    private ServiceConnection mConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: executed "+ PID);
        mContext = this;
        LocationUtil.getInstance().initLocationManager(mContext);
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
                .setContentTitle("SocketService: "+ PID)
                .build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        return notification;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: executed "+ PID+ " "+ intent);
        //连接服务器等待接收指令
        if(mClientThread == null) {
            mClientThread = new ClientThread(this, mHandler, mListener);
            mClientThread.start();
            MyApplication.setClientThread(mClientThread);
        }
        //显示透明浮窗
        FloatViewUtil.getInstance().showFloatingWindow(mContext);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //中断接收指令
        if(mClientThread != null){
            mClientThread.isStopThread = true;
            mClientThread.interrupt();
            mClientThread = null;
            MyApplication.setClientThread(null);
        }
    }

    private OnClientListener mListener = new OnClientListener() {

        @Override
        public void onStartConnect() {
        }

        @Override
        public void onConnected(final String host, final int ip) {
            if(Constant.isDebug) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, ("服务器，" + host), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        @Override
        public void onAuthenticateFailed() {

        }

        @Override
        public void onAuthenticateSuccess() {

        }

        @Override
        public void onCommand(String cmd, String json) {
            Log.d(TAG, "onCommand: cmd="+ cmd+ ", json="+ json);
            if(cmd.equals(Constant.CMD_FETCH_REMOTE_DEVICE)){
                Intent intent = new Intent();
                intent.setClass(mContext, MainActivity.class);
                intent.setPackage(getPackageName());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle bundle = new Bundle();
                bundle.putString(Constant.KEY_HOSTNAME, json);
                intent.putExtra("data", bundle);
                startActivity(intent);
            }else if(cmd.equals(Constant.CMD_STOP_REMOTE_OPERA)){
                PhoneRecordActivity.PhoneServiceConnection mPhoneServiceConnection = ((MyApplication)getApplication()).getPhoneServiceConnection();
                if(mPhoneServiceConnection != null){
                    unbindService(mPhoneServiceConnection);
                }

                ScreenUtil.stopScreenRecord(mContext);

                /**
                 * 开启录音
                 */
            }else if(cmd.equals(Constant.CMD_FETCH_REMOTE_PHONE)){
                Log.d(TAG, "onCommand: start phone record");
                Intent intent = new Intent();
                intent.setClass(mContext, PhoneRecordActivity.class);
                intent.setPackage(getPackageName());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle bundle = new Bundle();
                bundle.putString(Constant.KEY_HOSTNAME, json);
                intent.putExtra("data", bundle);
                startActivity(intent);
            }else if(cmd.equals(Constant.CMD_STOP_REMOTE_PHONE)){
                PhoneRecordActivity.PhoneServiceConnection mPhoneServiceConnection = ((MyApplication)getApplication()).getPhoneServiceConnection();
                if(mPhoneServiceConnection != null){
                    Log.d(TAG, "onCommand: unbindservice");
                    try {
                        if(Constant.isDebug){
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext, "收到停止录音指令", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        if(mPhoneServiceConnection.record != null) {
                            mPhoneServiceConnection.record.stopRecord();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                /**
                 * 开启录屏
                 */
            }else if(cmd.equals(Constant.CMD_FETCH_REMOTE_SCREEN)){
                Log.d(TAG, "onCommand: start screen record");
                Intent intent = new Intent();
                intent.setClass(mContext, ScreenRecordActivity.class);
                intent.setPackage(getPackageName());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle bundle = new Bundle();
                bundle.putString(Constant.KEY_HOSTNAME, json);
                intent.putExtra("data", bundle);
                startActivity(intent);
            }else if(cmd.equals(Constant.CMD_STOP_REMOTE_SCREEN)){
                ScreenUtil.stopScreenRecord(mContext);
            }
        }

        @Override
        public void onConnectionFailed(String message) {

        }

        @Override
        public void onError(int errCode, String errMessage) {

        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
