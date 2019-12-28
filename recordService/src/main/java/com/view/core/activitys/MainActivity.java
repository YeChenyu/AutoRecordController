package com.view.core.activitys;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;

import com.view.core.services.PhoneService;


public class MainActivity extends Activity {
    private Button button;
    private PhoneService recordService;
    public static MyServiceConnection myServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //button = findViewById(R.id.bu_1);
        recordService = new PhoneService();
        myServiceConnection = new MyServiceConnection();
        Intent intent = new Intent(this, PhoneService.class);
        //startService(intent);
        //绑定服务
        bindService(intent, myServiceConnection, BIND_AUTO_CREATE);
    }

    public void click1(View view) {
        String tempContent = button.getText().toString();
        System.out.println(tempContent);
        if (tempContent.equals("开启监听")) {
            button.setText("关闭监听");
            recordService = new PhoneService();
            myServiceConnection = new MyServiceConnection();
            Intent intent = new Intent(this, PhoneService.class);
            //绑定服务
            bindService(intent, myServiceConnection, BIND_AUTO_CREATE);

        } else {
            button.setText("开启监听");
            //解绑服务
            unbindService(myServiceConnection);
        }
    }

    private class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

}
