package com.view.core.thread;

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
    public static final boolean isDebug = true;
    public static final int SERVER_CONNECT_TIMEOUT = 30*1000;

    public static final String CMD_SEARCH_REMOTE_LIST   = "100000";
    public static final String CMD_FETCH_REMOTE_DEVICE  = "100001";
    public static final String CMD_RETURN_REMOTE_DEVICE  = "100002";
    public static final String CMD_STOP_REMOTE_OPERA    = "100003";
    public static final String CMD_FETCH_REMOTE_PHONE    = "100004";
    public static final String CMD_STOP_REMOTE_PHONE    = "100005";
    public static final String CMD_FETCH_REMOTE_SCREEN    = "100006";
    public static final String CMD_STOP_REMOTE_SCREEN    = "100007";


    public static final String KEY_CMD = "CMD";
    public static final String KEY_LIST = "LIST";
    public static final String KEY_HOSTNAME = "HOSTNAME";
    public static final String KEY_FILE = "FILE";
    public static final String KEY_LENGTH = "LENGTH";

    public static final String TYPE_PHONE = "type_phone";
    public static final String TYPE_SCREEN = "type_screen";

    public static final String FILE_PHONE = "record_phone.3gp";
    public static final String FILE_SCREEN = "record_screen.mp4";
}
