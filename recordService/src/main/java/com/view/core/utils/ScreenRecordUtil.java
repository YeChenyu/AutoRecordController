package com.view.core.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;

/**
 * @author: yechenyu
 * @create: 2019/12/30 下午8:56
 * @email: Yecynull@163.com
 * @version:
 * @descripe:
 **/
public class ScreenRecordUtil {

    private static final String TAG = ScreenRecordUtil.class.getSimpleName();

    private static Context mContext;
    private static ScreenRecordUtil INSTANCE;

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDpi;

    public static ScreenRecordUtil getInstance(){
        if(INSTANCE == null)
            INSTANCE = new ScreenRecordUtil();
        return INSTANCE;
    }

    /**
     * 录屏功能 5.0+ 的手机才能使用
     * @return
     */
    public static boolean isScreenRecordEnable(){

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ;

    }


    public boolean initScreenRecordManager(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        if(mScreenWidth==0 || mScreenHeight==0){
            mScreenWidth = 1080;
            mScreenHeight = 1920;
        }
        mScreenDpi = metrics.densityDpi;

        return true;
    }

    public boolean startRecordScreen(){

        return true;
    }


    public void destoryManager(){

    }

    /**
     * 获取打开摄像机的权限，录音，文件读写
     *
     * @param activity
     */
    public boolean checkPermission(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkPermission =
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                            + ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE)
                            + ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            + ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                //动态申请
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
                return false;
            } else {
                return true;
            }
        }
        return true;
    }


    public int getScreenWidth(){
        if(mScreenWidth == 0)
            return 1080;
        return mScreenWidth;
    }

    public int getScreenHeight(){
        if(mScreenHeight == 0){
            return 1920;
        }
        return mScreenHeight;
    }

    public int getScreenDpi(){
        return mScreenDpi;
    }
}
