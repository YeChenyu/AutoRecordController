package com.view.core.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author: yechenyu
 * @create: 2019/12/31 下午10:21
 * @email: Yecynull@163.com
 * @version:
 * @descripe:
 **/
public class ScreenRecordService extends Service {


    @Override
    public IBinder onBind(Intent intent) {
        return new ScreenRecordBinder(this);
    }
}
