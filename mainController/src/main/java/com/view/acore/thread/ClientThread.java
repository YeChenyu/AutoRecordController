package com.view.acore.thread;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

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
    private static final String AUTH_STRING = "1234567890";

    public ClientThread(Context mContext, Handler handler, OnClientListener mListener) {
        this.mContext = mContext;
        mHandler = handler;
        this.mListener = mListener;
    }

    @Override
    public void run() {
        if(socket == null) {
            while (true) {
                mListener.onStartConnect();
                Log.d(TAG,"run: start to connect server...");
                socket = new Socket();
                try {
                    socket.setSoTimeout(Constant.SERVER_CONNECT_TIMEOUT);
                    socket.connect(new InetSocketAddress(Constant.SERVER_IP, Constant.SERVER_PORT),
                            Constant.SERVER_CONNECT_TIMEOUT);
                    if (!socket.isConnected()) {
                        Log.e(TAG, "run: connect failed!");
                        socket.close();
                        socket = null;
                        mListener.onConnectionFailed("connection status error");
                        continue;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    mListener.onConnectionFailed(e.getMessage());
                    try {
                        Thread.sleep(5*1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    continue;
                }
                final SocketAddress address = socket.getRemoteSocketAddress();
                if (address != null) {
                    Log.d(TAG, "run: server path=" + address.toString() +
                            ", status=" + socket.isConnected());
                }
                try {
                    mListener.onConnected(address.toString(), socket.getPort());
                    is = socket.getInputStream();
                    os = socket.getOutputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                    Log.d(TAG, "run: hello to server...");
                    bw.write(AUTH_STRING + "\r\n");
                    bw.flush();
                    Log.d(TAG, "run: start to read auth...");
                    String auth = br.readLine();
                    Log.d(TAG, "run: auth=" + auth);
                    if (auth == null || auth.equals(AUTH_STRING)) {
                        Log.d(TAG, "run: auth failed!");
                        mListener.onAuthenticateFailed();
                        if (is != null) is.close();
                        if (os != null) os.close();
                        socket.close();
                        socket = null;
                        break;
                    }
                    mListener.onAuthenticateSuccess();
                    String data = null;
                    Log.d(TAG, "run: start to read data...");
                    while ((data = br.readLine()) != null) {
                        Log.d(TAG, "run: receive data=" + data);
                        parseCommand(data);
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                    mListener.onError(-1, e.getMessage());
                }finally {
                    try {
                        if(is != null)is.close();
                        if(os != null)os.close();
                        if(socket != null) {
                            if (!socket.isInputShutdown()) socket.shutdownInput();
                            if (!socket.isOutputShutdown()) socket.isOutputShutdown();
                            socket.close();
                            socket = null;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        mListener.onError(-2, e.getMessage());
                    }
                }
            }
        }
    }

    private void parseCommand(String data){
        if(data != null){
            if(data.equals("123")){
                mListener.onCommand("123", 0, null);
            }else{

            }
        }
    }


    public void writeData(byte[] data, int length){
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
