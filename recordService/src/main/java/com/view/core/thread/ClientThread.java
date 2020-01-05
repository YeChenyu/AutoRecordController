package com.view.core.thread;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.view.core.MyApplication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * @author: yechenyu
 * @create: 2020/1/5 上午9:24
 * @email: Yecynull@163.com
 * @version:
 * @descripe:
 **/
public class ClientThread extends Thread {

    private static final String TAG = ClientThread.class.getSimpleName();

    private Context mContext;
    private OnClientListener mListener;

    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private static final String AUTH_STRING = "1234567890\r\n";

    public ClientThread(Context mContext, OnClientListener mListener) {
        this.mContext = mContext;
        this.mListener = mListener;
    }

    @Override
    public void run() {
        if(socket == null) {
            while (true) {
                try {
                    System.out.println("run: start to connect server...");
                    socket = new Socket();
                    socket.setSoTimeout(MyApplication.SERVER_CONNECT_TIMEOUT);
                    socket.connect(new InetSocketAddress(MyApplication.SERVER_IP, MyApplication.SERVER_PORT));
                    SocketAddress address = socket.getRemoteSocketAddress();
                    if (address != null) {
                        Log.d(TAG, "run: server path=" + address.toString() +
                                ", status=" + socket.isConnected());
                        Toast.makeText(mContext, "Server: " + address.toString(), Toast.LENGTH_SHORT).show();
                    }
                    if(!socket.isConnected()){
                        Log.e(TAG, "run: connect failed!" );
                        socket.close();
                        socket = null;
                        continue;
                    }
                    is = socket.getInputStream();
                    os = socket.getOutputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                    System.out.println("run: hello to server...");
                    bw.write(AUTH_STRING);
                    bw.flush();
                    System.out.println("run: start to read auth...");
                    String auth = br.readLine();
                    if(auth == null || auth.equals(AUTH_STRING)){
                        Log.d(TAG, "run: auth failed!");
                        if(is != null)is.close();
                        if(os != null)os.close();
                        socket.close();
                        socket = null;
                        continue;
                    }
                    String data = null;
                    Log.d(TAG, "run: start to read data...");
                    while ((data = br.readLine()) != null) {
                        System.out.println("run: receive data=" + data);
                        parseCommand(data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        if(is != null)is.close();
                        if(os != null)os.close();
                        if(!socket.isInputShutdown())socket.shutdownInput();
                        if(!socket.isOutputShutdown())socket.isOutputShutdown();
                        socket.close();
                        socket = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void parseCommand(String data){
        if(data != null){
            if(data.equals("123")){
                mListener.onCommand("123", 0, null);
            }else{

            }
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
