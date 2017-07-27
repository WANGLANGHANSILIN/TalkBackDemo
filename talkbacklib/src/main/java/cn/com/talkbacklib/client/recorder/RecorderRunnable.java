package cn.com.talkbacklib.client.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Created by wang l on 2017/7/7.
 */

public class RecorderRunnable implements Runnable {

    private volatile boolean isRecording;
    private final Object mObject = new Object();
    private AudioRecord mAudioRecord;//录音类
    private int mSamplingRate = 8000;
    private int mFrameSize = 160;
    private String mDevIP;

    public RecorderRunnable(String devIP) {
        this.mDevIP = devIP;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        mAudioRecord = new AudioRecord( MediaRecorder.AudioSource.MIC,
                mSamplingRate,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                AudioRecord.getMinBufferSize( mSamplingRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT));
        mAudioRecord.startRecording();
        EncodeRunnable encodeRunnable = new EncodeRunnable(mDevIP);
        encodeRunnable.setRecording(isRecording());
        new Thread(encodeRunnable).start();
        while (isRecording()){
            synchronized (mObject){
                short[] recordShort = new short[mFrameSize];
                mAudioRecord.read(recordShort,0,mFrameSize);
                encodeRunnable.putRecordData(recordShort,mFrameSize);
            }
        }
        mAudioRecord.stop();
        mAudioRecord.release();
        mAudioRecord = null;
    }

    public boolean isRecording() {
        synchronized (mObject){
            return isRecording;
        }
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
        synchronized (mObject){
        }
    }

    public String getDevIP() {
        return mDevIP;
    }
}
