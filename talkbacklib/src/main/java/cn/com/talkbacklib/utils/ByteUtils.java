package cn.com.talkbacklib.utils;

import java.io.UnsupportedEncodingException;

/**
 * Created by wang l on 2017/6/26.
 */

public class ByteUtils {

    public static byte[] compoundByte(String string) throws UnsupportedEncodingException {
        byte[] bytes1 = string.getBytes("utf-8");
        byte[] bytes = new byte[1+bytes1.length];
        bytes[0] = (byte) bytes1.length;
        System.arraycopy(bytes1,0,bytes,1,bytes1.length);
        return bytes;
    }

    public static byte[] compoundByte(String string,String string1) throws UnsupportedEncodingException {
        byte[] bytes1 = compoundByte(string);
        byte[] bytes2 = compoundByte(string1);
        return compoundByte(bytes1,bytes1.length,bytes2,bytes2.length);
    }

    public static byte[] compoundByte(byte[] bytes1,int len1,byte[] bytes2,int len2) throws UnsupportedEncodingException {
        byte[] bytes = new byte[len1+len2];
        System.arraycopy(bytes1,0,bytes,0,len1);
        System.arraycopy(bytes2,0,bytes,len1,len2);
        return bytes;
    }

}
