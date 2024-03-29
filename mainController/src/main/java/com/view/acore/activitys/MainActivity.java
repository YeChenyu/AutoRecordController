package com.view.acore.activitys;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.auto.commonlibrary.exception.SDKException;
import com.auto.commonlibrary.transfer.TransferManager;
import com.view.acore.thread.ClientThread;
import com.view.acore.thread.Constant;
import com.view.acore.thread.OnClientListener;
import com.view.acore.thread.OnServerListener;
import com.view.acore.thread.ServerThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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

    private ScrollView scrollView;
    private TextView mContent;
    private EditText mIp, mPort;
    private Button btnConnect;

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
        String IP = getlocalip();
        if(IP == null || IP.equals("0.0.0.0"))
            IP = getLocalIpAddress();
        scrollView = findViewById(R.id.scrollview);
        btnConnect = findViewById(R.id.btn_connect);
        mContent = ((TextView)findViewById(R.id.content));
        mContent.setText("本机地址："+ IP+ "\r\n");
//        setTitle("本机地址："+ IP);

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
        if(btnConnect.getText().toString().equals("连接服务器")) {
            if (mClientThread == null) {
                mClientThread = new ClientThread(mContext, mHandler, new OnClientListener() {

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
                        msg.arg1 = 0;
                        if (cmd.equals(Constant.KEY_LIST)) {
                            msg.arg1 = 1;
                        } else if (cmd.equals(Constant.KEY_FILE)) {
                            msg.arg1 = 2;
                        } else if (cmd.equals(Constant.CMD_FETCH_REMOTE_LOCATION)) {
                            msg.arg1 = 3;
                        }
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
        }else{
            if(mClientThread != null){
                mClientThread.stopClient();
                mClientThread.interrupt();
                mClientThread = null;
            }
            btnConnect.setText("连接服务器");
            appendMessageToContent("成功断开服务器，请重新连接！！！！！！！！！");
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case CMD_START_CONNECT:
                appendMessageToContent("正在连接服务器...");
                break;
            case CMD_CONNECTED:
                appendMessageToContent("连接成功！！！！！！！！！");
                appendMessageToContent("ip: "+ (String)msg.obj+ "");
                appendMessageToContent("port: "+ msg.arg1+ "");
                btnConnect.setText("断开服务器");
                break;
            case CMD_AUTHENTICATE_FAILED:
                appendMessageToContent("认证失败！");
                if(mClientThread != null){
                    mClientThread.stopClient();
                    mClientThread.interrupt();
                    mClientThread = null;
                }
                break;
            case CMD_AUTHENTICATE_SUCCESS:
                appendMessageToContent("认证成功！！！！！！！！！");
                break;
            case CMD_COMMAND:
                String json = (String)msg.obj;
                Log.d(TAG, "handleMessage: cmd="+ msg.arg1+ ", data="+ json);
                if(msg.arg1 == 1){
                    /**
                     * 接收设备列表
                     */
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
                                appendMessageToContent("Device"+ (i+1)+ ": "+ host+ "");
                            }
                        }
                        if(!hasRemote)
                            appendMessageToContent("暂无设备连接！");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                /**
                 * 文件传输
                 */
                }else if(msg.arg1 == 2){
                    if(json == null){
                        appendMessageToContent("暂无文件！");
                    }else{
                        appendMessageToContent("接收文件："+ json+ " 请在文件管理器中确认！");
                    }
                }else if(msg.arg1 == 3){
                    if(json == null){
                        appendMessageToContent("无法获取位置信息！");
                    }else{
                        try {
                            JSONObject jsonObj = new JSONObject(json);
                            appendMessageToContent("位置："+ jsonObj.getString(Constant.KEY_LONGITUDE)+
                                    " "+ jsonObj.getString(Constant.KEY_LATITUDE));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            appendMessageToContent("无法获取位置信息！");
                        }
                    }
                }
                Toast.makeText(mContext, (String)msg.obj, Toast.LENGTH_SHORT).show();
                break;
            case CMD_CONNECTION_FAILED:
                appendMessageToContent("连接失败:"+ (String)msg.obj+ "");
                if(mClientThread != null){
                    mClientThread.stopClient();
                    mClientThread.interrupt();
                    mClientThread = null;
                }
                btnConnect.setText("连接服务器");
                break;
            default:
                appendMessageToContent("未知错误...");
                if(mClientThread != null){
                    mClientThread.stopClient();
                    mClientThread.interrupt();
                    mClientThread = null;
                }
                btnConnect.setText("连接服务器");
                break;
        }
        return false;
    }

    public void onSearchRemote(View v){
        if(isDoubleClick()) return;
        appendMessageToContent("远程设备信息获取中...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                try {
                    json.put(Constant.KEY_CMD, Constant.CMD_SEARCH_REMOTE_LIST);
                    byte[] data = json.toString().getBytes();
                    if(mClientThread == null){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "请确认是否成功连接服务器!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return ;
                    }
                    TransferManager.getInstance().translate(Constant.CMD_SEARCH_REMOTE_LIST, data, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SDKException e) {
                    e.printStackTrace();
                    handleSDKExecption(e);
                }
            }
        }).start();
    }

    public void onFetchRemoteData(View v){
        if(isDoubleClick()) return;
        appendMessageToContent("获取服务器文件...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                try {
                    json.put(Constant.KEY_CMD, Constant.CMD_FETCH_REMOTE_DEVICE);
                    String hostname = mIp.getText().toString().trim();
                    json.put(Constant.KEY_HOSTNAME, hostname);
                    byte[] data = (json.toString()).getBytes();
                    if(mClientThread == null){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "请确认是否成功连接服务器!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return ;
                    }
                    TransferManager.getInstance().translate(Constant.CMD_FETCH_REMOTE_DEVICE, data, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SDKException e) {
                    e.printStackTrace();
                    handleSDKExecption(e);
                }
            }
        }).start();
    }

    public void onFetchRemotePhone(View v){
        if(isDoubleClick()) return;
        appendMessageToContent("正在远程录音中...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                try {
                    json.put(Constant.KEY_CMD, Constant.CMD_FETCH_REMOTE_PHONE);
                    String hostname = mIp.getText().toString().trim();
                    json.put(Constant.KEY_HOSTNAME, hostname);
                    byte[] data = (json.toString()).getBytes();
                    if(mClientThread == null){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "请确认是否成功连接服务器!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return ;
                    }
                    TransferManager.getInstance().translate(Constant.CMD_FETCH_REMOTE_PHONE, data, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SDKException e) {
                    e.printStackTrace();
                    handleSDKExecption(e);
                }
            }
        }).start();
    }

    public void onStopRemotePhone(View v){
        if(isDoubleClick()) return;
        appendMessageToContent("中止远程录音操作...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                try {
                    json.put(Constant.KEY_CMD, Constant.CMD_STOP_REMOTE_PHONE);
                    String hostname = mIp.getText().toString().trim();
                    json.put(Constant.KEY_HOSTNAME, hostname);
                    byte[] data = (json.toString()).getBytes();
                    if(mClientThread == null){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "请确认是否成功连接服务器!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return ;
                    }
                    TransferManager.getInstance().translate(Constant.CMD_STOP_REMOTE_PHONE, data, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SDKException e) {
                    e.printStackTrace();
                    handleSDKExecption(e);
                }
            }
        }).start();
    }

    public void onFetchRemoteScreen(View v){
        if(isDoubleClick()) return;
        appendMessageToContent("正在远程录屏中...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                try {
                    json.put(Constant.KEY_CMD, Constant.CMD_FETCH_REMOTE_SCREEN);
                    String hostname = mIp.getText().toString().trim();
                    json.put(Constant.KEY_HOSTNAME, hostname);
                    byte[] data = (json.toString()).getBytes();
                    if(mClientThread == null){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "请确认是否成功连接服务器!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return ;
                    }
                    TransferManager.getInstance().translate(Constant.CMD_FETCH_REMOTE_SCREEN, data, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SDKException e) {
                    e.printStackTrace();
                    handleSDKExecption(e);
                }
            }
        }).start();
    }

    public void onStopRemoteScreen(View v){
        if(isDoubleClick()) return;
        appendMessageToContent("中止远程录屏操作...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                try {
                    json.put(Constant.KEY_CMD, Constant.CMD_STOP_REMOTE_SCREEN);
                    String hostname = mIp.getText().toString().trim();
                    json.put(Constant.KEY_HOSTNAME, hostname);
                    byte[] data = (json.toString()).getBytes();
                    if(mClientThread == null){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "请确认是否成功连接服务器!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return ;
                    }
                    TransferManager.getInstance().translate(Constant.CMD_STOP_REMOTE_SCREEN, data, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SDKException e) {
                    e.printStackTrace();
                    handleSDKExecption(e);
                }
            }
        }).start();
    }

    public void onFetchRemoteLocation(View v){
        if(isDoubleClick(500)) return;
        appendMessageToContent("获取远程设备位置信息...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                try {
                    json.put(Constant.KEY_CMD, Constant.CMD_FETCH_REMOTE_LOCATION);
                    String hostname = mIp.getText().toString().trim();
                    json.put(Constant.KEY_HOSTNAME, hostname);
                    byte[] data = (json.toString()).getBytes();
                    if(mClientThread == null){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "请确认是否成功连接服务器!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return ;
                    }
                    TransferManager.getInstance().translate(Constant.CMD_FETCH_REMOTE_LOCATION, data, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SDKException e) {
                    e.printStackTrace();
                    handleSDKExecption(e);
                }
            }
        }).start();
    }

    private void handleSDKExecption(final SDKException e){
        if(mClientThread != null){
            mClientThread.stopClient();
            mClientThread.interrupt();
            mClientThread = null;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
            String message = "通信异常，请重新连接服务器！！！！！！！！！\r\n"+
                    e.getMessage();
            appendMessageToContent(message);
            btnConnect.setText("连接服务器");
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
        if(mClientThread != null){
            mClientThread.stopClient();
            mClientThread.interrupt();
            mClientThread = null;
        }
    }

    private void appendMessageToContent(String message){
        mContent.append(message+ "\r\n");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 100);
    }

    private long startTime = 0;
    private boolean isDoubleClick(int max){
        long time = System.currentTimeMillis() - startTime;
        if(time > max){
            startTime = System.currentTimeMillis();
            return false;
        }else{
            return true;
        }
    }

    private boolean isDoubleClick(){
        long time = System.currentTimeMillis() - startTime;
        if(time > 1000){
            startTime = System.currentTimeMillis();
            return false;
        }else{
            return true;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 1, "文件预览");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case 0:
                onPreShowFileExplore();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onPreShowFileExplore(){
        Log.d(TAG, "onPreShowFileExplore: executed");
        if(Build.VERSION.SDK_INT < 25) {
            //获取父目录
            File file = new File(Constant.LOCAL_STORAGE);
            File parentFlie = new File(file.getParent());
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setDataAndType(Uri.fromFile(file), "*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivity(intent);
        }else{
            //获取到指定文件夹，这里为：/storage/emulated/0/Android/data/你的包	名/files/Download
            File file = new File(Constant.LOCAL_STORAGE);
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            //7.0以上跳转系统文件需用FileProvider，参考链接：https://blog.csdn.net/growing_tree/article/details/71190741
            Uri uri = FileProvider.getUriForFile(mContext,getPackageName()+ ".FileProvider", file);
            intent.setData(uri);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent,200);
        }
    }
}
