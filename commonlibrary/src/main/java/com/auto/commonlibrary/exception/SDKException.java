package com.auto.commonlibrary.exception;

public class SDKException extends Exception {


    /**
     * 成功
     */
    public final static String CODE_SUCCESS = "00";// 成功

    /**
     * 超时
     */
    public final static String ERR_CODE_TIME_OUT = "07"; // 超时

    /**
     * 返回数据为空, 数据包ID重复
     */
    public final static String ERR_RESULT_NULL = "21"; // 返回数据为空，数据包ID重复

    private static final long serialVersionUID = 1L;
    /**
     * <i class="dis_ch">处理成功</i>&emsp;<i class="dis_en">Success</i>
     */
    public final static String[] TERMINAL_CMD_RSP_CODE_OK = {"00", "Success"}; /* 处理成功 */
    /**
     * <i class="dis_ch">K21不支持改指令</i>&emsp;<i class="dis_en">Instruction code not supported</i>
     */
    public final static String[] TERMINAL_CMD_RSP_CODE_ERR_UNSUPPORT = {"01", "Instruction code not supported"}; /* 指令码不支持 */
    /**
     * <i class="dis_ch">参数错误</i>&emsp;<i class="dis_en">Parameter error</i>
     */
    public final static String[] TERMINAL_CMD_RSP_CODE_ERR_PARAM = {"02", "Parameter error"}; /* 参数错误 */
    /**
     * <i class="dis_ch">可变数据域长度错误</i>&emsp;<i class="dis_en">The length of variable data field is error</i>
     */
    public final static String[] TERMINAL_CMD_RSP_CODE_ERR_VARLEN = {"03", "Variable data field length error"}; /* 可变数据域长度错误 */
    /**
     * <i class="dis_ch">帧格式错误</i>&emsp;<i class="dis_en">The format of frame is error</i>
     */
    public final static String[] TERMINAL_CMD_RSP_CODE_ERR_FRAME = {"04", "Frame format error"}; /* 帧格式错误 */
    /**
     * <i class="dis_ch">LRC校验失败</i>&emsp;<i class="dis_en">LRC check failed</i>
     */ 
    public final static String[] TERMINAL_CMD_RSP_CODE_ERR_LRC = {"05", "LRC check failed"}; /* LRC校验失败 */
    /**
     * <i class="dis_ch">其他错误</i>&emsp;<i class="dis_en">Other error</i>
     */
    public final static String[] TERMINAL_CMD_RSP_CODE_ERR_OTHER = {"06", "Other error"}; /* 其他 */
    /**
     * <i class="dis_ch">与K21通信超时</i>&emsp;<i class="dis_en">Communication timeout</i>
     */
    public final static String[] TERMINAL_CMD_RSP_CODE_ERR_TIMEOUT = {"07", "Timeout"}; /* 超时 */
    /**
     * <i class="dis_ch">打开文件失败</i>&emsp;<i class="dis_en">Failed to open file</i>
     */
    public final static String[] TERMINAL_CMD_RSP_CODE_ERR_OPENFILE = {"08", "Failed to open file"}; /* 打开文件失败 */
    /**
     * <i class="dis_ch">设备认证失败</i>&emsp;<i class="dis_en">Device authentication failed</i>
     */
    public final static String[] TERMINAL_CMD_RSP_CODE_ERR_DEVICE_AUTHENTICATE = {"09", "Device authentication failed"};// 设备认证失败
    /**
     * <i class="dis_ch">外部认证失败</i>&emsp;<i class="dis_en">External authentication failure</i>
     */
    public final static String[] TERMINAL_CMD_RSP_CODE_ERR_EXTERN_AUTHENTICATE = {"0A", "External authentication failure"}; // 外部认证失败
    /**
     * <i class="dis_ch">公钥灌装失败</i>&emsp;<i class="dis_en">Public key filling failed</i>
     */
    public final static String[] TERMINAL_CMD_RSP_CODE_ERR_PUBLIC_KEY = {"0B", "Public key filling failed"}; /* 公钥灌装失败 */
    /**
     * <i class="dis_ch">生成密钥对</i>&emsp;<i class="dis_en">Failed to generate key</i>
     */
    public final static String[] TERMINAL_CMD_RSP_CODE_ERR_GENERATE_KEYPAIR = {"0C", "Failed to generate key"}; /* 生成密钥对失败 */
    /**
     * <i class="dis_ch">用户取消</i>&emsp;<i class="dis_en">User cancel</i>
     */
    public final static String[] TERMINAL_CMD_RSP_CODE_ERR_USER_CANCEL = {"10", "User cancel"}; /* 用户取消 */
    /**
     * <i class="dis_ch">算法不支持</i>&emsp;<i class="dis_en">Algorithm does not support</i>
     */
    public final static String[] TERMINAL_CMD_RSP_CODE_ERR_UNSUPPORT_ALG = {"11", "Algorithm does not support"}; /* 算法不支持 */
    /**
     * <i class="dis_ch">长度不匹配</i>&emsp;<i class="dis_en">Length mismatch</i>
     */
    public final static String[] TERMINAL_CMD_RSP_CODE_ERR_LEN_NO_MATCH = {"12", "Length mismatch"}; /* 长度不匹配 */
    /**
     * <i class="dis_ch">参数ID错误</i>&emsp;<i class="dis_en">Parameter ID error</i>
     */
    public final static String[] TERMINAL_CMD_RSP_CODE_ERR_INVALID_ID = {"13", "Parameter ID error"}; /* 参数ID错误 */
    /**
     * <i class="dis_ch">参数错误</i>&emsp;<i class="dis_en">Parameter error</i>
     */
    public final static String[] TERMINAL_CMD_RSP_CODE_ERR_INVALID_PARAM = {"14", "Parameter error"}; /* 参数错误 */
    /**
     * <i class="dis_ch">STX错误</i>&emsp;<i class="dis_en">STX error</i>
     */
    public final static String[] TERMINAL_CMD_RSP_CODE_ERR_STX = {"15", "STX error"}; /* STX错误*/
    /**
     * <i class="dis_ch">ETX错误</i>&emsp;<i class="dis_en">ETX error</i>
     */
    public final static String[] TERMINAL_CMD_RSP_CODE_ERR_ETX = {"16", "ETX error"}; /* ETX错误 */
    /**
     * <i class="dis_ch">二磁道错误</i>&emsp;<i class="dis_en">track two error</i>
     */
    public final static String[] TERMINAL_CMD_RSP_CODE_ERR_TRACK2 = {"17", "Two track error"}; /* 二磁道错误 */
    /**
     * <i class="dis_ch">内存错误</i>&emsp;<i class="dis_en">Memory error</i>
     */
    public final static String[] TERMINAL_CMD_RSP_CODE_ERR_MEM = {"18", "Memory error"}; /* 内存错误 */
    /**
     * <i class="dis_ch">返回数据为空</i>&emsp;<i class="dis_en">Return data is empty</i>
     */
    public final static String[] COMMUNICATE_ERR_RESULT_NULL = {"21", "Return data is empty"}; /* 返回数据为空 */
    /**
     * <i class="dis_ch">图片宽度大于384</i>&emsp;<i class="dis_en">Picture width greater than 384</i>
     */
    public final static String[] COMMUNICATE_ERR_IMG_WIDTH = {"22", "Picture width greater than 384"}; /* 图片宽度大于384 */
    /**
     * <i class="dis_ch">身份证图片解析错误</i>&emsp;<i class="dis_en">Identity card image parsing error</i>
     */
    public final static String[] COMMUNICATE_ERR_IMG_DECODE = {"23", "Identity card image parsing error"}; /* 身份证图片解析错误 */
    /**
     * <i class="dis_ch">身份证图片解析文件不存在</i>&emsp;<i class="dis_en">ID card image resolution file does not exist</i>
     */
    public final static String[] COMMUNICATE_ERR_IMG_DECODE_FILE_NOT_FOUND = {"24", "ID card image resolution file does not exist"}; /* 身份证图片解析文件不存在 */
    /**
     * <i class="dis_ch">通讯异常_帧头STX位错误</i>&emsp;<i class="dis_en">Communication exception STX error</i>
     */
    public final static String[] COMMUNICATE_ERROR_STX_ERROR = {"31", "Communication exception STX error"};// 通讯异常_STX位错误
    /**
     * <i class="dis_ch">通讯异常_帧尾ETX位错误</i>&emsp;<i class="dis_en">Communication exception ETX error</i>
     */
    public final static String[] COMMUNICATE_ERROR_ETX_ERROR = {"32", "Communication exception ETX error"};// 通讯异常_ETX位错误
    /**
     * <i class="dis_ch">通讯超时异常</i>&emsp;<i class="dis_en">Communication timeout exception</i>
     */
    public final static String[] COMMUNICATE_ERROR_TIMEOUT = {"33", "Communication timeout exception"};// 通讯超时异常
    /**
     * <i class="dis_ch">通讯IO异常</i>&emsp;<i class="dis_en">Communication IO exception </i>
     */
    public final static String[] COMMUNICATE_ERROR_IO_ERROR = {"34", "Communication IO exception"};// 通讯异常IO异常
    /**
     * <i class="dis_ch">返回数据指令码不同</i>&emsp;<i class="dis_en">Communication instruction number does not match</i>
     */
    public final static String[] COMMUNICATE_ERROR_BACK_CMD_ERROR = {"35", "Communication instruction number does not match"};// 返回数据指令号不同
    /**
     * <i class="dis_ch">返回数据LRC校验失败</i>&emsp;<i class="dis_en">LRC check failed of return data</i>
     */
    public final static String[] COMMUNICATE_ERROR_LRC_ERROR = {"36", "LRC check failed"};// 返回数据LRC校验失败
    /**
     * <i class="dis_ch">其他通信异常</i>&emsp;<i class="dis_en">Other communication exception</i>
     */
    public final static String[] COMMUNICATE_ERROR_OTHER = {"37", "Other communication exception"};// 其他通讯异常
    /**
     * <i class="dis_ch">指令停止</i>&emsp;<i class="dis_en">Command stop</i>
     */
    public final static String[] COMMUNICATE_ERROR_STOP = {"38", "Command stop"};// 指令停止

    /**
     * <i class="dis_ch">打印忙</i>&emsp;<i class="dis_en">Printer is busy</i>
     */
    public final static String[] COMMUNICATE_ERROR_PRINTER_BUSY = {"39", "Printer busy"};// 打印忙

    /**
     * <i class="dis_ch">SDK上层参数校验错误</i>&emsp;<i class="dis_en">Parameter error</i>
     */
    public final static String[] SDK_CMD_ERROR_PARAM = {"70", "Parameter error"};// SDK上层校验参数错误
    /**
     * <i class="dis_ch">终端返回数据错误</i>&emsp;<i class="dis_en">Terminal return error</i>
     */
    public final static String[] SDK_CMD_TERMINAL_RESULT = {"71", "Terminal return error"};// 终端返回数据错误

    private final static String[][] TERMINAL_CMD_ERRCODE = {TERMINAL_CMD_RSP_CODE_OK,
            TERMINAL_CMD_RSP_CODE_ERR_UNSUPPORT, TERMINAL_CMD_RSP_CODE_ERR_PARAM, TERMINAL_CMD_RSP_CODE_ERR_VARLEN,
            TERMINAL_CMD_RSP_CODE_ERR_FRAME, TERMINAL_CMD_RSP_CODE_ERR_LRC, TERMINAL_CMD_RSP_CODE_ERR_OTHER,
            TERMINAL_CMD_RSP_CODE_ERR_TIMEOUT, TERMINAL_CMD_RSP_CODE_ERR_OPENFILE,
            TERMINAL_CMD_RSP_CODE_ERR_DEVICE_AUTHENTICATE, TERMINAL_CMD_RSP_CODE_ERR_EXTERN_AUTHENTICATE,
            TERMINAL_CMD_RSP_CODE_ERR_PUBLIC_KEY, TERMINAL_CMD_RSP_CODE_ERR_GENERATE_KEYPAIR,
            TERMINAL_CMD_RSP_CODE_ERR_USER_CANCEL, TERMINAL_CMD_RSP_CODE_ERR_UNSUPPORT_ALG,
            TERMINAL_CMD_RSP_CODE_ERR_LEN_NO_MATCH, TERMINAL_CMD_RSP_CODE_ERR_INVALID_ID, COMMUNICATE_ERR_RESULT_NULL,
            TERMINAL_CMD_RSP_CODE_ERR_INVALID_PARAM, TERMINAL_CMD_RSP_CODE_ERR_STX, TERMINAL_CMD_RSP_CODE_ERR_ETX,
            TERMINAL_CMD_RSP_CODE_ERR_TRACK2, TERMINAL_CMD_RSP_CODE_ERR_MEM, COMMUNICATE_ERROR_STX_ERROR,
            COMMUNICATE_ERROR_ETX_ERROR, COMMUNICATE_ERROR_TIMEOUT, COMMUNICATE_ERROR_IO_ERROR,
            COMMUNICATE_ERROR_BACK_CMD_ERROR, COMMUNICATE_ERROR_LRC_ERROR, COMMUNICATE_ERROR_OTHER,
            COMMUNICATE_ERROR_STOP, SDK_CMD_ERROR_PARAM, COMMUNICATE_ERROR_PRINTER_BUSY};

    private String errCode;

    /**
     * Description:
     * <br> &emsp;构造方法
     * @param errCode   <i class="dis_ch">错误码</i>&emsp;<i class="dis_en">Error code</i>
     */
    public SDKException(String errCode) {
        super(setErrMsg(errCode));
        this.errCode = errCode;
    }

    public SDKException(String[] errorPair) {
        super(setErrMsg(errorPair[0]));
        this.errCode = errorPair[0];
    }

    /**
     * Description:
     * <br> &emsp;构造方法
     * @param errCode   <i class="dis_ch">错误码</i>&emsp;<i class="dis_en">Error code</i>
     * @param msg       <i class="dis_ch">错误信息</i>&emsp;<i class="dis_en">Error message</i>
     */
    public SDKException(String errCode, String msg) {
        super(msg);
        this.errCode = errCode;
    }

    private static String setErrMsg(String errCode) {
        String errMsg = "(其他错误,未在错误列表)Other error";
        for (String[] errPair : TERMINAL_CMD_ERRCODE) {
            if (errCode.equalsIgnoreCase(errPair[0])) {
                errMsg = errPair[1];
            }
            if (errCode.equalsIgnoreCase(errPair[1])) {
                errMsg = errPair[1];
            }
        }
        return errMsg;
    }

    /**
     * Description:
     * <br> &emsp;<i class="dis_ch">获取错异常信息中的误码</i>&emsp;<i class="dis_en">Get the error code of exception</i>
     * @return
     */
    public String getErrCode() {
        return errCode;
    }

}
