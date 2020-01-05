package Android.view.acore;


import android.util.Log;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private static final String TAG = ExampleUnitTest.class.getSimpleName();


    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
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
                    System.out.println("run: start to connect server...");
                    socket = new Socket();
                    socket.setSoTimeout(30*1000);
                    socket.connect(new InetSocketAddress("192.168.11.3", 30001));
                    System.out.println("run: server path="+ socket.getRemoteSocketAddress().toString()+
                            ", status="+ socket.isConnected());
                    is = socket.getInputStream();
                    os = socket.getOutputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                    System.out.println("run: hello to server...");
                    bw.write("hello server, this is client!");
                    bw.flush();
                    String data = null;
                    System.out.println("run: start to read data...");
                    while ((data=br.readLine()) != null){
                        System.out.println("run: receive data="+ data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void serverTest(){
        System.out.println("executed server test");
        mServerThread = new SocketThread();
        mServerThread.start();
    }

    private SocketThread mServerThread ;
    public class SocketThread extends Thread{

        private ServerSocket mServer = null;
        private Socket socket;
        private InputStream is;
        private OutputStream os;

        public SocketThread(){
        }
        @Override
        public void run() {
            try {
                mServer = new ServerSocket(30001);
                System.out.println("run: start to wait client connect...");
                socket = mServer.accept();
                if(socket == null){
                    System.out.println("run: connected fail" );
                    return;
                }
                System.out.println("run: connected success!");
                is = socket.getInputStream();
                os = socket.getOutputStream();
                int ret = -1;
                byte[] data = new byte[256];
                System.out.println("run: start to read data...");
                while ((ret=is.read(data)) != -1){
                    byte[] temp = new byte[ret];
                    System.arraycopy(data, 0, temp, 0, ret);
                    System.out.println("run: receive data="+ new String(temp));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if (is != null) is.close();
                    if(os != null) os.close();
                    if(socket != null) socket.close();
                    if(mServer != null) mServer.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

    } ;
}