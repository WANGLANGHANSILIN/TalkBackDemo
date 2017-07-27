package cn.com.talkbacklib.client.recorder;

import android.util.Log;

import com.example.Android_audio_talkback_demo_program.SpeexAec;
import com.example.Android_audio_talkback_demo_program.SpeexPreprocessor;
import com.example.Android_audio_talkback_demo_program.WebRtcNsx;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

import cn.com.auxdio.protocol.TalkBackConfig;
import cn.com.auxdio.speex.Speex;
import cn.com.talkbacklib.TalkBackHandle;
import cn.com.talkbacklib.bean.RecordBean;

/**
 * Created by wang l on 2017/7/7.
 */

public class EncodeRunnable implements Runnable {

    private volatile boolean isRecording;
    public static final Object mObject = new Object();
    public static volatile LinkedList<RecordBean> mPlayLinkedList;
    private static LinkedList<RecordBean> mRecordLinkedList;

    private int mSamplingRate = 8000;
    private int mFrameSize = 160;
    private InetAddress mInetAddress;

//    private SpeexDsp mSpeexDsp;

    private SpeexAec mSpeexAec;
    private WebRtcNsx mWebRtcNsx;
    private SpeexPreprocessor mSpeexPreprocessor;

//    private AuxdioSpeexAEC mAuxdioSpeexAEC;
//    private AuxdioSpeexNoise mAuxdioSpeexNoise;
//    private MobileAEC mMobileAEC;

    public EncodeRunnable(String devIP) {
        mRecordLinkedList = new LinkedList<>();
        mPlayLinkedList = new LinkedList<>();
        Speex.init();
//        mAuxdioSpeexAEC = new AuxdioSpeexAEC();
//        mAuxdioSpeexAEC.initAudioAECs(mFrameSize,mFrameSize*8,mSamplingRate);
//        mAuxdioSpeexNoise = new AuxdioSpeexNoise();
//        mAuxdioSpeexNoise.initAudioNoises(mFrameSize,mSamplingRate);

//        mMobileAEC = new MobileAEC(MobileAEC.SamplingFrequency.FS_8000Hz)
//                .setAecmMode(MobileAEC.AggressiveMode.MOST_AGGRESSIVE).prepare();

//        mSpeexDsp = new SpeexDsp();
//        mSpeexDsp.InitAudioAEC(mFrameSize,mFrameSize*8,mSamplingRate);
        mSpeexAec = new SpeexAec();
        mWebRtcNsx = new WebRtcNsx();
        mSpeexPreprocessor = new SpeexPreprocessor();
//
        mSpeexAec.Init(mFrameSize,mSamplingRate,mFrameSize*6);
        mWebRtcNsx.Init(8000,2);
        mSpeexPreprocessor.SpeexPreprocessInit(8000,160,mSpeexAec.GetSpeexEchoState().longValue());

        try {
            mInetAddress = InetAddress.getByName(devIP);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (isRecording()){

            if (mRecordLinkedList.size() == 0){
                synchronized (mObject){
                    try {
                        mObject.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            synchronized (mObject)
            {
                if (mRecordLinkedList.size() > 0){
                    RecordBean first = mRecordLinkedList.getFirst();
                    short[] outputAudio = new short[mFrameSize];
                    byte[] bytes = new byte[mFrameSize];
                    Log.i("EncodeRunnable", "run: recordList.size = "+mRecordLinkedList.size()+",playList.size = "+mPlayLinkedList.size());

                    short[] shorts = new short[mFrameSize];
                    if (mPlayLinkedList.size() > 0){
                        RecordBean first1 = mPlayLinkedList.getFirst();
                        shorts = first1.recordShort;
//                        mSpeexDsp.AudioAECProc(first.recordShort,first1.recordShort,outputAudio);

                        mSpeexAec.Aec(first.recordShort,first1.recordShort,outputAudio);
                        mWebRtcNsx.Process(8000,outputAudio,outputAudio.length);
                        mSpeexPreprocessor.Preprocess(outputAudio,new Long(1));

//                        mAuxdioSpeexAEC.audioAECExecutes(first.recordShort,first1.recordShort,outputAudio);

                        mPlayLinkedList.remove(first1);
                    }else
                        outputAudio = first.recordShort;

                    try {
//                        mMobileAEC.farendBuffer(first.recordShort,first.recordLen);
//                        mMobileAEC.echoCancellation(first.recordShort,null,outputAudio, (short) outputAudio.length, (short) 190);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    outputAudio = first.recordShort;
                    mRecordLinkedList.remove(first);
                    Log.i("EncodeRunnable", "outputAudiorun: "+outputAudio[0]+" "+outputAudio[1]+" "+outputAudio[2]+" "+outputAudio[3]+" "+outputAudio[4]+" "+outputAudio[5]);
//                    mAuxdioSpeexNoise.audioNoise8Ks(outputAudio);
                    Speex.init().encode(outputAudio,0,bytes,bytes.length);
                    sendData(bytes);
                }
            }
        }

        mSpeexAec.Destory();
        mWebRtcNsx.Destory();
        mSpeexPreprocessor.finalize();
        mSpeexAec = null;
        mWebRtcNsx = null;
        mSpeexPreprocessor = null;
        mRecordLinkedList.clear();
        mPlayLinkedList.clear();

//        mSpeexDsp.ExitSpeexDsp();
//        mAuxdioSpeexNoise.exitAudioNoises();
//        mAuxdioSpeexAEC.exitAudioAECs();
//        mMobileAEC.close();
    }

    private void sendData(byte[] bytes) {
        try {
            DatagramPacket packet = new DatagramPacket(bytes,bytes.length,mInetAddress, TalkBackConfig.TALKBACK_PORT);
            TalkBackHandle.newInstance().getSendTransportThread().sendTalkbackData(packet);
//            mDatagramSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isRecording() {
        synchronized (mObject){
            return isRecording;
        }
    }

    public void setRecording(boolean recording) {
        synchronized (mObject){
            isRecording = recording;
        }
    }

    public static void putRecordData(short[] shorts,int len){
        synchronized (mObject){
            mRecordLinkedList.addLast(new RecordBean(shorts,len));
            mObject.notify();
        }
    }

    public static void putPlayData(short[] shorts,int len){
        synchronized (mObject){
            if (mPlayLinkedList != null) {
                mPlayLinkedList.addLast(new RecordBean(shorts,len));
                mObject.notify();
            }
        }
    }

}
