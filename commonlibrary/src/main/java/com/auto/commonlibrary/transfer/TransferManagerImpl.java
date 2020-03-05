package com.auto.commonlibrary.transfer;

import android.util.Log;

import com.auto.commonlibrary.exception.SDKException;
import com.auto.commonlibrary.util.StringUtil;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
    private InputStream mInputStream;
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
            mInputStream = socket.getInputStream();
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
     * @param timeout 超时时间，单位:ms
     * @return
     * @throws SDKException
     */
    @Override
    public synchronized RespResult translate(byte[] cmd, byte[] params, int timeout, byte type) throws SDKException {
//        LOG.trace("translate executed");
        if(!login()){
            throw new SDKException(SDKException.COMMUNICATE_ERROR_IO_ERROR);
        }
        // 封装请求报文
        byte[] requestData = handleProtocol.packRequestProtocol(cmd, params, type);
        try {
            write(requestData);
//            RespResult result = readData(cmd, timeout);
//            int ret = result.getExecuteId();
//            if (ret == 0) {
                return null;
//            } else if (ret == -1) {
//                throw new SDKException(SDKException.COMMUNICATE_ERROR_BACK_CMD_ERROR);
//            } else if (ret == -2) {
//                throw new SDKException(SDKException.COMMUNICATE_ERROR_LRC_ERROR);
//            } else {
//                throw new SDKException(SDKException.COMMUNICATE_ERROR_OTHER);
//            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new SDKException(SDKException.COMMUNICATE_ERROR_IO_ERROR);
        }
    }

    private void write(byte[] data) throws IOException {
//        if (mInputStream != null) {
//            while (mInputStream.available() > 0)
//                mInputStream.read();
//        }
        if (mOutputStream != null) {
            mOutputStream.flush();
//            byte[] temp = new byte[data.length+1];
//            System.arraycopy(data, 0, temp, 0, data.length);
//            temp[data.length] = 0x0d;
            Log.d(TAG, "write:"+ StringUtil.byte2HexStr(data));
            mOutputStream.write(StringUtil.byte2HexStr(data)+ "\n");
            mOutputStream.flush();
        }
    }



    private RespResult readData(byte[] cmd, int timeout) throws SDKException, IOException {
        // 取STX
//        byte[] STXBuff = new byte[128];
        byte[] STXBuff = new byte[]{(byte) 0x33};
        int stxRet = read(STXBuff, STXBuff.length, timeout < OVERTIME ? OVERTIME : timeout);
//        Log.d(TAG, "readData: "+ StringUtil.byte2HexStr(STXBuff));
        if (stxRet != 0) {
            if (stxRet == 2) {
                isLogined = false;//K21挂掉时，设置为需要重新登录
                throw new SDKException(SDKException.COMMUNICATE_ERROR_TIMEOUT);
            }
            if (stxRet == 4) {
                throw new SDKException(SDKException.COMMUNICATE_ERROR_IO_ERROR);
            }
            if (stxRet == 3) {
                throw new SDKException(SDKException.COMMUNICATE_ERROR_STOP);
            }
        }
        if (STXBuff[0] != 0x02) {
            Log.d(TAG,"stx 首字节读到0, 进行第二次读取...");
            stxRet =  read(STXBuff, STXBuff.length, OVERTIME);
            if (stxRet != 0) {
                if (stxRet == 2) {
                    isLogined = false;//K21挂掉时，设置为需要重新登录
                    throw new SDKException(SDKException.COMMUNICATE_ERROR_TIMEOUT);
                }
                if (stxRet == 4) {
                    throw new SDKException(SDKException.COMMUNICATE_ERROR_IO_ERROR);
                }
                if (stxRet == 3) {
                    throw new SDKException(SDKException.COMMUNICATE_ERROR_STOP);
                }
            }
        }

        if (STXBuff[0] == 0x02) {
            byte[] dataLenBuff = new byte[2];
            int dataLenRet = read(dataLenBuff, dataLenBuff.length, 2 * 1000);
            if (dataLenRet != 0) {
                throw new SDKException(SDKException.COMMUNICATE_ERROR_TIMEOUT);
            }
            int dataLen = Integer.valueOf(StringUtil.byte2HexStr(dataLenBuff));
            byte[] dataBuff = new byte[dataLen + 2];
            int dataRet = read(dataBuff, dataBuff.length, 2 * 1000);
            if (dataRet != 0) {
                throw new SDKException(SDKException.COMMUNICATE_ERROR_TIMEOUT);
            }

            if (dataBuff[dataBuff.length - 2] == 0x03) {
                RespResult result = handleProtocol.unPackageProtocol(dataBuff, cmd);
                return result;
            } else {
                throw new SDKException(SDKException.COMMUNICATE_ERROR_ETX_ERROR);
            }
        } else {
            throw new SDKException(SDKException.COMMUNICATE_ERROR_STX_ERROR);
        }
    }

    public int read(byte recvBuf[], int recvLen, long waitTime) throws IOException {
        long lBeginTime = System.currentTimeMillis();// 更新当前秒计数
        long lCurrentTime = 0;
        int nRet = 0;
        int nReadedSize = 0;
        if (mInputStream == null) {
            return 4;
        }
        while (true) {

            if (mInputStream.available() > 0) {
//                BufferedReader br = new BufferedReader(new InputStreamReader(mInputStream));
//                String str = br.readLine();
//                Log.d(TAG, "readLine: data="+ StringUtil.str2HexStr(str));
                nRet = mInputStream.read(recvBuf, nReadedSize, (recvLen - nReadedSize));
                Log.d(TAG, "read:"+ StringUtil.byte2HexStr(recvBuf));
                if (nRet > 0) {
                    nReadedSize += nRet;
                    if (recvLen == nReadedSize) {
                        return 0;
                    }
                }
            }
            try {
                Thread.sleep(SLEEP_TIME);
                lCurrentTime = System.currentTimeMillis();
                if ((lCurrentTime - lBeginTime) > waitTime) {
                    return 2;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
