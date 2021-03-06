package Android.view.core;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private static final String TAG = ExampleInstrumentedTest.class.getSimpleName();


    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("Android.view.core.test", appContext.getPackageName());


    }

    @Test
    public void clientTest(){
        System.out.println("executed client test");
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
                    socket = new Socket("10.131.252.138", 3303);
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
}
