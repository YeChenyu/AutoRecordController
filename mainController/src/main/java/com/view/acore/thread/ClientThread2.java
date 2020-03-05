package com.view.acore.thread;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.auto.commonlibrary.transfer.TransferManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
public class ClientThread2 extends Thread {

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

    public ClientThread2(Context mContext, Handler handler, OnClientListener mListener) {
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
                        isRunning = false;
                        break;
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
            mListener.onConnected(hostname, socket.getLocalPort());
            TransferManager.getInstance().initMasterDevice(socket);
            mListener.onAuthenticateSuccess();
        }
    }

    private boolean isFileTransfer = false;
    private String currFileName;
    private long fileLength = 0;
    private boolean parseCommand(String data){
        Log.d(TAG, "parseCommand: data="+ data);
        if(data != null){
            try {
                JSONObject json = new JSONObject(data);
                if(json.has(Constant.KEY_LIST)) {
                    String ips = json.getString(Constant.KEY_LIST);
                    Log.d(TAG, "parseCommand: cmd="+ ips);
                    mListener.onCommand(Constant.KEY_LIST, ips);
                    return false;
                }else if(json.has(Constant.KEY_CMD)){
                    String cmd = json.getString(Constant.KEY_CMD);
                    if(cmd.equals(Constant.CMD_RETURN_REMOTE_DEVICE)){
                        currFileName = json.getString(Constant.KEY_FILE);
                        fileLength = json.getLong(Constant.KEY_LENGTH);
                        String main = json.getString(Constant.KEY_HOSTNAME);
                        if (fileBw != null) {
                            try {
                                fileBw.close();
                                fileBw = null;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        fileBw = createFile(currFileName);
                        if (fileBw != null) {
                            isFileTransfer = true;
                            Log.d(TAG, "parseCommand: transfer status=" + isFileTransfer);
                            mListener.onCommand(Constant.KEY_FILE, currFileName);
                            return true;
                        }else{
                            return false;
                        }
                    }else if(cmd.equals(Constant.CMD_FETCH_REMOTE_DEVICE)){
                        if(!json.has(Constant.KEY_FILE)) {
                            mListener.onCommand(Constant.KEY_FILE, null);
                        }
                    }else if(cmd.equals(Constant.CMD_FETCH_REMOTE_LOCATION)){
                        if(json.has(Constant.KEY_LONGITUDE) && json.has(Constant.KEY_LATITUDE)){
                            mListener.onCommand(Constant.CMD_FETCH_REMOTE_LOCATION, data);
                        }else{
                            mListener.onCommand(Constant.CMD_FETCH_REMOTE_LOCATION, null);
                        }
                        return false;
                    }
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }
        return false;
    }

    private FileOutputStream fileBw;
    private FileOutputStream createFile(String fileName){
        File file = new File("/mnt/sdcard/"+ fileName.replace(".", "_"+ System.currentTimeMillis()+ "."));
        try {
            if (!file.exists())
                file.createNewFile();
            return new FileOutputStream(file);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }




    public void writeData(byte[] data, int length){
        if(os != null){
            try {
                os.flush();
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
