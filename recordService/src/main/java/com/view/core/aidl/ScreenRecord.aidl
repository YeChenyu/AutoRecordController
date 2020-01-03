// ScreenRecord.aidl
package com.view.core.aidl;

// Declare any non-default types here with import statements
import com.view.core.aidl.OnScreenRecordListener;
import android.content.Intent;

interface ScreenRecord {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void initRecordService(in Bundle param, OnScreenRecordListener listener);

    boolean isReady();

    void startRecord();

    void stopRecord();

    void pauseRecord();

    void resumeRecord();

    void checkPermission();

}
