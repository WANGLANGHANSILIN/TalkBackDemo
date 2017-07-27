package cn.com.talkbacklib.server.play;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

import cn.com.auxdio.protocol.TalkBackConfig;
import cn.com.auxdio.speex.Speex;

/**
 * Created by wang l on 2017/7/7.
 */

public class DecodeRunnable implements Runnable {
//    private DatagramSocket mDatagramSocket;
    public MulticastSocket mMulticastSocket;

    private final Object mObject = new Object();
    private boolean isStop;

    public DecodeRunnable(String byName) {
        try {
            mMulticastSocket = new MulticastSocket(TalkBackConfig.TALKBACK_PORT);
            mMulticastSocket.joinGroup(InetAddress.getByName(byName));
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        PlayAudioRunnable playAudioRunnable = new PlayAudioRunnable();
        playAudioRunnable.setStopPlay(isStop);
        new Thread(playAudioRunnable).start();
        Log.i("ssss", "run: playAudioRunnable start");
        while (!isStop()){
            try {
                byte[] bytes = new byte[1024];
                if (mMulticastSocket != null){
                    DatagramPacket datagramPacket = new DatagramPacket(bytes,bytes.length);
                    mMulticastSocket.receive(datagramPacket);

                    int length = datagramPacket.getLength();
                    String hostAddress = datagramPacket.getAddress().getHostAddress();
                    if (length > 0){
                        synchronized (mObject){
                            short[] shorts = new short[160];
                            Speex.init().decode(datagramPacket.getData(),shorts,shorts.length);
                            playAudioRunnable.putPlayData(shorts,shorts.length);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        playAudioRunnable.setStopPlay(true);
    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }
}
