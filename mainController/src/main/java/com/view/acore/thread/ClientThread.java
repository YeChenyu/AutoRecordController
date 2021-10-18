package com.view.acore.thread;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.auto.commonlibrary.transfer.HandleProtocol;
import com.auto.commonlibrary.transfer.TransferManager;
import com.auto.commonlibrary.util.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
public class ClientThread extends Thread {

    private static final String TAG = ClientThread.class.getSimpleName();

    private Context mContext;
    private Handler mHandler;
    private OnClientListener mListener;

    private Socket socket;
    private static final String AUTH_STRING = "1234567890";

    private String mClientHostname ;
    private boolean isRunning = false;
    private HandleProtocol mHandleProtocol = new HandleProtocol();

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
                    mClientHostname = local.toString();
                }
                BufferedReader br = null;
                try {
                    mListener.onConnected(address.toString(), socket.getPort());
                    br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    Log.d(TAG, "run: hello to server...");
                    bw.write(AUTH_STRING + "\n");
                    bw.flush();
                    Log.d(TAG, "run: start to read auth...");
                    String auth = br.readLine();
                    Log.d(TAG, "run: auth=" + auth);
                    if (auth == null || !auth.equals(AUTH_STRING)) {
                        Log.d(TAG, "run: auth failed!");
                        mListener.onAuthenticateFailed();
                        if(br != null) br.close(); br = null;
                        if(bw != null) bw.close(); bw = null;
                        if(socket != null) socket.close();
                        socket = null;
                        return;
                    }
                    TransferManager.getInstance().initMasterDevice(socket);
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
                while (true) {

                    try {
                        String data = TransferManager.getInstance().readLine();
//                        Log.d(TAG, "run: read jsonData="+ data);
                        if (data != null) {
                            if (parseCommand(data)) {
                                int length = 0;
                                String readData = null;
                                while ((readData = TransferManager.getInstance().readDataLine()) != null) {
                                    Log.d(TAG, "read "+ readData.length()+ " :"+ readData);
                                    byte[] result = StringUtil.hexStr2Bytes(readData);
                                    length += result.length;
                                    if(fileBw != null){
                                        fileBw.write(result);
                                        fileBw.flush();
                                    }
                                    if(length >= fileLength) {
                                        break;
                                    }
                                    Thread.sleep(50);
                                }
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
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
                    }
                    else if(cmd.equals(Constant.CMD_FETCH_REMOTE_DEVICE)){
                        if(!json.has(Constant.KEY_FILE)) {
                            mListener.onCommand(Constant.KEY_FILE, null);
                        }
                    }
                    else if(cmd.equals(Constant.CMD_FETCH_REMOTE_LOCATION)){
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
        File root = new File(Constant.LOCAL_STORAGE+ mClientHostname);
        try {
            if(!root.exists()){
                root.mkdirs();
            }
            File file = new File(root.getAbsolutePath()+ "/"+ fileName);
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

    public String getHostName(){
        return mClientHostname==null ? "unknown" : mClientHostname;
    }

    public void stopClient(){
        this.isRunning = false;
    }
}
