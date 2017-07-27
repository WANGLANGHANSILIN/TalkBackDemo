package cn.com.auxdio.speex;

/**
 * Created by yanghao1 on 2017/4/18.
 */

public class Speex {

    private static final int DEFAULT_COMPRESSION = 8;
    public static final String SPEEX_LOG = Speex.class.getSimpleName();

    static {
        try {
            System.loadLibrary("auxdiospeex");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static Speex speex = null;

    private Speex() {
        open(DEFAULT_COMPRESSION);
    }

    public static Speex init() {
        if (speex == null) {
            synchronized (Speex.class) {
                if (speex == null) {
                    speex = new Speex();
                }
            }
        }
        return speex;
    }

    public native int open(int compression);

    public native int getFrameSize();

    public native int decode(byte encoded[], short lin[], int size);

    public native int encode(short lin[], int offset, byte encoded[], int size);

    public native void close();
}
