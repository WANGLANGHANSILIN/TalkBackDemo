package com.example.Android_audio_talkback_demo_program;
public class SpeexPreprocessor//Speex预处理器类
{
    private Long clSpeexPreprocessState;//Speex预处理器的内存指针

    //构造函数
    public SpeexPreprocessor()
    {
        clSpeexPreprocessState = new Long(0);
    }

    //析构函数
    public void finalize()
    {
        Destory();
        clSpeexPreprocessState = null;
    }

    //初始化Speex预处理器
    private int Init( int iSamplingRate, int iFrameSize, int iIsUseNs, int iNoiseSuppress, int iIsUseVad, int iVadProbStart, int iVadProbContinue, int iIsUseAgc, int iAgcLevel, int iIsUseRec, long lSpeexEchoState, int iEchoSuppress, int iEchoSuppressActive )
    {
        if( clSpeexPreprocessState.longValue() == 0)//如果Speex预处理器还没有初始化
        {
            return SpeexPreprocessInit( clSpeexPreprocessState, iSamplingRate, iFrameSize, iIsUseNs, iNoiseSuppress, iIsUseVad, iVadProbStart, iVadProbContinue, iIsUseAgc, iAgcLevel, iIsUseRec, lSpeexEchoState, iEchoSuppress, iEchoSuppressActive );
        }
        else//如果Speex预处理器已经初始化
        {
            return 0;
        }
    }

    //获取Speex预处理器的内存指针
    public Long GetSpeexPreprocessState()
    {
        return clSpeexPreprocessState;
    }

    //对一帧音频输入数据进行Speex预处理
    public int Preprocess( short clAudioInput[], Long clVoiceActivityStatus )
    {
        return SpeexPreprocessPreprocess( clSpeexPreprocessState, clAudioInput, clVoiceActivityStatus );
    }

    //销毁Speex预处理器
    private void Destory()
    {
        SpeexPreprocessDestory( clSpeexPreprocessState);
        clSpeexPreprocessState = null;
    }
    public int SpeexPreprocessInit(int iSamplingRate, int iFrameSize, long l){
        Init(iSamplingRate,iFrameSize,1,80,1,30,60,1,20000,1,l,1,30);
        return 0;
    }

    private native int SpeexPreprocessInit( Long clSpeexPreprocessState, int iSamplingRate, int iFrameSize, int iIsUseNs, int iNoiseSuppress, int iIsUseVad, int iVadProbStart, int iVadProbContinue, int iIsUseAgc, int iAgcLevel, int iIsUseRec, long lSpeexEchoState, int iEchoSuppress, int iEchoSuppressActive );
    private native int SpeexPreprocessPreprocess( Long clSpeexPreprocessState, short clAudioInput[], Long clVoiceActivityStatus );
    private native void SpeexPreprocessDestory( Long clSpeexPreprocessState );
}