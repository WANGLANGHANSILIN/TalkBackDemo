package cn.com.talkbacklib.bean;

import java.util.LinkedList;

/**
 * Created by wang l on 2017/6/28.
 */

public class AuxdioDataBean {

    private static LinkedList<short[]> audioOutput = new LinkedList<>();
    private static  LinkedList<short[]>audioInput = new LinkedList<>();
    private static int mAudioSessionId;

    public synchronized static short[] getFistAudioInput(){
        synchronized (audioInput){
            if (audioOutput.size() < 1)
                return new short[0];
            short[] first = audioInput.getFirst();
            audioInput.removeFirst();
            return first;
        }
    }

    public synchronized static void AddAuxdioInput(short[] shorts){
        synchronized (audioInput){
            if (audioInput == null)
                audioInput = new LinkedList<>();
            if (audioInput.size() > 160)
                audioInput.clear();
            audioInput.addLast(shorts);
        }
    }

    public synchronized static short[] getFistAudioOutput(){
        synchronized (audioOutput){
            if (audioOutput.size() < 1)
                return new short[0];
            short[] first = audioOutput.getFirst();
            audioOutput.removeFirst();
            return first;
        }
    }

    public synchronized static void AddAuxdioOutput(short[] shorts){
        synchronized (audioOutput){
            if (audioOutput == null)
                audioOutput = new LinkedList<>();
            if (audioOutput.size() > 160)
                audioOutput.clear();
            audioOutput.addLast(shorts);
        }
    }

    public static void setAuxdioSessionId(int audioSessionId) {
        mAudioSessionId = audioSessionId;
    }

    public static int getmAudioSessionId() {
        return mAudioSessionId;
    }
}
