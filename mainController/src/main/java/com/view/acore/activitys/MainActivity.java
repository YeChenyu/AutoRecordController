package com.view.acore.activitys;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.view.acore.thread.ClientThread;
import com.view.acore.thread.Constant;
import com.view.acore.thread.OnClientListener;
import com.view.acore.thread.OnServerListener;
import com.view.acore.thread.ServerThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import Android.view.acore.R;

public class MainActivity extends AppCompatActivity implements Handler.Callback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Context mContext = this;
    private ServerThread mThread = null;
    private ClientThread mClientThread = null;

    private Handler mHandler = new Handler(this);

    private TextView mContent;
    private EditText mIp, mPort;

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
//        mThread.start();
        String IP = getlocalip();
        if(IP == null || IP.equals("0.0.0.0"))
            IP = getLocalIpAddress();
        mContent = ((TextView)findViewById(R.id.content));
        mContent.setText("本机地址："+ IP+ "\r\n");

        mIp = findViewById(R.id.address_ip);
        mPort = findViewById(R.id.address_port);

    }

    private String getlocalip() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        Log.d(TAG, "int ip "+ipAddress);
        if (ipAddress == 0) return null;
        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static final int CMD_START_CONNECT = 0;
    private static final int CMD_CONNECTED = 1;
    private static final int CMD_AUTHENTICATE_FAILED = 2;
    private static final int CMD_AUTHENTICATE_SUCCESS = 3;
    private static final int CMD_COMMAND = 4;
    private static final int CMD_CONNECTION_FAILED = 5;
    private static final int CMD_ERROR = 6;
    public void onConnectServer(View v){
        mClientThread = new ClientThread(mContext, mHandler, new OnClientListener(){

            @Override
            public void onStartConnect() {
                Message msg = mHandler.obtainMessage(CMD_START_CONNECT);
                mHandler.sendMessage(msg);
            }

            @Override
            public void onConnected(String host, int port) {
                Message msg = mHandler.obtainMessage(CMD_CONNECTED);
                msg.obj = host;
                msg.arg1 = port;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onAuthenticateFailed() {
                Message msg = mHandler.obtainMessage(CMD_AUTHENTICATE_FAILED);
                mHandler.sendMessage(msg);
            }

            @Override
            public void onAuthenticateSuccess() {
                Message msg = mHandler.obtainMessage(CMD_AUTHENTICATE_SUCCESS);
                mHandler.sendMessage(msg);
            }

            @Override
            public void onCommand(String cmd, String json) {
                Message msg = mHandler.obtainMessage(CMD_COMMAND);
                msg.arg1 = (cmd.equals(Constant.KEY_LIST) ? 1 : 2);
                msg.obj = json;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onConnectionFailed(String message) {
                Message msg = mHandler.obtainMessage(CMD_CONNECTION_FAILED);
                msg.obj = message;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onError(int errCode, String errMessage) {

            }
        });
        mClientThread.start();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case CMD_START_CONNECT:
                mContent.append("正在连接服务器...\r\n");
                break;
            case CMD_CONNECTED:
                mContent.append("连接成功！！！！！！！！！\r\n");
                mContent.append("ip: "+ (String)msg.obj+ "\r\n");
                mContent.append("port: "+ msg.arg1+ "\r\n");
                break;
            case CMD_AUTHENTICATE_FAILED:
                mContent.append("认证失败！\r\n");
                if(mClientThread != null){
                    mClientThread.interrupt();
                    mClientThread = null;
                }
                break;
            case CMD_AUTHENTICATE_SUCCESS:
                mContent.append("认证成功！！！！！！！！！\r\n");
                break;
            case CMD_COMMAND:
                String json = (String)msg.obj;
                if(msg.arg1 == 1){
                    try {
                        JSONArray jsonArray = new JSONArray(json);
                        boolean hasRemote = false;
                        if(jsonArray.length() > 0){
                            for(int i=0; i<jsonArray.length(); i++) {
                                String host = (String) jsonArray.get(i);
                                Log.d(TAG, "handleMessage: remote host="+ host+ ", local host="+ mClientThread.getHostName());
                                if(host.contains(mClientThread.getHostName()))
                                    continue;
                                hasRemote = true;
                                mContent.append("Device"+ (i+1)+ ": "+ host+ "\r\n");
                            }
                        }
                        if(!hasRemote)
                            mContent.append("暂无设备连接！\r\n");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else if(msg.what == 2){

                }
                Toast.makeText(mContext, (String)msg.obj, Toast.LENGTH_SHORT).show();
                break;
            case CMD_CONNECTION_FAILED:
                mContent.append("连接失败:"+ (String)msg.obj+ "\r\n");
                break;
            default: mContent.append("未知错误...\r\n");
                break;
        }
        return false;
    }

    public void onSearchRemote(View v){
        mContent.append("远程设备信息获取中...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                try {
                    json.put(Constant.KEY_CMD, Constant.CMD_SEARCH_REMOTE_LIST);
                    byte[] data = (json.toString()+ "\n").getBytes();
                    if(mClientThread == null){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "请确认是否成功连接服务器!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return ;
                    }
                    mClientThread.writeData(data, data.length);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void onFetchRemoteData(View v){
        mContent.append("正在远程操作中...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                try {
                    json.put(Constant.KEY_CMD, Constant.CMD_FETCH_REMOTE_DEVICE);
                    String hostname = mIp.getText().toString().trim();
                    json.put(Constant.KEY_HOSTNAME, hostname);
                    byte[] data = (json.toString()+ "\n").getBytes();
                    if(mClientThread == null){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "请确认是否成功连接服务器!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return ;
                    }
                    mClientThread.writeData(data, data.length);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void onStopRemoteOpera(View v){
        mContent.append("中止远程操作...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                try {
                    json.put(Constant.KEY_CMD, Constant.CMD_STOP_REMOTE_OPERA);
                    String hostname = mIp.getText().toString().trim();
                    json.put(Constant.KEY_HOSTNAME, hostname);
                    byte[] data = (json.toString()+ "\n").getBytes();
                    if(mClientThread == null){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "请确认是否成功连接服务器!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return ;
                    }
                    mClientThread.writeData(data, data.length);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
        if(mClientThread != null){
            mThread.interrupt();
            mThread = null;
        }
    }
}
