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
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.imuxuan.floatingview.FloatingView;
import com.view.core.MyApplication;
import com.view.core.services.MessageCenterService;
import com.view.core.services.SocketService;

import java.net.Socket;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

//        //左上角显示
//        Window window = getWindow();
//        window.setGravity(Gravity.START|Gravity.TOP);
//
//        //设置为1像素大小
//        WindowManager.LayoutParams params = window.getAttributes();
//        params.x = 0;
//        params.y = 0;
//        params.width = 10;
//        params.height = 10;
//        window.setAttributes(params);
//
//        ((TextView)findViewById(R.id.content)).setText(getlocalip());
//        ((MyApplication)getApplication()).SERVER_IP = ((EditText)findViewById(R.id.server_ip)).getText().toString();

//        finish();

        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
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
            startService(new Intent(mContext, SocketService.class));
        }
//        FloatingView.get().add();
    }

    @Override
    protected void onStop() {
        super.onStop();
        FloatingView.get().detach(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FloatingView.get().attach(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                startService(new Intent(mContext, SocketService.class));
            }
        }
    }


}
