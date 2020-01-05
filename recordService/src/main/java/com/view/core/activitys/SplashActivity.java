package com.view.core.activitys;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.view.core.MyApplication;

import Android.view.core.R;

/**
 * @author: yechenyu
 * @create: 2020/1/2 下午9:15
 * @email: Yecynull@163.com
 * @version:
 * @descripe:
 **/
public class SplashActivity extends Activity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        ((TextView)findViewById(R.id.content)).setText(getlocalip());
        ((MyApplication)getApplication()).SERVER_IP = ((EditText)findViewById(R.id.server_ip)).getText().toString();

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

    public void startClient(View v){
        ((MyApplication)getApplication()).SERVER_IP = ((EditText)findViewById(R.id.server_ip)).getText().toString();
        ((MyApplication)getApplication()).getRemoteClient();
    }
}
