package com.view.core;

import android.app.Application;
import android.content.ComponentName;
import android.content.pm.PackageManager;

import com.view.core.activitys.MainActivity;

/**
 * @author: yechenyu
 * @create: 2019/12/25 下午8:53
 * @email: Yecynull@163.com
 * @version:
 * @descripe:
 **/
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PackageManager pm = getPackageManager();
        ComponentName component = new ComponentName(getApplicationContext(), MainActivity.class);
        pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
