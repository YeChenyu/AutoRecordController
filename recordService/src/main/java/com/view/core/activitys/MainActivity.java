package com.view.core.activitys;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.auto.commonlibrary.exception.SDKException;
import com.auto.commonlibrary.transfer.TransferManager;
import com.view.core.MyApplication;
import com.view.core.aidl.OnPhoneRecordListener;
import com.view.core.aidl.OnScreenRecordListener;
import com.view.core.aidl.ScreenRecord;
import com.view.core.services.PhoneRecordService;
import com.view.core.services.ScreenRecordBinder;
import com.view.core.services.ScreenRecordService;
import com.view.core.thread.ClientThread;
import com.view.core.thread.Constant;
import com.view.core.utils.LocationUtil;
import com.view.core.utils.ScreenRecordUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.view.core.activitys.PhoneRecordActivity.PhoneServiceConnection;

import Android.view.core.R;

import static android.support.v4.content.PermissionChecker.PERMISSION_DENIED;


public class MainActivity extends Activity {

    private static final String TAG = ClientThread.class.getSimpleName();

    private Context mContext = MainActivity.this;
    private int REQUEST_SCREEN_PERMISSION_RESULT = 1;
    private int REQUEST_RECORD_PERMISSION_RESULT = 2;

    private String mPhoneFile;
    private String mScreenFile;

    private String remoteHost;

    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ScreenRecordUtil.getInstance().initScreenRecordManager(this);

        Bundle bundle = getIntent().getBundleExtra("data");
        if(bundle != null)
            remoteHost = bundle.getString(Constant.KEY_HOSTNAME);
        Log.d(TAG, "onCreate: remote hostname="+ remoteHost);

        if(requestPermission()){
            Log.d(TAG, "start phone service...");
            Intent intent = new Intent(this, PhoneRecordService.class);
            PhoneServiceConnection connection = getPhoneServiceConnection();
            if(connection != null){
                bindService(intent, connection, BIND_AUTO_CREATE);
            }
        }

        double[] location = LocationUtil.getInstance().getLocationInfo();
        if(location != null)
            Log.d(TAG, "onCreate: location="+ location[0]+ ", "+ location[1]);
        location = LocationUtil.getInstance().getLastLocationInfo();
        if(location != null)
            Log.d(TAG, "onCreate: last location="+ location[0]+ ", "+ location[1]);

        ((TextView)findViewById(R.id.content)).setText(getlocalip());
    }

    private PhoneServiceConnection getPhoneServiceConnection(){
        PhoneServiceConnection connection = ((MyApplication)getApplication()).getPhoneServiceConnection();
        return connection;
    }


    private ServiceConnection getScreenServiceConnection(){
        ServiceConnection connection = ((MyApplication)getApplication()).getScreenServiceConnection();
        if(connection == null){
            connection = new ServiceConnection() {
                private ScreenRecord binder;
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.d(TAG, "onServiceConnected: the screen service is binded");
                    binder = ScreenRecordBinder.asInterface(service);
                    Bundle param = new Bundle();
                    param.putString("KEY_FILE_PATH", "/mnt/sdcard/screen_record.mp4");
                    param.putInt("KEY_DISPLAY_WIDTH", ScreenRecordUtil.getInstance().getScreenWidth());
                    param.putInt("KEY_DISPLAY_HEIGHT", ScreenRecordUtil.getInstance().getScreenHeight());
                    param.putInt("KEY_DISPLAY_DPI", ScreenRecordUtil.getInstance().getScreenDpi());
                    param.putInt("KEY_RECORD_RESULT_CODE", mResultData);
                    param.putParcelable("KEY_RECORD_RESULT_DATA", mData);
                    try {
                        binder.initRecordService(param, mListener);
                        binder.startRecord();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onServiceDisconnected(ComponentName name) {
                    if(binder != null){
                        try {
                            binder.stopRecord();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            ((MyApplication)getApplication()).setScreenServiceConnection(connection);
        }
        return connection;
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
    public void onGetLocationInfo(View v){
        double[] location = LocationUtil.getInstance().getLocationInfo();
        if(location != null)
            Log.d(TAG, "onCreate: location="+ location[0]+ ", "+ location[1]);
        location = LocationUtil.getInstance().getLastLocationInfo();
        if(location != null)
            Log.d(TAG, "onCreate: last location="+ location[0]+ ", "+ location[1]);
    }

    private boolean requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED) {
                // put your code for Version>=Marshmallow
                return true;
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    Toast.makeText(this,
                            "App required access to audio", Toast.LENGTH_SHORT).show();
                }
                this.requestPermissions(new String[]{
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, REQUEST_RECORD_PERMISSION_RESULT);
                return false;
            }
        } else {
            // put your code for Version < Marshmallow
            return true;
        }
    }

    private OnPhoneRecordListener mPhoneRecordListener = new OnPhoneRecordListener() {
        @Override
        public void onRecordStart() throws RemoteException {
            Log.d(TAG, "onRecordStart: executed");
            ClientThread thread = ((MyApplication)getApplication()).getRemoteClient();
            if(thread != null) thread.hangUp(true);
        }

        @Override
        public void onRecordStop(int code) throws RemoteException {
            Log.d(TAG, "onRecordStop: code="+ code);
            Toast.makeText(mContext, "中止操作" ,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void updateProgress(int current, int max, String message) throws RemoteException {
            Log.d(TAG, "updateProgress: current="+ current);
        }

        @Override
        public void onRecordSuccess(String filePath) throws RemoteException {
            Log.d(TAG, "onRecordSuccess: path="+ filePath);
            mPhoneFile = filePath;

            if (ScreenRecordUtil.getInstance().isScreenRecordEnable()){
                MediaProjectionManager mediaProjectionManager
                        = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                if (mediaProjectionManager != null){
                    Log.d(TAG, "onRecordSuccess: request permission");
                    Intent intent = mediaProjectionManager.createScreenCaptureIntent();
                    PackageManager packageManager = getPackageManager();
                    if (packageManager.resolveActivity(intent,PackageManager.MATCH_DEFAULT_ONLY) != null){
                        //存在录屏授权的Activity
                        startActivityForResult(intent, REQUEST_SCREEN_PERMISSION_RESULT);
                    }else {
                        Toast.makeText(mContext, "权限获取失败！" ,Toast.LENGTH_SHORT).show();
                    }
                }
            }
            unbindService(getPhoneServiceConnection());
        }

        @Override
        public void onRecordError(int errCode, String message) throws RemoteException {
            Log.e(TAG, "onRecordError: code="+ errCode+ ", message="+ message );
        }

        @Override
        public void onPhoneIdel() throws RemoteException {
            try {
                ClientThread thread = ((MyApplication)getApplication()).getRemoteClient();
                if(thread != null) thread.hangUp(false);
                Thread.sleep(1000);
                uploadFile(mPhoneFile, Constant.TYPE_PHONE);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public IBinder asBinder() {
            return null;
        }
    };

    private Intent mData;
    private int mResultData;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: request="+ requestCode+ ", result="+ resultCode);
        if (requestCode == REQUEST_SCREEN_PERMISSION_RESULT && resultCode == Activity.RESULT_OK){
            mResultData = resultCode;
            mData = data;
            Log.d(TAG, "onActivityResult: start to screen service...");
            Intent intent = new Intent(this, ScreenRecordService.class);
            bindService(intent,  getScreenServiceConnection(), BIND_AUTO_CREATE);
        } else {
            Toast.makeText(this,"拒绝录屏", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_RECORD_PERMISSION_RESULT){
            Log.d(TAG, "start phone service...");
            Intent intent = new Intent(this, PhoneRecordService.class);
            bindService(intent,  getPhoneServiceConnection(), BIND_AUTO_CREATE);
        }else if(requestCode == REQUEST_SCREEN_PERMISSION_RESULT){
            Log.d(TAG, "onActivityResult: start to screen service...");
            Intent intent = new Intent(this, ScreenRecordService.class);
            bindService(intent,  getScreenServiceConnection(), BIND_AUTO_CREATE);
        }else{
            for (int temp : grantResults) {
                if (temp == PERMISSION_DENIED) {
                    break;
                }
            }
        }
    }


    private OnScreenRecordListener mListener = new OnScreenRecordListener() {
        @Override
        public void onRecordStart() throws RemoteException {

        }

        @Override
        public void onRecordStop(int code) throws RemoteException {
            Toast.makeText(mContext, "中止操作" ,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void updateProgress(int current, int max, String message) throws RemoteException {

        }

        @Override
        public void onRecordSuccess(String filePath) throws RemoteException {
            Log.d(TAG, "onRecordSuccess: file path="+ filePath);
            mScreenFile = filePath;
            try {
                ClientThread thread = ((MyApplication)getApplication()).getRemoteClient();
                if(thread != null) thread.hangUp(false);
                Thread.sleep(1000);
                uploadFile(mScreenFile, Constant.TYPE_SCREEN);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            unbindService(getScreenServiceConnection());
        }

        @Override
        public void onRecordError(int errCode, String message) throws RemoteException {

        }

        @Override
        public IBinder asBinder() {
            return null;
        }
    };

    private void uploadFile(String filePath, String fileType) throws JSONException {
        Log.d(TAG, "uploadFile: path="+ filePath+ ", type="+ fileType);
        File mFile = null;
        JSONObject json = new JSONObject();
        json.put(Constant.KEY_CMD, Constant.CMD_RETURN_REMOTE_DEVICE);
        json.put(Constant.KEY_HOSTNAME, remoteHost);
        if(filePath != null){
            mFile = new File(filePath);
            json.put(Constant.KEY_FILE, mFile.getName());
            json.put(Constant.KEY_LENGTH, mFile.length());
        }

        byte[] arrData = json.toString().getBytes();
        try {
            TransferManager.getInstance().translate(Constant.CMD_RETURN_REMOTE_DEVICE, arrData, null);
        } catch (SDKException e) {
            e.printStackTrace();
        }

        try {
            FileInputStream fis = new FileInputStream(mFile);
                int ret = -1;
                byte[] result = new byte[1024];
                while ((ret = fis.read(result)) != -1) {
                    TransferManager.getInstance().writeHexData(result);
                }
            Log.d(TAG, "uploadFile: upload success");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SDKException e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationUtil.getInstance().destoryLocationManager();
    }
}
