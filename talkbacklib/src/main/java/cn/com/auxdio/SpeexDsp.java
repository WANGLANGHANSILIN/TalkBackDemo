package cn.com.auxdio;

/**
 * Created by wang l on 2017/6/30.
 */

public class SpeexDsp {


    static {
        System.loadLibrary("speexdsp");
    }
    /*
       jint frame_size        帧长      一般都是  80,160,320
       jint filter_length     尾长      一般都是  80*25 ,160*25 ,320*25
       jint sampling_rate     采样频率  一般都是  8000，16000，32000
    */
    public native int InitAudioAEC(int frameSize,int filterLength,int samplingRate);

    public native int AudioAECProc(short[] recodeAudio,short[]playAudio,short[] outAudio);

    public native int ExitSpeexDsp();
}
