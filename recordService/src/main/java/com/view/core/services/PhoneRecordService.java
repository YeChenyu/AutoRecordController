package com.view.core.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * @author  why
 *
 */
public class PhoneRecordService extends Service {

    private static final String TAG = PhoneRecordService.class.getSimpleName();

    PhoneRecordBinder binder = null;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        binder= new PhoneRecordBinder(this);
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if(binder != null){
            try {
                binder.stopRecord();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
