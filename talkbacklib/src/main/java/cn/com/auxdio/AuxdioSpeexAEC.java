package cn.com.auxdio;

/**
 * Created by wang l on 2017/7/11.
 */

public class AuxdioSpeexAEC {

    static
    {
        System.loadLibrary("speexaec");
    }

    public int initAudioAECs(int frameSize,int filterLen,int samplingRate){
        return initAudioAEC(frameSize,filterLen,samplingRate);
    }

    public int audioAECExecutes(short[] recordArray,short[] playArray,short[] outArray){
        return audioAECExecute(recordArray,playArray,outArray);
    }

    public void exitAudioAECs() {
        exitAudioAEC();
    }

    private native int initAudioAEC(int frameSize,int filterLen,int samplingRate);

    private native int audioAECExecute(short[] recordArray,short[] playArray,short[] outArray);

    private native int exitAudioAEC();
}
