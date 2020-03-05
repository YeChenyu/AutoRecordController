package com.auto.commonlibrary.transfer;

import com.auto.commonlibrary.util.StringUtil;

/**
 * Created by xxx on 15/3/12.
 */
public class RespResult {

    private int executeId = -9999;//解析结果
    private String cmdCode = "";//指令
    private String respCode = "";//响应码
    private byte[] params = null;
    private byte cmdType;// 数据的指令类型

    public int getExecuteId() {
        return executeId;
    }

    public RespResult setExecuteId(int executeId) {
        this.executeId = executeId;
        return  this;
    }

    public String getCmdCode() {
        return cmdCode;
    }

    public RespResult setCmdCode(String cmd) {
        this.cmdCode = cmd;
        return  this;
    }

    public byte[] getParams() {
        return params;
    }

    public void setParams(byte[] params) {
        this.params = params;
    }

    public byte getCmdType() {
        return cmdType;
    }

    public void setCmdType(byte type) {
        this.cmdType = type;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    @Override
    public String toString() {
        return "RespResult [execute_id=" + executeId + ", cmdCode=" + cmdCode + ", cmdType=" + cmdType + ", params="
                + StringUtil.byte2HexStr(params) + "]";
    }

}
