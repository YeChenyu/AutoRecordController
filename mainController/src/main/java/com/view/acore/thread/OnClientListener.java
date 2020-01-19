package com.view.acore.thread;

/**
 * @author: yechenyu
 * @create: 2020/1/5 上午9:25
 * @email: Yecynull@163.com
 * @version:
 * @descripe:
 **/
public interface OnClientListener extends BaseListener {

    public void onStartConnect();
    public void onConnected(String host, int ip);
    public void onAuthenticateFailed();
    public void onAuthenticateSuccess();
    public void onCommand(String cmd, String json);
    public void onConnectionFailed(String message);
    public void onError(int errCode, String errMessage);
}
