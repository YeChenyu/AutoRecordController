package com.auto.commonlibrary.transfer;

import android.util.Log;

import com.auto.commonlibrary.exception.SDKException;
import com.auto.commonlibrary.util.StringUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 */
public class TransferManagerImpl extends TransferManager {

    private final String TAG = "Transfer";

    private static final long OVERTIME = 10*1000;
    private static final int SLEEP_TIME = 1;//修改读取是的延时时间为1ms
    private boolean isLogined = false;

    private Socket mSocket = null;
    private InputStream is;
    private BufferedReader mInputStream;
    private BufferedWriter mOutputStream;
    private HandleProtocol handleProtocol = new HandleProtocol();
//    private Logger LOG = Logger.getLogger(TransferManager.class);

    public TransferManagerImpl() { }

    private static TransferManagerImpl self = null;

    public static TransferManagerImpl getInstance() {

        if (self == null) {
            synchronized (TransferManagerImpl.class) {
                if (self == null) {
                    self = new TransferManagerImpl();
                }
            }
        }
        return self;
    }
    /**
     * 连接K21安全模块
     *
     * @return
     */
    @Override
    public boolean initMasterDevice(Socket socket) {
//        LOG.trace("initMasterDevice executed");
        if(socket == null){
//            LOG.error("socket is null");
            return false;
        }
        try {
            mSocket = socket;
            if(!testCommunicate()){
//                LOG.error("the connection is unactive");
                return false;
            }
            is = socket.getInputStream();
            mInputStream = new BufferedReader(new InputStreamReader(is));
            mOutputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean destoryDevice() {

        return false;
    }

    /**
     * 与K21通信的通用接口
     *
     * @param cmd     指令号
     * @param params  参数
     * @param respCode 响应码
     * @return
     * @throws SDKException
     */
    @Override
    public synchronized boolean translate(String cmd, byte[] params, byte[] respCode) throws SDKException {
        Log.d(TAG, "translate executed");
        if(!login()){
            throw new SDKException(SDKException.COMMUNICATE_ERROR_IO_ERROR);
        }
        // 封装请求报文
        byte[] arrCmd = StringUtil.hexStr2Bytes(cmd);
        byte[] requestData = null;
        if(respCode == null){
            requestData = handleProtocol.packRequestProtocol(arrCmd, params);
        }else{
            requestData = handleProtocol.packResponseProtocol(arrCmd, params, respCode);
        }
        try {
            write(requestData);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            throw new SDKException(SDKException.COMMUNICATE_ERROR_IO_ERROR);
        }
    }

    @Override
    public boolean writeHexData(byte[] data) throws SDKException {
        if (mOutputStream != null) {
            try {
                mOutputStream.flush();
                Log.d(TAG, "write:" + StringUtil.byte2HexStr(data));
                mOutputStream.write(StringUtil.byte2HexStr(data)+ "\n");
                mOutputStream.flush();
                return true;
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public byte[] read() throws SDKException {
        if(is == null){
            Log.e(TAG, "readLine: please execute initMasterDevice first");
            return null;
        }
        try {
            byte[] data = new byte[1024];
            int ret = is.read(data);
            if(ret > 0){
                byte[] result = new byte[ret];
                System.arraycopy(data, 0, result, 0, ret);
                Log.d(TAG, "read "+ ret+ " data ="+ StringUtil.byteToStr(result));
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int read(byte[] data, int off, int length) throws SDKException{
        if(is == null){
            Log.e(TAG, "readLine: please execute initMasterDevice first");
            return -2;
        }
        try {
            return is.read(data, off, length);
        } catch (IOException e) {
            e.printStackTrace();
            return -3;
        }
    }

    @Override
    public String readLine() throws SDKException {
        if(mInputStream == null){
            Log.e(TAG, "readLine: please execute initMasterDevice first");
            return null;
        }
        while (true) {
            try {
                String data = null;
                if ((data = mInputStream.readLine()) != null) {
                    Log.d(TAG, "readLine: read="+ data);
                    RespResult result = handleProtocol.unPackageProtocol(StringUtil.hexStr2Bytes(data));
                    Log.d(TAG, "readLine: reault="+ result.toString());
                    if(result.getExecuteId() == 0){
                        String respCode = result.getRespCode();
                        byte cmdType = result.getCmdType();
                        if(cmdType==0x2f && respCode.length()==2){
                            if(respCode.equals("00")){
                                byte[] param = result.getParams();
                                if(param != null){
                                    return new String(param);
                                }
                            }
                        }else{
                            byte[] param = result.getParams();
                            if(param != null){
                                return new String(param);
                            }
                        }
                    }
                    return null;
                }
                Thread.sleep(100);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public String readDataLine() throws SDKException{
        if(mInputStream == null){
            Log.e(TAG, "readLine: please execute initMasterDevice first");
            return null;
        }
        try {
            return mInputStream.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void write(byte[] data) throws IOException {
        if (mOutputStream != null) {
            mOutputStream.flush();
            Log.d(TAG, "write:"+ StringUtil.byte2HexStr(data));
            mOutputStream.write(StringUtil.byte2HexStr(data)+ "\n");
            mOutputStream.flush();
        }
    }

    /**
     * 登录设备 未抛出异常代表登录成功
     */
    private synchronized boolean login() throws SDKException {
        if (isLogined) {
            return true;
        }
        //初始化配置log4j
//        Log4jUtil.configure();

        //通信测试
        return testCommunicate();
    }

    /**
     * 测试通信
     *
     * @return
     */
    public boolean testCommunicate() {
        if(mSocket != null) {
            try {
                mSocket.sendUrgentData(0xff);
                isLogined = true;
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                isLogined = false;
                return false;
            }
        }
        isLogined = false;
        return false;
    }
}
