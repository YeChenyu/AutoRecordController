package com.view.core.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.view.core.MyApplication;

import Android.view.core.R;

/**
 * @author: xxx
 * @create: 2020-02-12 20:41
 * @email: xxx.xxx.xxx
 * @version:
 * @descripe:
 **/
public class FloatViewUtil {

    public static final int FLOAT_SIZE = 1;//悬浮窗大小
    private static FloatViewUtil INSTANCE ;

    public static FloatViewUtil getInstance(){
        if(INSTANCE == null){
            INSTANCE = new FloatViewUtil();
        }
        return INSTANCE;
    }

    public void showFloatingWindow(Context context) {
        if (Settings.canDrawOverlays(context)) {
            // 获取WindowManager服务
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

            View v = LayoutInflater.from(context).inflate(R.layout.float_activity, null);

            // 设置LayoutParam
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            // FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
            // FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按,不设置这个flag的话，home页的划屏会有问题
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

            layoutParams.format = PixelFormat.RGBA_8888;
            layoutParams.gravity = Gravity.START | Gravity.TOP;
            layoutParams.width = FLOAT_SIZE;
            layoutParams.height = FLOAT_SIZE;
            layoutParams.x = 0;
            layoutParams.y = 0;

            // 将悬浮窗控件添加到WindowManager
            windowManager.addView(v, layoutParams);
        }
    }

    public boolean checkFloatPermission(Activity activity){
        if (!Settings.canDrawOverlays(activity)) {
            Toast.makeText(activity, "当前无权限，请授权", Toast.LENGTH_SHORT);
            activity.startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName())), 0);
            return false;
        }else{
            return true;
        }
    }
}
