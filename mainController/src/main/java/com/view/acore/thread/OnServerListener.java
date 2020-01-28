package com.view.acore.thread;

/**
 * @author:  xxx
 * @create: 2020/1/5 上午9:25
 * @email:  xxx.xxx.xxx
 * @version:
 * @descripe:
 **/
public interface OnServerListener extends BaseListener {

    public void onCommand(String cmd, String data);
}
