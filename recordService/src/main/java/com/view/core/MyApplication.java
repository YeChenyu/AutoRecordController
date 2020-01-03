package com.view.core;

import android.app.Application;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.util.Log;

import com.view.core.activitys.MainActivity;
import com.view.core.utils.LocationUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author: yechenyu
 * @create: 2019/12/25 下午8:53
 * @email: Yecynull@163.com
 * @version:
 * @descripe:
 **/
public class MyApplication extends Application {

    private static final String TAG = MyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
//        PackageManager pm = getPackageManager();
//        ComponentName component = new ComponentName(getApplicationContext(), MainActivity.class);
//        pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//                PackageManager.DONT_KILL_APP);

        LocationUtil.getInstance().initLocationManager(this);
    }

    private SocketThread mThread ;
    public class SocketThread extends Thread{

        private ServerSocket mServer = null;
        private Socket socket;
        private InputStream is;
        private OutputStream os;

        public SocketThread(){
            try {
                mServer = new ServerSocket(30001);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            try {
                Log.d(TAG, "run: start to wait client connect...");
                socket = mServer.accept();
                if(socket == null){
                    Log.e(TAG, "run: connected fail" );
                    return;
                }
                Log.d(TAG, "run: connected success!");
                is = socket.getInputStream();
                os = socket.getOutputStream();
                int ret = -1;
                byte[] data = new byte[256];
                Log.d(TAG, "run: start to read data...");
                while ((ret=is.read(data)) != -1){
                    byte[] temp = new byte[ret];
                    System.arraycopy(data, 0, temp, 0, ret);
                    Log.d(TAG, "run: receive data="+ new String(temp));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if (is != null) is.close();
                    if(os != null) os.close();
                    if(socket != null) socket.close();
                    if(mServer != null) mServer.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

    } ;


}
