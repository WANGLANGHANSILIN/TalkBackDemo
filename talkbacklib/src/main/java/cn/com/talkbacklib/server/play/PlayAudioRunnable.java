package cn.com.talkbacklib.server.play;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.LinkedList;

import cn.com.talkbacklib.bean.RecordBean;
import cn.com.talkbacklib.client.recorder.EncodeRunnable;

/**
 * Created by wang l on 2017/7/10.
 */

public class PlayAudioRunnable implements Runnable {

    private AudioTrack mAudioTrack;
    private int mSamplingRate = 8000;
    public static LinkedList<RecordBean> sPlayLinkedList = new LinkedList<>();
    private final Object mObject = new Object();
    private boolean isStopPlay;
    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        int size = AudioTrack.getMinBufferSize(8000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                mSamplingRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                //AudioTrack.getMinBufferSize (m_iSamplingRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT),
                size,
                AudioTrack.MODE_STREAM);
        mAudioTrack.play();

        while (!isStopPlay()){

            if (sPlayLinkedList.size() == 0){
                synchronized (mObject){
                    try {
                        mObject.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (sPlayLinkedList.size() > 0){
                synchronized (mObject){
                    RecordBean first = sPlayLinkedList.getFirst();
                    mAudioTrack.write(first.recordShort,0,first.recordLen);
                    EncodeRunnable.putPlayData(first.recordShort,first.recordLen);
                    sPlayLinkedList.remove(first);
                }
            }
        }

        mAudioTrack.stop();
        mAudioTrack.release();
    }

    public void putPlayData(short[] shorts, int length) {
        synchronized (mObject){
            RecordBean recordBean = new RecordBean(shorts,length);
            sPlayLinkedList.addLast(recordBean);
            mObject.notify();
        }
    }

    public boolean isStopPlay() {
        return isStopPlay;
    }

    public void setStopPlay(boolean stopPlay) {
        isStopPlay = stopPlay;
    }
}
