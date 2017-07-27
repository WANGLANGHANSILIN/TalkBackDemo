package cn.com.auxdio.protocol;

/**
 * Created by wang l on 2017/6/20.
 */

public class CmdDefine {
    public static final byte CMD_DEVICE_BRAOD_CAST = 0X41;//搜索设备
    public static final byte CMD_DEVICE_ONLINE = 0X42;//设备上线
    public static final byte CMD_TALKBACK_STATE = 0X43;//设备对讲状态
    public static final byte CMD_ADD_GROUP = 0X44; //加入组播
    public static final byte CMD_EXIT_GROUP = 0X45; //退出组播
    public static final byte CMD_REQUEST_TALKBACK = 0X46; //发起对讲
    public static final byte CMD_EXIT_TALKBACK = 0X47; //退出对讲
    public static final byte CMD_REQUEST_BROAD = 0X48; //发起广播
    public static final byte CMD_EXIT_BROAD = 0X49; //退出广播
    public static final byte CMD_GROUP_ADDR_CHANGE = 0X4A; //组播地址改变通知

    public static class TalkBackStatus{
        public static final byte STATUS_IDLE = 0X01; //空闲状态
        public static final byte STATUS_READY = 0X02; //就绪状态
        public static final byte STATUS_BUSY = 0X03; //忙碌状态
    }

    public static final class QueryStatus{
        public static final int STATUS_SET = 0X08; //查询状态
        public static final int STATUS_QUERY = 0x80; //设置状态
        public static final int STATUS_BROAD = 0x88;
    }

}
