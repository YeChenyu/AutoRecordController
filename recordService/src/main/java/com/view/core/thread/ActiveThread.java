package com.view.core.thread;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.auto.commonlibrary.exception.SDKException;
import com.auto.commonlibrary.transfer.HandleProtocol;
import com.auto.commonlibrary.transfer.RespResult;
import com.auto.commonlibrary.transfer.TransferManager;
import com.auto.commonlibrary.util.StringUtil;
import com.view.core.MyApplication;
import com.view.core.utils.LocationUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 * @author:  xxx
 * @create: 2020/1/5 上午9:24
 * @email:  xxx.xxx.xxx
 * @version:
 * @descripe:
 **/
public class ActiveThread extends Thread {

    private static final String TAG = ActiveThread.class.getSimpleName();

    private Context mContext;
    private Handler mHandler;
    private OnClientListener mListener;

    private Socket socket;
    private boolean isHangUp = false;
    public boolean isStopThread = false;

    private ClientThread mClientThread = null;

    public ActiveThread(){

    }

    public ActiveThread(Context mContext, Handler handler, OnClientListener mListener) {
        this.mContext = mContext;
        mHandler = handler;
        this.mListener = mListener;
    }

    @Override
    public void run() {

        long cnt = 0;
        while (!isStopThread) {
            try {
                //连接服务器等待接收指令
                if(mClientThread == null) {
                    mClientThread = new ClientThread(mContext, mHandler, mListener);
                    mClientThread.start();
                    MyApplication.setClientThread(mClientThread);
                }
                Thread.sleep(5000);
                boolean state = mClientThread.getSocketStatus();
                if(!state){
                    mClientThread.isStopThread = true;
                    mClientThread.hangUp(true);
                    mClientThread.interrupt();
                    mClientThread = null;
                    MyApplication.setClientThread(mClientThread);
                    Log.d(TAG, "run: restart active thread");
                }
                Log.d(TAG, "run: test "+ ++cnt);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void hangUp(boolean enable){
        this.isHangUp = enable;
    }



}
