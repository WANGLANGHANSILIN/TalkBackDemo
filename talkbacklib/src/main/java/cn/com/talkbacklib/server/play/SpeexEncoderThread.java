package cn.com.talkbacklib.server.play;


import cn.com.auxdio.speex.Speex;
import cn.com.auxdio.protocol.TalkBackConfig;
import cn.com.talkbacklib.client.SendTransportThread;

public class SpeexEncoderThread extends Thread {

	public static int encoder_packagesize = 1024;

	/**保存每一帧编码后的数据*/
	private byte[] processedData = new byte[encoder_packagesize];
	
	private short[] data = new short[encoder_packagesize];

    private SendTransportThread mSendTransportThread;

    @Override
	public void run() {
		super.run();
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        mSendTransportThread = new SendTransportThread();
		mSendTransportThread.start();
		Speex.init();
	}

	public void encodeData(short[] tempBuffer, int read) {
		System.arraycopy(tempBuffer, 0, data, 0, read);

        int encode = Speex.init().encode(data, 0, processedData, read);
        if (encode > 0 && mSendTransportThread != null)
            mSendTransportThread.sendBroadData(processedData,encode, TalkBackConfig.TALKBACK_PORT);
	}
}
