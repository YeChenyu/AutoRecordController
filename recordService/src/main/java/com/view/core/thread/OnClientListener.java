package com.view.core.thread;

/**
 * @author: yechenyu
 * @create: 2020/1/5 上午9:25
 * @email: Yecynull@163.com
 * @version:
 * @descripe:
 **/
public interface OnClientListener extends BaseListener {

    public void onCommand(String cmd, int length, byte[] data);
}
