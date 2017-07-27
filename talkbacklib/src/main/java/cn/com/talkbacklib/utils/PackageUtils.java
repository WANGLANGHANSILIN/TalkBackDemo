package cn.com.talkbacklib.utils;

/**
 * Created by wang l on 2017/6/20.
 */

public class PackageUtils {

    public static byte[] splitCmd(byte cmd){
        int i = cmd >> 8;
        int i1 = cmd & 0xFF;
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (i & 0xFF);
        bytes[1] = (byte) (i1 & 0xFF);
        return bytes;
    }

    public static int compoundCmd(byte[] bytes){
        if (bytes.length >= 2){
            int i = bytes[0] << 8;
            int i1 = bytes[1] & 0xFF;
            return i+i1;
        }
        return 0;
    }

    public static String printData(byte[] data, int len){
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < len; i++) {
            String s = String.valueOf(Integer.toHexString(data[i]));
            if (s.length() == 1)
                s = "0"+s;
            buffer.append(s+"  ");
        }
        return buffer.toString();
    }

    public static byte[] requestPackage(byte cmd,byte[] bytes){
        byte[] bytes2 = splitCmd(cmd);
        byte[] bytes3 = new byte[6];
        System.arraycopy(bytes2,0,bytes3,0,bytes2.length);
        bytes3[5] = (byte) bytes.length;
        byte[] bytes1 = new byte[bytes3.length+bytes.length+1];
        System.arraycopy(bytes3,0,bytes1,0,bytes3.length);
        System.arraycopy(bytes,0,bytes1,bytes3.length,bytes.length);
        bytes1[bytes3.length+bytes.length] = getCheckCode(bytes);
        return bytes1;
    }

    private static byte getCheckCode(byte[] bytes) {
        byte checkCode = 0;
        for (int i = 0; i < bytes.length; i++) {
            checkCode+=bytes[i];
        }
        return (byte) (checkCode & 0xFF);
    }
}
