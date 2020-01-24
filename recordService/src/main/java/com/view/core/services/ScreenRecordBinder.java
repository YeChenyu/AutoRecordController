package com.view.core.services;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.view.core.aidl.OnScreenRecordListener;
import com.view.core.aidl.ScreenRecord;
import com.view.core.utils.CommonUtil;
import com.view.core.utils.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * @author: yechenyu
 * @create: 2019/12/30 下午11:32
 * @email: Yecynull@163.com
 * @version:
 * @descripe:
 **/
public class ScreenRecordBinder extends ScreenRecord.Stub implements Handler.Callback {

    private static final String TAG = ScreenRecordService.class.getSimpleName();
    private Context mContext ;
    private OnScreenRecordListener mListener;

    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private MediaRecorder mMediaRecorder;
    private VirtualDisplay mVirtualDisplay;

    private boolean mIsRunning;
    private int mRecordWidth;
    private int mRecordHeight = CommonUtil.getScreenHeight();
    private int mScreenDpi = CommonUtil.getScreenDpi();


    private int mResultCode;
    private Intent mResultData;

    //录屏文件的保存地址
    private String mRecordFilePath;

    private Handler mHandler;
    //已经录制多少秒了
    private int mRecordSeconds = 0;

    private static final int MSG_TYPE_COUNT_DOWN = 110;

    public ScreenRecordBinder(Context context){
        mContext = context;

        mIsRunning = false;
        mMediaRecorder = new MediaRecorder();
        mHandler = new Handler(Looper.getMainLooper(),this);
    }

    @Override
    public void initRecordService(Bundle param, OnScreenRecordListener listener) throws RemoteException {
        mListener = listener;

        if(param.containsKey("KEY_FILE_PATH")){
            mRecordFilePath = param.getString("KEY_FILE_PATH");
        }else{
            mRecordFilePath = getSaveDirectory()  + File.separator+  System.currentTimeMillis() + ".mp4";
        }
        Log.d(TAG, "initRecordService: path="+ mRecordFilePath);
        if(param.containsKey("KEY_DISPLAY_WIDTH")){
            mRecordWidth = param.getInt("KEY_DISPLAY_WIDTH");
        }else{
            mRecordWidth = 256;
        }
        Log.d(TAG, "initRecordService: width="+ mRecordWidth);
        if(param.containsKey("KEY_DISPLAY_HEIGHT")){
            mRecordHeight = param.getInt("KEY_DISPLAY_HEIGHT");
        }else{
            mRecordHeight = 768;
        }
        Log.d(TAG, "initRecordService: height="+ mRecordHeight);
        if(param.containsKey("KEY_DISPLAY_DPI")){
            mScreenDpi = param.getInt("KEY_DISPLAY_DPI");
        }else{
            mScreenDpi = 256;
        }
        Log.d(TAG, "initRecordService: dpi="+ mScreenDpi);

        if(param.containsKey("KEY_RECORD_RESULT_CODE")) {
            mResultCode = param.getInt("KEY_RECORD_RESULT_CODE");
        }
        if(param.containsKey("KEY_RECORD_RESULT_DATA")) {
            mResultData = (Intent) param.getParcelable("KEY_RECORD_RESULT_DATA");
        }

        if(mResultCode<0 || mResultData==null){
            mListener.onRecordError(-1, "the result data is null!");
            return ;
        }

        if (mMediaProjection == null){
            mMediaProjection = mProjectionManager.getMediaProjection(mResultCode,mResultData);
        }
        mProjectionManager = (MediaProjectionManager) mContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mProjectionManager != null){
            mMediaProjection = mProjectionManager.getMediaProjection(mResultCode, mResultData);
        }

        if (mMediaRecorder == null){
            mMediaRecorder = new MediaRecorder();
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile( mRecordFilePath );
        mMediaRecorder.setVideoSize(mRecordWidth, mRecordHeight);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setVideoEncodingBitRate((int) (mRecordWidth * mRecordHeight * 3.6));
        mMediaRecorder.setVideoFrameRate(20);

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mVirtualDisplay = mMediaProjection.createVirtualDisplay("MainScreen", mRecordWidth, mRecordHeight, mScreenDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mMediaRecorder.getSurface(), null, null);
    }

    private String getSaveDirectory() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            return null;
        }
    }


    @Override
    public boolean isReady() throws RemoteException {
        return  mMediaProjection != null && mResultData != null;
    }

    @Override
    public void startRecord() throws RemoteException {
        if ( mIsRunning) {
            mListener.onRecordError(0, "the server is running now!");
            return ;
        }
        mMediaRecorder.start();
        //最多录制三分钟
        mHandler.sendEmptyMessageDelayed(MSG_TYPE_COUNT_DOWN,1000);
        mIsRunning = true;
        mListener.onRecordStart();
    }


    @Override
    public void stopRecord() throws RemoteException {
        if (!mIsRunning) {
            mListener.onRecordError(0, "the server is running now!");
            return ;
        }
        mIsRunning = false;

        try {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder = null;
            mVirtualDisplay.release();
            mMediaProjection.stop();
        }catch (Exception e){
            e.printStackTrace();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }

        mMediaProjection = null;
        mHandler.removeMessages(MSG_TYPE_COUNT_DOWN);
        mListener.onRecordStop(0);

        if (mRecordSeconds <= 2 ){
            FileUtil.deleteSDFile(mRecordFilePath);
        }else {
            //通知系统图库更新
            FileUtil.fileScanVideo(mContext, mRecordFilePath,mRecordWidth,mRecordHeight,mRecordSeconds);
        }
        mRecordSeconds = 0;

        return ;
    }

    @Override
    public void pauseRecord() throws RemoteException {
        if (mMediaRecorder != null ){
            if (Build.VERSION.SDK_INT >= 24) {
                mMediaRecorder.pause();
            }
        }
    }

    @Override
    public void resumeRecord() throws RemoteException {
        if (mMediaRecorder != null ){
            if (Build.VERSION.SDK_INT >= 24) {
                mMediaRecorder.resume();
            }
        }
    }


    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case MSG_TYPE_COUNT_DOWN:{
                String str = null;
                boolean enough = FileUtil.getSDFreeMemory() / (1024* 1024) < 4;
                if (enough){
                    //空间不足，停止录屏
                    try {
                        mListener.onRecordError(-2, "存储空间不足！");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    mRecordSeconds = 0;
                    break;
                }

                mRecordSeconds++;
                int minute = 0, second = 0;
                if (mRecordSeconds >= 60 ){
                    minute = mRecordSeconds / 60;
                    second = mRecordSeconds % 60;
                } else {
                    second = mRecordSeconds;
                }
                try {
                    mListener.updateProgress(mRecordSeconds, 180, "" );
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                if (mRecordSeconds < 3 * 60 ){
                    mHandler.sendEmptyMessageDelayed(MSG_TYPE_COUNT_DOWN,1000);
                } else if (mRecordSeconds == 3 * 60 ){
                    try {
                        mListener.onRecordSuccess(mRecordFilePath);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    mRecordSeconds = 0;
                }

                break;
            }
        }
        return true;
    }

    @Override
    public void checkPermission() throws RemoteException {

    }
}
