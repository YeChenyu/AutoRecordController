// ScreenRecord.aidl
package com.view.core.aidl;

// Declare any non-default types here with import statements
import com.view.core.aidl.OnPhoneRecordListener;

interface PhoneRecord {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void initRecordService(in Bundle param, OnPhoneRecordListener listener);

    boolean isReady();

    void stopRecord();

    void pauseRecord();

    void resumeRecord();

}
