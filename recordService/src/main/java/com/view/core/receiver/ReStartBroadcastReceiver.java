package com.view.core.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.view.core.MyApplication;
import com.view.core.services.SocketService;

/**
 * @author: yechenyu
 * @create: 2020/1/30 下午9:47
 * @email: Yecynull@163.com
 * @version:
 * @descripe:
 **/
public class ReStartBroadcastReceiver extends BroadcastReceiver {

//    private static final String TAG = ReStartBroadcastReceiver.class.getSimpleName();
    private static final String TAG = SocketService.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: intent="+ intent.getAction());
        Intent service = new Intent();
        service.setPackage(context.getPackageName());
        service.setClass(context, SocketService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service);
        } else {
            context.startService(service);
        }
    }
}
