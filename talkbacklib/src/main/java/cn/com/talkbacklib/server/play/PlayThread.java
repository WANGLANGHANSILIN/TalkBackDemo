package cn.com.talkbacklib.server.play;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import cn.com.auxdio.speex.Speex;
import cn.com.talkbacklib.TalkBackHandle;
import cn.com.talkbacklib.bean.AuxdioDataBean;

import static cn.com.auxdio.protocol.TalkBackConfig.TALKBACK_PORT;

public class PlayThread extends Thread{

	private DatagramSocket socket;
	private AudioTrack track;
	private int sampleRate = 8000;
	private int mMinBufferSize;



	public PlayThread() {
		super();
	}

	@Override
	public void run() {
		super.run();
		Speex.init();
		mMinBufferSize = AudioTrack.getMinBufferSize(sampleRate,
				AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT);

		Log.i(Speex.SPEEX_LOG, "player's minBufferSize = "+ mMinBufferSize);

		track = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
				sampleRate,
				AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT,
				mMinBufferSize,
				AudioTrack.MODE_STREAM,
				AuxdioDataBean.getmAudioSessionId());

		while(TalkBackHandle.newInstance().isTalkBackState()){
			byte[] data = new byte[1024];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try {
				if (socket == null)
					this.socket = new DatagramSocket(TALKBACK_PORT);
				socket.receive(packet);
				byte[] data2 = packet.getData();
				Log.i(getName(), "ReceivceData:"+packet.getLength()+"  "+data2);

				if(data2.length > 0 && packet.getLength() > 0){
					short[] decoded = new short[mMinBufferSize];
					int decode = Speex.init().decode(data2, decoded, packet.getLength());

					AuxdioDataBean.AddAuxdioOutput(decoded);

					track.play();
					Log.i(getName(), "ReceivceData:decode"+decode+"  "+data2);
					if (decode > 0) {
						track.write(decoded, 0, decode);
						Log.i(getName(), "ReceivceData:decode"+decode+"  "+data2);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Speex.init().close();
		track.release();
		track = null;
		socket.close();
		socket = null;
	}
}
