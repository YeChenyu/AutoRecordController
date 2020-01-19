package com.view.core.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.view.core.aidl.OnPhoneRecordListener;
import com.view.core.aidl.PhoneRecord;
import com.view.core.utils.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * @author: yechenyu
 * @create: 2019/12/31 下午11:21
 * @email: Yecynull@163.com
 * @version:
 * @descripe:
 **/
public class PhoneRecordBinder extends PhoneRecord.Stub implements Handler.Callback {


    private static final String TAG = PhoneService.class.getSimpleName();
    private static final String OUTGOING_ACTION = "android.intent.action.NEW_OUTGOING_CALL";

    private Context mContext;
    private OnPhoneRecordListener mListener;
    private MyPhoneStateReceiver myPhoneStateReceiver;

    private TelephonyManager tm;
    private MediaRecorder mediaRecorder;
    private PhoneListener listener;

    private File file;
    private String incomeNumber;
    private Handler mHandler;
    private static final int MSG_TYPE_COUNT_DOWN = 110;
    //已经录制多少秒了
    private int mRecordSeconds = 0;


    public PhoneRecordBinder(Context context){
        mContext = context;

        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        listener = new PhoneListener();
        mHandler = new Handler(this);

        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);  //注册监听器 监听电话状态

        IntentFilter intentFilter = new IntentFilter();
        // 监听去电广播
        intentFilter.addAction(OUTGOING_ACTION);
        myPhoneStateReceiver = new MyPhoneStateReceiver();
        // 动态注册去电广播接收器
        context.registerReceiver(myPhoneStateReceiver, intentFilter);

        Toast.makeText(context,"开启", Toast.LENGTH_LONG).show();
    }

    @Override
    public void initRecordService(Bundle param, OnPhoneRecordListener listener) throws RemoteException {
        mListener = listener;
        if(param.containsKey("KEY_RECORD_FILE")){
            file = new File(param.getString("KEY_RECORD_FILE"));
        } else{
            file = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".3gp");
        }
        mRecordSeconds = 0;
    }

    @Override
    public boolean isReady() throws RemoteException {
        return false;
    }

    @Override
    public void stopRecord() throws RemoteException {
        if(mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        mRecordSeconds = 0;
        mContext.unregisterReceiver(myPhoneStateReceiver);
    }

    @Override
    public void pauseRecord() throws RemoteException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if(mediaRecorder != null)mediaRecorder.pause();
        }
    }

    @Override
    public void resumeRecord() throws RemoteException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if(mediaRecorder != null)mediaRecorder.resume();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case MSG_TYPE_COUNT_DOWN:{
                String str = null;
                boolean enough = FileUtil.getSDFreeMemory() / (1024* 1024) < 4;
                if (enough){
                    //空间不足，停止录屏
                    try {
                        if(mediaRecorder != null){
                            mediaRecorder.stop();
                            mediaRecorder.release();
                            mediaRecorder = null;
                            mRecordSeconds = 0;
                        }
                        mListener.onRecordError(-2, "存储空间不足！");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                }

                mRecordSeconds++;
                try {
                    mListener.updateProgress(mRecordSeconds, 180, "" );
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                if (mRecordSeconds < 10 ){
                    mHandler.sendEmptyMessageDelayed(MSG_TYPE_COUNT_DOWN,1000);
                } else if (mRecordSeconds ==  10 ){
                    try {
                        if(mediaRecorder != null) {
                            mediaRecorder.stop();
                            mediaRecorder.release();
                            mediaRecorder = null;
                            mRecordSeconds = 0;
                            mContext.unregisterReceiver(myPhoneStateReceiver);
                            mListener.onRecordSuccess(file.getAbsolutePath());
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
        return true;
    }


    private class PhoneListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            try {
                switch(state) {
                    //来电
                    case TelephonyManager.CALL_STATE_RINGING:
                        incomeNumber = incomingNumber;
                        break;
                    //接通电话
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        mediaRecorder = new MediaRecorder();
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);   //获得声音数据源
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);   // 按3gp格式输出
                        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                        mediaRecorder.setOutputFile(file.getAbsolutePath());   //输出文件
                        mediaRecorder.prepare();    //准备
                        mediaRecorder.start();
                        mListener.onRecordStart();
                        mHandler.sendEmptyMessageDelayed(MSG_TYPE_COUNT_DOWN,1000);
                        break;
                    //挂掉电话
                    case TelephonyManager.CALL_STATE_IDLE:
                        if(mediaRecorder != null) {
                            mediaRecorder.stop();
                            mediaRecorder.release();
                            mediaRecorder = null;
                            mContext.unregisterReceiver(myPhoneStateReceiver);
                            mListener.onRecordSuccess(file.getAbsolutePath());
                            mListener.onPhoneIdel();
                        }
                        break;
                }
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private class MyPhoneStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取去电号码
            String outgoingNumber = getResultData();
            incomeNumber = outgoingNumber;
        }
    }
}
