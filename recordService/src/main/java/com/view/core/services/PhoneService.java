package com.view.core.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author  why
 *
 */
public class PhoneService extends Service {

    private static final String TAG = PhoneService.class.getSimpleName();


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return new PhoneRecordBinder(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
