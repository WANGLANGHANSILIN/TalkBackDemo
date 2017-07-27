package cn.com.talkbacklib.utils;

import java.io.UnsupportedEncodingException;

import cn.com.auxdio.protocol.TalkBackConfig;

/**
 * Created by wang l on 2017/6/27.
 */

public class StringUtils {

    public static String convetString(byte[] bytes,int index) throws UnsupportedEncodingException {
        int nameLen = bytes[index++] & 0xFF;
        byte[] byteName = new byte[nameLen];
        System.arraycopy(bytes,index,byteName,0,nameLen);
        String name = new String(byteName,0,nameLen,"utf-8");
        return name;
    }

    public static int getGroupAddressLastNum(String groupAddr){
        int lastIndexOf = groupAddr.lastIndexOf(".");
        return Integer.valueOf(groupAddr.substring(lastIndexOf+1));
    }

    public static String getGroupAddress__(){
        String groupAddr = TalkBackConfig.MULTICAST_ADDR;
        int lastIndexOf = groupAddr.lastIndexOf(".");
        return groupAddr.substring(0,lastIndexOf+1);
    }
}
