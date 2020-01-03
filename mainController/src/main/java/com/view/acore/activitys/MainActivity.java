package com.view.acore.activitys;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import Android.view.acore.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mThread = new ClientThread();
        mThread.start();
    }

    private ClientThread mThread ;
    public class ClientThread extends Thread{

        private Socket socket;
        private InputStream is;
        private OutputStream os;

        public ClientThread(){

        }

        @Override
        public void run() {
            if(socket == null) {
                try {
                    Log.d(TAG, "run: start to connect server...");
                    socket = new Socket("192.168.0.11",  30001);
                    socket.setSoTimeout(10*1000);
                    Log.d(TAG, "run: server path="+ socket.getRemoteSocketAddress().toString());
                    is = socket.getInputStream();
                    os = socket.getOutputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                    Log.d(TAG, "run: hello to server...");
                    bw.write("hello server, this is client!");
                    bw.flush();
                    String data = null;
                    Log.d(TAG, "run: start to read data...");
                    while ((data=br.readLine()) != null){
                        Log.d(TAG, "run: receive data="+ data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mThread != null)
            mThread.interrupt();
        mThread = null;
    }
}
