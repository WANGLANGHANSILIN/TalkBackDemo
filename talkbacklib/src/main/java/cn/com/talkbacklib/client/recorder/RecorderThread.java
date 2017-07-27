package cn.com.talkbacklib.client.recorder;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;


import cn.com.auxdio.protocol.TalkBackConfig;
import cn.com.auxdio.speex.Speex;
import cn.com.talkbacklib.TalkBackHandle;
import cn.com.talkbacklib.bean.AuxdioDataBean;
import cn.com.talkbacklib.bean.DeviceBean;
import cn.com.talkbacklib.client.SendTransportThread;

public class RecorderThread extends Thread {

	private int frequency = 8000;
	private static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

	public static int bitrate = 160;
	private volatile boolean isRecording = false;

	public static int encoder_packagesize = 1024;
	private byte[] processedData = new byte[encoder_packagesize];
	private short[] data = new short[encoder_packagesize];
	private SendTransportThread mSendTransportThread;
	private DeviceBean deviceBean;
	private AcousticEchoCanceler canceler;//回声消除
	private short[] audioResule;

	public RecorderThread(SendTransportThread sendTransportThread) {
		mSendTransportThread = sendTransportThread;
	}

	@Override
	public void run() {
		super.run();
		
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

		AudioManager audioManager = (AudioManager) TalkBackHandle.newInstance().getContext().getSystemService(Context.AUDIO_SERVICE);
		audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		audioManager.setSpeakerphoneOn(true);

		int bufferSize = AudioRecord.getMinBufferSize(frequency, AudioFormat.CHANNEL_IN_MONO, audioEncoding);

		Speex.init();//初始化Spexx
//		SpeexAec speexAec = new SpeexAec();
//		speexAec.Init(bufferSize,frequency,bufferSize*2);


		AudioRecord recordInstance = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, frequency, AudioFormat.CHANNEL_IN_MONO, audioEncoding,bufferSize);
		AuxdioDataBean.setAuxdioSessionId(recordInstance.getAudioSessionId());
		initAEC(recordInstance.getAudioSessionId());
		recordInstance.startRecording();

		audioResule = new short[bufferSize];

		while(isRecording()){
			short[] tempBuffer = new short[bufferSize];

			int read = recordInstance.read(tempBuffer, 0, bitrate);
//			for (int i = 0; i < bitrate; i++) {
//				if (tempBuffer[i] == 0)
//					break;
//			}

//			short[] shorts = new short[bufferSize];
//			int read1 = recordInstance.read(shorts, 0, bitrate);
//			AuxdioDataBean.AddAuxdioInput(shorts);
//
//			short[] fistAudioOutput = AuxdioDataBean.getFistAudioOutput();
//			short[] fistAudioInput = AuxdioDataBean.getFistAudioInput();
//
//			if (fistAudioOutput != null && fistAudioInput != null) {
//				speexAec.Aec(fistAudioInput, fistAudioOutput,audioResule);
//				encodeData(audioResule,audioResule.length);
//			}else
//				encodeData(shorts,read1);

			encodeData(tempBuffer,read);
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}

		recordInstance.stop();
		recordInstance.release();
		Speex.init().close();
		canceler.release();
	}

	public boolean isRecording() {
		return isRecording;
	}

	public void setRecording(boolean isRecording) {
		this.isRecording = isRecording;
	}


	private void encodeData(short[] tempBuffer, int read) {
//		System.arraycopy(tempBuffer, 0, data, 0, read);

		int encode = Speex.init().encode(tempBuffer, 0, processedData, read);
		if (encode > 0 && mSendTransportThread != null){
			if (deviceBean == null)
				mSendTransportThread.sendBroadData(processedData,encode, TalkBackConfig.TALKBACK_PORT);
			else
				mSendTransportThread.sendUnicastData(processedData,encode,deviceBean.getDevIP(),TalkBackConfig.TALKBACK_PORT);
		}
	}

	public void setTalkbackDevice(DeviceBean deviceBean) {
		this.deviceBean = deviceBean;
	}

	public boolean initAEC(int audioSession) {
		if (canceler != null) {
			return false;
		}
		canceler = AcousticEchoCanceler.create(audioSession);
		canceler.setEnabled(true);
		return canceler.getEnabled();
	}

	public static boolean isDeviceSupport() {
		return AcousticEchoCanceler.isAvailable();
	}


}
