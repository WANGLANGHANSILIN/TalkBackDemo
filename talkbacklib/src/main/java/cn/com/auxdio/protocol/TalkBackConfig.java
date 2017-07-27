package cn.com.auxdio.protocol;

/**
 * Created by wang l on 2017/6/22.
 */

public class TalkBackConfig {

    public static final int TALKBACK_PORT = 11220;// 对讲端口

    public static final int TRANSPORT_SEND_PORT = 11230;// 数据通信端口

    public static final int TRANSPORT_TAGET_PORT = 11231;// 数据通信端口

    public static final String BROADCASE_ADDR = "255.255.255.255";// 广播地址

    public static final String MULTICAST_ADDR = "235.1.1.1";// 组播地址,224.0.0.0到239.255.255.255属于多播地址。

}
