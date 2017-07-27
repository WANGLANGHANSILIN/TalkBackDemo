package com.example.Android_audio_talkback_demo_program;
public class WebRtcNsx //WebRtc定点噪音抑制器类
{
    static
    {
        System.loadLibrary( "WebRtcNs" );//加载libSpeex.so
    }
    private Long clWebRtcNsx; //WebRtc定点噪音抑制器的内存指针

    //构造函数
    public WebRtcNsx()
    {
        clWebRtcNsx = new Long(0);
    }

    //析构函数
    public void finalize()
    {
        clWebRtcNsx = null;
    }

    //初始化WebRtc定点噪音抑制器
    public int Init( int iSamplingRate, int iPolicyMode )
    {
        if( clWebRtcNsx.longValue() == 0)//如果WebRtc定点噪音抑制器还没有初始化
        {
            return WebRtcNsxInit( clWebRtcNsx, iSamplingRate, iPolicyMode );
        }
        else//如果WebRtc定点噪音抑制器已经初始化
        {
            return 0;
        }
    }

    //获取WebRtc定点噪音抑制器的内存指针
    public Long GetWebRtcNsx()
    {
        return clWebRtcNsx;
    }

    //对一帧音频输入数据进行WebRtc定点噪音抑制
    public int Process( int iSamplingRate, short clAudioData[], int iAudioDataSize )
    {
        return WebRtcNsxProcess( clWebRtcNsx, iSamplingRate, clAudioData, iAudioDataSize );
    }

    //销毁WebRtc定点噪音抑制器
    public void Destory()
    {
        WebRtcNsxDestory( clWebRtcNsx);
        clWebRtcNsx = null;
    }

    private native int WebRtcNsxInit( Long clWebRtcNsx, int iSamplingRate, int iMode );
    private native int WebRtcNsxProcess( Long clWebRtcNsx, int iSamplingRate, short clAudioData[], int iAudioDataSize );
    private native void WebRtcNsxDestory( Long clWebRtcNsx );
}
