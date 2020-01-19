package com.view.core.thread;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 * @author: yechenyu
 * @create: 2020/1/5 上午9:24
 * @email: Yecynull@163.com
 * @version:
 * @descripe:
 **/
public class ClientThread extends Thread {

    private static final String TAG = ClientThread.class.getSimpleName();

    private Context mContext;
    private Handler mHandler;
    private OnClientListener mListener;

    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private boolean isHangUp = false;
    private static final String AUTH_STRING = "1234567890";

    public ClientThread(Context mContext, Handler handler, OnClientListener mListener) {
        this.mContext = mContext;
        mHandler = handler;
        this.mListener = mListener;
    }

    @Override
    public void run() {
        if(socket == null) {
            try {
                socket = new Socket();
                socket.setSoTimeout(Constant.SERVER_CONNECT_TIMEOUT);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            while (true) {
                if (!isHangUp) {
                    mListener.onStartConnect();
                    try {
                        Log.d(TAG, "run: start to connect server...");
                        if (!socket.isConnected()) {
                            socket.connect(new InetSocketAddress(Constant.SERVER_IP, Constant.SERVER_PORT),
                                    Constant.SERVER_CONNECT_TIMEOUT);
                            if (!socket.isConnected()) {
                                Log.e(TAG, "run: connect failed!");
                                socket.close();
                                socket = null;
                                mListener.onConnectionFailed("connection status error");
                                Thread.sleep(1 * 1000);
                                continue;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        mListener.onConnectionFailed(e.getMessage());
                        try {
                            Thread.sleep(3 * 1000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        continue;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        continue;
                    }
                    final SocketAddress address = socket.getRemoteSocketAddress();
                    if (address != null) {
                        Log.d(TAG, "run: server path=" + address.toString() +
                                ", status=" + socket.isConnected());
                        if(Constant.isDebug) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext, "Server: " + address.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    try {
                        mListener.onConnected(address.toString(), socket.getPort());
                        is = socket.getInputStream();
                        os = socket.getOutputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                        Log.d(TAG, "run: hello to server...");
                        bw.write(AUTH_STRING + "\n");
                        bw.flush();
                        Log.d(TAG, "run: start to read auth...");
                        String auth = br.readLine();
                        Log.d(TAG, "run: auth=" + auth);
                        if (auth == null || !auth.equals(AUTH_STRING)) {
                            Log.d(TAG, "run: auth failed!");
                            mListener.onAuthenticateFailed();
                            if (is != null) is.close();
                            if (os != null) os.close();
                            if (br != null) br.close();
                            br = null;
                            if (bw != null) bw.close();
                            bw = null;
                            if (socket != null) socket.close();
                            socket = null;
                            break;
                        }
                        mListener.onAuthenticateSuccess();
                        Log.d(TAG, "run: start to read data...");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    byte[] result = new byte[256];
                    int ret = -1;
                    while (true) {
                        try {
                            if ((ret = is.read(result)) != -1) {
                                byte[] data = new byte[ret];
                                System.arraycopy(result, 0, data, 0, ret);
                                parseCommand(new String(data));
                            }
                            Thread.sleep(500);
                        }catch (IOException e){
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    try {
                        Thread.sleep(1000);
                        Log.d(TAG, "run: client is hang up...");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                if (is != null) is.close();
                if (os != null) os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void hangUp(boolean enable){
        this.isHangUp = enable;
    }

    private void parseCommand(String data){
        if(data != null){
            try {
                JSONObject json = new JSONObject(data);
                if(json.has(Constant.KEY_CMD)){
                    String cmd = (String) json.get(Constant.KEY_CMD);
                    if(cmd.equals(Constant.CMD_FETCH_REMOTE_DEVICE)
                            || cmd.equals(Constant.CMD_FETCH_REMOTE_PHONE)
                            || cmd.equals(Constant.CMD_FETCH_REMOTE_SCREEN)){
                        //开始进行远程操作
                        String hostname = (String) json.get(Constant.KEY_HOSTNAME);
                        mListener.onCommand(cmd, hostname);
                    }else if(cmd.equals(Constant.CMD_STOP_REMOTE_OPERA)
                            || cmd.equals(Constant.CMD_STOP_REMOTE_PHONE)
                            || cmd.equals(Constant.CMD_STOP_REMOTE_SCREEN)){
                        //停止远程操作
                        String hostname = (String) json.get(Constant.KEY_HOSTNAME);
                        mListener.onCommand(cmd, hostname);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public void writeData(byte[] data, int length){
        Log.d(TAG, "writeData: length="+ length);
        if(os != null){
            try {
                os.write(data, 0, length);
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
