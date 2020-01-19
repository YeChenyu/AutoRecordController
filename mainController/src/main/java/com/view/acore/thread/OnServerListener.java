package com.view.acore.thread;

/**
 * @author: yechenyu
 * @create: 2020/1/5 上午9:25
 * @email: Yecynull@163.com
 * @version:
 * @descripe:
 **/
public interface OnServerListener extends BaseListener {

    public void onCommand(String cmd, String data);
}
