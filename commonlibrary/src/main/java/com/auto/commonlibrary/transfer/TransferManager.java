package com.auto.commonlibrary.transfer;

import android.content.Context;

import com.auto.commonlibrary.exception.SDKException;

import java.net.Socket;

/**
 * @author: yechenyu
 * @create: 2020-02-18 00:09
 * @email: Yecynull@163.com
 * @version:
 * @descripe:
 **/
public abstract class TransferManager {

    private static TransferManager INSTANCE ;

    public static TransferManager getInstance(){
        if(INSTANCE == null){
            synchronized (TransferManager.class){
                INSTANCE = new TransferManagerImpl();
            }
        }
        return INSTANCE;
    }

    public abstract boolean initMasterDevice(Socket socket);

    public abstract boolean destoryDevice();

    public abstract boolean translate(String cmd, byte[] params, byte[] respCode) throws SDKException;

    public abstract boolean writeData(byte[] data) throws SDKException;

    public abstract byte[] read() throws SDKException;

    public abstract String readLine() throws SDKException;
}
