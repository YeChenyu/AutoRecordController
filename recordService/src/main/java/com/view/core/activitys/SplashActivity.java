package com.view.core.activitys;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.view.core.MyApplication;
import com.view.core.services.SocketService;
import com.view.core.thread.Constant;
import com.view.core.utils.FloatViewUtil;

import Android.view.core.R;

/**
 * @author:  xxx
 * @create: 2020/1/2 下午9:15
 * @email:  xxx.xxx.xxx
 * @version:
 * @descripe:
 **/
public class SplashActivity extends Activity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private Context mContext = this;
    private Intent service = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        ((TextView)findViewById(R.id.content)).setText("本机IP："+ getlocalip());
        ((EditText)findViewById(R.id.server_ip)).setText(Constant.SERVER_IP);

//        if (FloatViewUtil.getInstance().checkFloatPermission(this)) {
//            //启动服务
//            ((MyApplication)getApplication()).startSocketService();
//            //退出桌面
//            Intent home = new Intent(Intent.ACTION_MAIN);
//            home.addCategory(Intent.CATEGORY_HOME);
//            startActivity(home);
//        }
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
//        ((MyApplication)getApplication()).SERVER_IP = ((EditText)findViewById(R.id.server_ip)).getText().toString();
//        ((MyApplication)getApplication()).getRemoteClient();

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "当前无权限，请授权", Toast.LENGTH_SHORT);
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())), 0);
        } else {
            service = new Intent(this, SocketService.class);
            startService(service);
        }
    }
    public void closeClient(View v){
//        ((MyApplication)getApplication()).SERVER_IP = ((EditText)findViewById(R.id.server_ip)).getText().toString();
//        ((MyApplication)getApplication()).getRemoteClient();

        if(service != null){
            Toast.makeText(mContext, "断开连接...", Toast.LENGTH_SHORT).show();
            stopService(service);
            service = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                ((MyApplication)getApplication()).startSocketService();
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.addCategory(Intent.CATEGORY_HOME);
                startActivity(home);
            }
        }
    }


}
