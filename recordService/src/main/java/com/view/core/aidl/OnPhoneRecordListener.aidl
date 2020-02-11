// OnScreenRecordListener.aidl
package com.view.core.aidl;

// Declare any non-default types here with import statements

interface OnPhoneRecordListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onRecordStart();

    void onRecordStop(int code);

    void updateProgress(int current, int max, String message);

    void onRecordSuccess(String filePath);

    void onRecordError(int errCode, String message);
    /**
     * 通话结束时调用该方法，可防止通话时网络中断
     */
    void onPhoneIdel();
}
