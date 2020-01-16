package com.view.acore.thread;

/**
 * @author: yechenyu
 * @create: 2020/1/8 下午9:26
 * @email: Yecynull@163.com
 * @version:
 * @descripe:
 **/
public class Constant {

        public static String SERVER_IP = "101.133.174.68";
//    public static String SERVER_IP = "172.20.10.2";
    public static final int SERVER_PORT = 8080;
    public static final int SERVER_CONNECT_TIMEOUT = 30*1000;

    public static final String CMD_SEARCH_REMOTE_LIST   = "100000";
    public static final String CMD_FETCH_REMOTE_DEVICE  = "100001";
    public static final String CMD_RETURN_REMOTE_DEVICE  = "100002";
    public static final String CMD_STOP_REMOTE_OPERA    = "100003";
    public static final String CMD_TRANSFER_PHONE_DATA    = "100004";
    public static final String CMD_REANSFER_SCREEN_DATA    = "100005";


    public static final String KEY_CMD = "CMD";
    public static final String KEY_LIST = "LIST";
    public static final String KEY_HOSTNAME = "HOSTNAME";
    public static final String KEY_FILE = "FILE";
    public static final String KEY_LENGTH = "LENGTH";
    public static final String KEY_DATA = "DATA";

    public static final String TYPE_PHONE = "type_phone";
    public static final String TYPE_SCREEN = "type_screen";

    public static final String FILE_PHONE = "phone.3gp";
    public static final String FILE_SCREEN = "screen.mp4";
}
