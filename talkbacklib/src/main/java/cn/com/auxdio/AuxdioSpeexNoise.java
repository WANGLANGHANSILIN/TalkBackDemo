package cn.com.auxdio;

/**
 * Created by wang l on 2017/7/11.
 */

public class AuxdioSpeexNoise {

    static
    {
        System.loadLibrary("speexnoise");
    }

    public int initAudioNoises(int frameSize,int samplingRate){
        return initAudioNoise(frameSize,samplingRate);
    }

    public int audioNoise8Ks(short[] recordArray){
        return audioNoise8K(recordArray);
    }

    public int audioNoise16Ks(short[] recordArray){
        return audioNoise16K(recordArray);
    }

    public void exitAudioNoises(){
        exitAudioNoise();
    }

    private native int initAudioNoise(int frameSize,int samplingRate);

    private native int audioNoise8K(short[] recordArray);

    private native int audioNoise16K(short[] recordArray);

    private native int exitAudioNoise();
}
