package com.view.acore.thread;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author:  xxx
 * @create: 2020/1/5 上午9:24
 * @email:  xxx.xxx.xxx
 * @version:
 * @descripe:
 **/
public class ServerThread extends Thread {

    private static final String TAG = ServerThread.class.getSimpleName();

    private Context mContext;
    private OnServerListener mListener;

    public static final String SERVER_IP = "192.168.11.3";
    public static final int SERVER_PORT = 4401;
    public static final int SERVER_CONNECT_TIMEOUT = 10*1000;

    private ServerSocket mServer;
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private static final String AUTH_STRING = "1234567890";

    public ServerThread(Context mContext, OnServerListener mListener) {
        this.mContext = mContext;
        this.mListener = mListener;
    }

    @Override
    public void run() {
        while (true) {
            try {
                mServer = new ServerSocket(SERVER_PORT);
                Log.d(TAG,"run: start to wait client connect...");
                mServer.setSoTimeout(SERVER_CONNECT_TIMEOUT);
                socket = mServer.accept();
                if (socket == null) {
                    Log.d(TAG,"run: connected fail");
                    mServer.close();
                    mServer = null;
                    continue;
                }
                Log.d(TAG, "run: connected success!");
                is = socket.getInputStream();
                os = socket.getOutputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

                Log.d(TAG, "run: start to read auth...");
                String auth = br.readLine();
                if(auth == null || auth.equals(AUTH_STRING)){
                    Log.d(TAG, "run: auth failed!");
                    if(is != null)is.close();
                    if(os != null)os.close();
                    socket.close();
                    socket = null;
                    mServer.close();
                    mServer = null;
                    continue;
                }
                Log.d(TAG, "run: auth success, back data to client");
                bw.write(auth);
                bw.flush();

                Log.d(TAG, "run: start to read data...");
                String preData = null;
                while ((preData = br.readLine()) != null) {
                    System.out.println("run: receive data=" + preData);
                    parseCommand(preData);
                }

                int ret = -1;
                byte[] data = new byte[256];
                Log.d(TAG, "run: start to read data...");
                while ((ret = is.read(data)) != -1) {
                    byte[] temp = new byte[ret];
                    System.arraycopy(data, 0, temp, 0, ret);
                    Log.d(TAG,"run: receive data=" + new String(temp));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) is.close();
                    if (os != null) os.close();
                    if (socket != null) socket.close();
                    if (mServer != null) mServer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void parseCommand(String data){
        if(data != null){
            mListener.onCommand("123", data);
        }
    }


    public void writeData(byte[] data, int length){
        if(os != null){
            try {
                os.write(data, 0, length);
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
