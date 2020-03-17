package com.auto.commonlibrary.transfer;


import android.util.Log;

import com.auto.commonlibrary.util.BCDDecode;
import com.auto.commonlibrary.util.LrcUtil;
import com.auto.commonlibrary.util.StringUtil;

import java.util.Locale;


/**
 * 报文处理类，负责封装请求报文以及解析请求报文
 */
public class HandleProtocol {

	public static final int EXECUTE_ID_SUCCESS = 0;

	public static final int EXECUTE_ID_LRC_ERROR = -1;
	public static final int EXECUTE_ID_CMD_ERROR = -2;
	public static final int EXECUTE_ID_STX_ERROR = -3;
	public static final int EXECUTE_ID_LEN_ERROR = -4;

	public static final String TAG = HandleProtocol.class.getSimpleName();

	private static final boolean D = false;

	/**
	 * 组装报文： STX、PKGLEN、CMD、2F、DATA、ETX、LRC
	 *
	 * @param cmd
	 * @param params
	 * @return
	 */
	public byte[] packRequestProtocol(byte[] cmd, byte[] params) {

		int len = 9;// 参数除外的字节数
		int paramLen = 0;// 参数的字节数

		if (params != null && params.length > 0)
			paramLen = params.length;
		int pos = 0;
		byte[] requestData = new byte[len + paramLen];
		// 包起止符2字符x
		requestData[pos++] = 0x02;// STX

		// 包长度,参数和指令的字节数，高字节优先输出
		String dataLenStr = String.format(Locale.US,"%04d", paramLen + 4);
		byte[] dataLen = BCDDecode.str2Bcd(dataLenStr);
		requestData[pos++] = dataLen[0];
		requestData[pos++] = dataLen[1];
		// 包指令
		requestData[pos++] = cmd[0];
		requestData[pos++] = cmd[1];
		requestData[pos++] = cmd[2];
		// DATA
		requestData[pos++] = 0x3f;
		if (paramLen > 0) {
			System.arraycopy(params, 0, requestData, pos, paramLen);
			pos += paramLen;
		}
		// ETX
		requestData[pos++] = 0x03;
		// LRC
		byte[] lrc_data = new byte[requestData.length - 2];
		System.arraycopy(requestData, 1, lrc_data, 0, requestData.length - 2);
		Log.d(TAG, "packProtocol: lrcData:"+ StringUtil.byte2HexStr(lrc_data));
		requestData[pos] = LrcUtil.lrc_check(lrc_data);
		logSend(cmd, params);
		return requestData;
	}

	/**
	 * 组装响应报文： STX、PKGLEN、CMD、2F、RESP、DATA、ETX、LRC
	 *
	 * @param cmd
	 * @param params
	 * @parm respCode
	 * @return
	 */
	public byte[] packResponseProtocol(byte[] cmd, byte[] respCode, byte[] params) {

		int len = 11;// 参数除外的字节数
		int paramLen = 0;// 参数的字节数

		if (params != null && params.length > 0)
			paramLen = params.length;
		int pos = 0;
		byte[] requestData = new byte[len + paramLen];
		// 包起止符2字符x
		requestData[pos++] = 0x02;// STX  0200191000002F30307B224C495354223A225B5D227D031D

		// 包长度,参数和指令的字节数，高字节优先输出
		String dataLenStr = String.format(Locale.US,"%04d", paramLen + 6);
		byte[] dataLen = BCDDecode.str2Bcd(dataLenStr);
		requestData[pos++] = dataLen[0];
		requestData[pos++] = dataLen[1];
		// 包指令
		requestData[pos++] = cmd[0];
		requestData[pos++] = cmd[1];
		requestData[pos++] = cmd[2];

		requestData[pos++] = 0x2f;

		requestData[pos++] = respCode[0];
		requestData[pos++] = respCode[1];
		// DATA
		if (paramLen > 0) {
			System.arraycopy(params, 0, requestData, pos, paramLen);
			pos += paramLen;
		}
		// ETX
		requestData[pos++] = 0x03;
		// LRC
		byte[] lrc_data = new byte[requestData.length - 2];
		System.arraycopy(requestData, 1, lrc_data, 0, requestData.length - 2);
		requestData[pos] = LrcUtil.lrc_check(lrc_data);

		logSend(cmd, params);
		return requestData;
	}
	
	/**
	 * 解析请求报文
	 *
	 * @param src
	 * @return
	 */
	public RespResult unPackageRequestProtocol(byte[] src) {

		RespResult respResult = new RespResult();
		int pos = 0;

		byte stx = src[pos++];
		if(stx != 0x02){
			respResult.setExecuteId(EXECUTE_ID_STX_ERROR);
			return respResult;
		}

		byte[] data_len = new byte[2];
		System.arraycopy(src, pos, data_len, 0, data_len.length);
		pos += data_len.length;
		int len = Integer.valueOf(BCDDecode.bcd2Str(data_len));
		if(len != src.length-5){
			respResult.setExecuteId(EXECUTE_ID_LEN_ERROR);
			return respResult;
		}
		byte[] src_cmd = new byte[3];
		System.arraycopy(src, pos, src_cmd, 0, src_cmd.length);
		pos += 3;
		// 比对指令  00191000002F7B22484F53544E414D45223A22227D03
		String cmd = StringUtil.byte2HexStr(src_cmd);
		Log.d(TAG, "cmdStr ="+ cmd );
		respResult.setCmdCode(cmd);

		byte cmdType = src[3];// 指示位, 0x2F, 请求/响应报文指示 ， 0x3F，为终端主动发送的报文
		pos++;// 指示位
		respResult.setCmdType(cmdType);

		int paramLen = src.length - pos - 2;
		if (paramLen > 0) {
			byte[] params = new byte[paramLen];
			System.arraycopy(src, pos, params, 0, paramLen);
			pos += paramLen;
			respResult.setParams(params);
			logRecv(src_cmd, src, respResult.getCmdCode());
		} else {
			logRecv(src_cmd, null, respResult.getCmdCode());
		}
		pos++;// ETX
		byte lrc = src[pos];
		Log.d(TAG, "lrc:"+ (byte)lrc);

		String dataLenStr = String.format(Locale.US,"%04d", src.length-5);
		byte[] dataLen = BCDDecode.str2Bcd(dataLenStr);

		byte[] lrcData = new byte[src.length-2];
		System.arraycopy(src, 1, lrcData, 0, src.length - 2);
		Log.d(TAG, "lrcData:"+ StringUtil.byte2HexStr(lrcData));
		byte calLrc = LrcUtil.lrc_check(lrcData);// LRC
		if (lrc != calLrc) {
			respResult.setExecuteId(EXECUTE_ID_LRC_ERROR);
		} else {
			respResult.setExecuteId(EXECUTE_ID_SUCCESS);
		}

		return respResult;
	}

	/**
	 * 解析响应报文
	 *
	 * @param src
	 * @return
	 */
	public RespResult unPackageResponseProtocol(byte[] src) {

		RespResult respResult = new RespResult();
		int pos = 0;

		byte stx = src[pos++];
		if(stx != 0x02){
			respResult.setExecuteId(EXECUTE_ID_LRC_ERROR);
			return respResult;
		}

		byte[] data_len = new byte[2];
		System.arraycopy(src, pos, data_len, 0, data_len.length);
		pos += data_len.length;
		int len = Integer.valueOf(BCDDecode.bcd2Str(data_len));
		if(len != src.length-5){
			respResult.setExecuteId(EXECUTE_ID_LRC_ERROR);
			return respResult;
		}

		byte[] src_cmd = new byte[3];
		System.arraycopy(src, pos, src_cmd, 0, src_cmd.length);
		pos += 3;
		// 比对指令
		String cmdStr = StringUtil.byte2HexStr(src_cmd);
		Log.d(TAG, "cmd ="+cmdStr);

		byte dir_cmd = src[pos];// 指示位, 0x2F, 请求/响应报文指示 ， 0x3F，为终端主动发送的报文
		pos++;// 指示位
		if ((byte) 0x2F == dir_cmd) {
			byte[] src_response = new byte[2];
			System.arraycopy(src, pos, src_response, 0, 2);
			pos += 2;
			// 响应码
			respResult.setRespCode(StringUtil.hexStr2Str(StringUtil.byte2HexStr(src_response)));
		}

		int paramLen = src.length - pos - 2;
		if (paramLen > 0) {
			byte[] params = new byte[paramLen];
			System.arraycopy(src, pos, params, 0, paramLen);
			pos += paramLen;
			respResult.setParams(params);
			logRecv(src_cmd, src, respResult.getRespCode());
		} else {
			logRecv(src_cmd, null, respResult.getRespCode());
		}
		pos++;// ETX
		byte lrc = src[pos];
		Log.d(TAG, "lrc:"+lrc);

		String dataLenStr = String.format(Locale.US,"%04d", src.length - 2);
		byte[] dataLen = BCDDecode.str2Bcd(dataLenStr);

		byte[] lrcData = new byte[src.length + 1];
		System.arraycopy(dataLen, 0, lrcData, 0, 2);
		System.arraycopy(src, 0, lrcData, 2, src.length - 1);
		byte calLrc = LrcUtil.lrc_check(lrcData);// LRC
		if (lrc != calLrc) {
			respResult.setExecuteId(EXECUTE_ID_LRC_ERROR);
		} else {
			respResult.setExecuteId(EXECUTE_ID_SUCCESS);
		}

		return respResult;
	}

	/**
	 * 解析报文
	 *
	 * @param src
	 * @return
	 */
	public RespResult unPackageProtocol(byte[] src) {

		RespResult respResult = new RespResult();
		int pos = 0;

		byte stx = src[pos++];
		if(stx != 0x02){
			respResult.setExecuteId(EXECUTE_ID_STX_ERROR);
			return respResult;
		}

		byte[] data_len = new byte[2];
		System.arraycopy(src, pos, data_len, 0, data_len.length);
		pos += data_len.length;
		int len = Integer.valueOf(BCDDecode.bcd2Str(data_len));
		if(len != src.length-5){
			respResult.setExecuteId(EXECUTE_ID_LEN_ERROR);
			return respResult;
		}

		byte[] src_cmd = new byte[3];
		System.arraycopy(src, pos, src_cmd, 0, src_cmd.length);
		pos += 3;
		// 比对指令
		String cmdStr = StringUtil.byte2HexStr(src_cmd);
		Log.d(TAG, "cmd ="+ cmdStr);
		respResult.setCmdCode(cmdStr);

		byte dir_cmd = src[pos];// 指示位, 0x2F, 请求/响应报文指示 ， 0x3F，为终端主动发送的报文
		pos++;// 指示位
		respResult.setCmdType(dir_cmd);

		if ((byte) 0x2F == dir_cmd) {
			byte[] src_response = new byte[2];
			System.arraycopy(src, pos, src_response, 0, 2);
			pos += 2;
			// 响应码
			respResult.setRespCode(StringUtil.hexStr2Str(StringUtil.byte2HexStr(src_response)));
		}

		int paramLen = src.length - pos - 2;
		if (paramLen > 0) {
			byte[] params = new byte[paramLen];
			System.arraycopy(src, pos, params, 0, paramLen);
			pos += paramLen;
			respResult.setParams(params);
			logRecv(src_cmd, src, respResult.getRespCode());
		} else {
			logRecv(src_cmd, null, respResult.getRespCode());
		}
		pos++;// ETX
		byte lrc = src[pos];
		Log.d(TAG, "lrc:"+lrc);

		String dataLenStr = String.format(Locale.US,"%04d", src.length - 2);
		byte[] dataLen = BCDDecode.str2Bcd(dataLenStr);

		byte[] lrcData = new byte[src.length-2];
		System.arraycopy(src, 1, lrcData, 0, src.length - 2);
		Log.d(TAG, "lrcData:"+ StringUtil.byte2HexStr(lrcData));
		byte calLrc = LrcUtil.lrc_check(lrcData);// LRC
		if (lrc != calLrc) {
			respResult.setExecuteId(EXECUTE_ID_LRC_ERROR);
		} else {
			respResult.setExecuteId(EXECUTE_ID_SUCCESS);
		}

		return respResult;
	}

	private void logSend(byte[] cmd, byte[] params) {
		StringBuilder send = new StringBuilder();
//		send.append("==============发送 Start===============\n");
		String cmdStr = StringUtil.byte2HexStr(cmd);
		send.append("发送 >>> 指令:[" + cmdStr +"]"+"\n");
		for (String[] cmdPair : ALL_CMD) {
			if (cmdPair[0].equalsIgnoreCase(cmdStr)) {
				send.append("[" + cmdPair[1] + "]\n");
				break;
			}
		}
		send.append("发送 >>> 内容:[" + StringUtil.byte2HexStr(params) + "]\n\n");
//		send.append("==============发送 End===============\n");
		Log.d(TAG, send.toString());
//		LOG.debug(send.toString());
	}

	private void logRecv(byte[] cmd, byte[] params,String respCode) {
		StringBuilder recv = new StringBuilder();
//		recv.append("==============接收 Start===============\n");
		String cmdStr = StringUtil.byte2HexStr(cmd);
		for (String[] cmdPair : ALL_CMD) {
			if (cmdPair[0].equalsIgnoreCase(cmdStr)) {
				recv.append("接收 <<< 指令:[" + cmdStr + " " + cmdPair[1] + "]\n");
				break;
			}
		}
		recv.append("接收 <<< 响应码:["+respCode+"] 内容:[" + StringUtil.byte2HexStr(params) + "]\n\n");
//		recv.append("==============接收 End===============\n");
		Log.d(TAG, recv.toString());
//		LOG.debug(recv.toString());
	}

//	public static final String CMD_SEARCH_REMOTE_LIST   = "100000";
//	public static final String CMD_FETCH_REMOTE_DEVICE  = "100001";
//	public static final String CMD_RETURN_REMOTE_DEVICE  = "100002";
//	public static final String CMD_STOP_REMOTE_OPERA    = "100003";
//	public static final String CMD_FETCH_REMOTE_PHONE    = "100004";
//	public static final String CMD_STOP_REMOTE_PHONE    = "100005";
//	public static final String CMD_FETCH_REMOTE_SCREEN    = "100006";
//	public static final String CMD_STOP_REMOTE_SCREEN    = "100007";
//
//	public static final String CMD_TRANSFER_REMOTE_DATA    = "100008";
//	public static final String CMD_FETCH_REMOTE_LOCATION    = "100009";

	public static final String[] CMD_SearchRemoteList =  new String[] { "100000", "获取远程设备列表" };
	public static final String[] CMD_FetchRemoteDevice = new String[] { "100001", "获取远程设备信息" };
	public static final String[] CMD_ReturnRemoteDevice = new String[] { "100002", "返回远程设备信息" };
	public static final String[] CMD_StopRemoteOpera = new String[] { "100003", "返回远程设备信息" };
	public static final String[] CMD_FetchRemotePhone =  new String[] { "100004", "远程录音" };
	public static final String[] CMD_StopRemotePhone =   new String[] { "100005", "停止远程录音" };
	public static final String[] CMD_FetchRemoteScreen = new String[] { "100006", "远程录屏" };
	public static final String[] CMD_StopRemoteScreen  = new String[] { "100007", "停止录屏" };
	public static final String[] CMD_FetchRemoteLocation = new String[] { "1000009", "获取远程位置信息" };
	public static final String[] CMD_TransferData = new String[] { "100008", "转发远程数据" };
	public static final String[] CMD_xxx = new String[] { "1000xx", "xxx" };

	public final static String[][] ALL_CMD = {
			CMD_SearchRemoteList, CMD_FetchRemoteDevice, CMD_ReturnRemoteDevice,
			CMD_FetchRemotePhone, CMD_StopRemotePhone, 
			CMD_FetchRemoteScreen, CMD_StopRemoteScreen,
			CMD_FetchRemoteLocation, CMD_TransferData, CMD_xxx
	};

}
