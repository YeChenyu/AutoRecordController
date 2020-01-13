package com.view.acore.thread;

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
    private static final String AUTH_STRING = "1234567890";

    private String hostname ;
    private boolean isRunning = false;

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
            isRunning = true;
            while (isRunning) {
                try {
                    mListener.onStartConnect();
                    Log.d(TAG,"run: start to connect server...");
                    if(!socket.isConnected()) {
                        socket.connect(new InetSocketAddress(Constant.SERVER_IP, Constant.SERVER_PORT),
                                Constant.SERVER_CONNECT_TIMEOUT);
                        if (!socket.isConnected()) {
                            Log.e(TAG, "run: connect status error!");
                            mListener.onConnectionFailed("run: connect status error!");
                            Thread.sleep(1 * 1000);
                            continue;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    //连接异常，等待3s 再次发起连接
                    mListener.onConnectionFailed(e.getMessage());
                    try {
                        Thread.sleep(3*1000);
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
                }
                SocketAddress local = socket.getLocalSocketAddress();
                if(local != null){
                    hostname = local.toString();
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
                        if (is != null) is.close(); is = null;
                        if (os != null) os.close(); os = null;
                        if(br != null) br.close(); br = null;
                        if(bw != null) bw.close(); bw = null;
                        if(socket != null) socket.close();
                        socket = null;
                        return;
                    }
                    mListener.onAuthenticateSuccess();
                    Log.d(TAG, "run: start to read data...");
                }catch (IOException e) {
                    e.printStackTrace();
                    mListener.onError(-1, e.getMessage());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
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
            }
            try {
                if (is != null)
                    is.close();
                is = null;
                if(os != null)
                    os.close();
                os = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseCommand(String data){
        Log.d(TAG, "parseCommand: data="+ data);
        if(data != null){
            try {
                JSONObject json = new JSONObject(data);
                if(json.has(Constant.KEY_LIST)) {
                    String ips = json.getString(Constant.KEY_LIST);
                    Log.d(TAG, "parseCommand: cmd="+ ips);
                    mListener.onCommand(Constant.KEY_LIST, ips);
                }else if(json.has(Constant.KEY_CMD)){
                    String cmd = json.getString(Constant.KEY_CMD);
                    if(cmd.equals(Constant.CMD_RETURN_REMOTE_DEVICE)){
                        String fileName = json.getString(Constant.KEY_FILE);
                        long length = json.getLong(Constant.KEY_LENGTH);
                        String main = json.getString(Constant.KEY_HOSTNAME);

                        receiveFileFromRemote(fileName, length);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void receiveFileFromRemote(String fileName, long length){
        Log.d(TAG, "receiveFileFromRemote: filename="+ fileName+ ", length="+ length);

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

    public String getHostName(){
        return hostname==null ? "unknown" : hostname;
    }
}
