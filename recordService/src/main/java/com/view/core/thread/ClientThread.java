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
public class ClientThread extends Thread {

    private static final String TAG = ClientThread.class.getSimpleName();

    private Context mContext;
    private Handler mHandler;
    private OnClientListener mListener;

    private Socket socket;
    private boolean isHangUp = false;
    private static final String AUTH_STRING = "1234567890";
    public boolean isStopThread = false;
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
            int cnt = 0;
            while (!isStopThread) {
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
                    BufferedReader br = null;
                    BufferedWriter bw = null;
                    try {
                        mListener.onConnected(address.toString(), socket.getPort());
                        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        Log.d(TAG, "run: hello to server...");
                        bw.write(AUTH_STRING + "\n");
                        bw.flush();
                        Log.d(TAG, "run: start to read auth...");
                        String auth = br.readLine();
                        Log.d(TAG, "run: auth=" + auth);
                        if (auth == null || !auth.equals(AUTH_STRING)) {
                            Log.d(TAG, "run: auth failed!");
                            mListener.onAuthenticateFailed();
                            if (br != null) br.close();
                            br = null;
                            if (bw != null) bw.close();
                            bw = null;
                            if (socket != null) socket.close();
                            socket = null;
                            break;
                        }
                        mListener.onAuthenticateSuccess();
                        TransferManager.getInstance().initMasterDevice(socket);
                        Log.d(TAG, "run: start to read data...");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    while (!isStopThread) {
                        try {
                            String data = null;
                            if ((data = br.readLine()) != null) {
                                Log.d(TAG, "run: readline="+ data);
                                RespResult result = mHandleProtocol.unPackageProtocol(StringUtil.hexStr2Bytes(data));
                                byte[] param = result.getParams();
                                if(param != null) {
                                    parseCommand(new String(param));
                                }

                            }
                            Thread.sleep(500);
                        }catch (IOException e){
                            e.printStackTrace();
                            if(e instanceof SocketException &&
                                    e.getMessage()!=null && e.getMessage().contains("Software caused connection abort")){
                                if (socket != null) {
                                    try {
                                        socket.close();
                                    } catch (IOException ioException) {
                                        ioException.printStackTrace();
                                    }
                                }
                            }
//                            try {
//                                // 定时发送心跳包
//                                if ((++cnt % 2) == 0) {
//                                    cnt = 0;
//                                    Thread.sleep(500);
//                                    JSONObject json = new JSONObject();
//                                    json.put(Constant.KEY_DATA, "1111");
//                                    byte[] arrData = StringUtil.str2bytesGBK(json.toString());
//                                    TransferManager.getInstance().translate(Constant.CMD_HEART_TEST, arrData, null);
//                                }
//                            }catch (SDKException | InterruptedException | JSONException e1){
//                                e.printStackTrace();
//                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    try {
                        Thread.sleep(1000);
                        // 定时发送心跳包
                        if(( ++cnt % 30 ) == 0) {
                            cnt = 0;
                            byte[] arrData = StringUtil.str2bytesGBK("1111");
                            TransferManager.getInstance().translate(Constant.CMD_HEART_TEST, arrData, null);
                        }
                        Log.d(TAG, "run: client is hang up...");
                    } catch (InterruptedException | SDKException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void hangUp(boolean enable){
        this.isHangUp = enable;
    }

    private void parseCommand(String data){
        if(data != null){
            try {
                Log.d(TAG, "parseCommand: data="+ data);
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
                    }else if(cmd.equals(Constant.CMD_FETCH_REMOTE_LOCATION)){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "上送位置信息", Toast.LENGTH_SHORT).show();
                            }
                        });
                        double[] location = LocationUtil.getInstance().getLocationInfo();
                        byte[] arrCmd = StringUtil.hexStr2Bytes(cmd);
                        if(location != null){
                            Log.d(TAG, "location info="+ location[0]+ ", "+ location[1]);
                            json.put(Constant.KEY_LONGITUDE, location[0]);
                            json.put(Constant.KEY_LATITUDE, location[1]);
                        }
                        byte[] arrData = StringUtil.str2bytesGBK(json.toString());
                        TransferManager.getInstance().translate(cmd, arrData, null);
                    }else if(cmd.equals(Constant.CMD_HEART_TEST)){
                        Log.d(TAG, "CMD_HEART_TEST: "+ data);
                    }
                }
            } catch (JSONException | SDKException e) {
                e.printStackTrace();
            }
        }
    }


    public boolean getSocketStatus() {
        if(socket != null){
            Log.d(TAG, "getSocketStatus: "+ socket.isClosed() + ", "+ socket.isConnected()+ ", "+ socket.isOutputShutdown());
            try {
                socket.sendUrgentData(0xff);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}
