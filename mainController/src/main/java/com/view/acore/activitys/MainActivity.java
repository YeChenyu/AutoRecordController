package com.view.acore.activitys;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.view.acore.thread.OnServerListener;
import com.view.acore.thread.ServerThread;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import Android.view.acore.R;

public class MainActivity extends AppCompatActivity implements Handler.Callback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Context mContext = this;
    private ServerThread mThread = null;

    private Handler mHandler = new Handler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mThread = new ServerThread(mContext, new OnServerListener() {
            @Override
            public void onCommand(String cmd, String data) {
                Message msg = mHandler.obtainMessage();
                msg.obj = data;
                mHandler.sendMessage(msg);
            }
        });
        mThread.start();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mThread != null)
            mThread.interrupt();
        mThread = null;
    }

    @Override
    public boolean handleMessage(Message msg) {
        Toast.makeText(mContext, (String)msg.obj, Toast.LENGTH_SHORT).show();
        return false;
    }
}
