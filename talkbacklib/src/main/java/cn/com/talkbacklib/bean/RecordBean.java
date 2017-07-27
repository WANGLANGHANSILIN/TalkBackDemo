package cn.com.talkbacklib.bean;

public class RecordBean{
    public RecordBean(short[] recordShort,int recordLen) {
        this.recordLen = recordLen;
        this.recordShort = recordShort;
    }

    public int recordLen;
    public short[] recordShort = new short[160];
}