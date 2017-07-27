package cn.com.talkbacklib.client;

import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

import cn.com.auxdio.protocol.CmdDefine;
import cn.com.auxdio.protocol.TalkBackConfig;
import cn.com.talkbacklib.TalkBackHandle;
import cn.com.talkbacklib.bean.DeviceBean;
import cn.com.talkbacklib.client.recorder.RecorderRunnable;
import cn.com.talkbacklib.utils.DeviceUtils;
import cn.com.talkbacklib.utils.PackageUtils;
import cn.com.talkbacklib.utils.StringUtils;


public class SendTransportThread extends Thread{

	public MulticastSocket mMulticastSocket;
	private List<DeviceBean> mDeviceBeanList;
	private InetAddress mInetAddress;

	public SendTransportThread() {
		try {
			mDeviceBeanList = new ArrayList<>();
			mMulticastSocket = new MulticastSocket(TalkBackConfig.TRANSPORT_SEND_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		super.run();

		while (true){
			try {
				if (mMulticastSocket != null){
					byte[] bytes = new byte[1024];
					DatagramPacket datagramPacket = new DatagramPacket(bytes,bytes.length);
					mMulticastSocket.receive(datagramPacket);

					int length = datagramPacket.getLength();
					Log.i("SendThread", "run: "+length);
					if (length > 0){
						handleReceiveCmdData(datagramPacket.getData(),length,datagramPacket.getAddress().getHostAddress());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleReceiveCmdData(byte[] data, int length, String hostAddress) throws IOException {
		Log.i("ClientPort","receiveData---hostAddress:"+hostAddress+",dataLen:"+length+"    "+data[0]+data[1]);
		Log.i("ClientPort","receiveData---"+PackageUtils.printData(data,length));
		DeviceBean devicebyIP = DeviceUtils.getDevicebyIP(hostAddress);
		switch (PackageUtils.compoundCmd(data)){
			case CmdDefine.CMD_DEVICE_BRAOD_CAST://server响应设备上线
				handleDeviceList(data);
				break;

			case CmdDefine.CMD_TALKBACK_STATE://server响应对讲状态
				if(CmdDefine.QueryStatus.STATUS_BROAD == data[6]){ //其他状态
					if (devicebyIP != null) {
						devicebyIP.setDevStatus(data[7] & 0xFF);
						TalkBackHandle.newInstance().getQueryDeviceStatusCallback().onDeviceStatus(devicebyIP,devicebyIP.getDevStatus() == 0x03);
					}
				}

			case CmdDefine.CMD_REQUEST_TALKBACK://server响应对讲
				if (data[6] == 0x01){
					if (devicebyIP != null)
						TalkBackHandle.newInstance().startUnicastTalkback(devicebyIP);
					else
						Log.i("CMD_REQUEST_TALKBACK","该设备不存在...不能进行对讲...");
				}else
					Log.i("CMD_REQUEST_TALKBACK","该设备拒绝对讲了......");
				break;

            case CmdDefine.CMD_REQUEST_BROAD://server响应广播状态
                if (data[6] == 0x01){
                    if (devicebyIP != null)
                        TalkBackHandle.newInstance().joinGroup(devicebyIP);
                    else
                        Log.i("CMD_REQUEST_TALKBACK","该设备不存在...不能进行对讲...");
                }else
                    Log.i("CMD_REQUEST_TALKBACK","该设备拒绝对讲了......");
                break;

			case CmdDefine.CMD_ADD_GROUP://server响应加入组状态
				if ((data[6] & 0xFF) == 0x01) {
					String convetString = StringUtils.convetString(data, 7);
					Log.i("CMD_ADD_GROUP", "handleReceiveCmdData: "+hostAddress+" 成功加入组 "+convetString);
					TalkBackHandle.newInstance().notifyGroupAddressChanged(convetString,true);
					RecorderRunnable recorderRunnable = TalkBackHandle.newInstance().getRecorderRunnable();
					if (recorderRunnable != null){
						String devIP = recorderRunnable.getDevIP();
						if (devIP == null || !convetString.equals(devIP)) {
							TalkBackHandle.newInstance().startBroadcastTalkback(convetString);
							mMulticastSocket.joinGroup(InetAddress.getByName(convetString));
						}
					}else
						TalkBackHandle.newInstance().startBroadcastTalkback(convetString);
				}
				else
					Log.i("CMD_ADD_GROUP", "handleReceiveCmdData: "+hostAddress+" 加入组失败...");

				break;
		}
	}

	public void handleDeviceList(byte[] data) throws UnsupportedEncodingException {
		int index = 5;
		int packLen = data[index++] & 0xFF;

		int ipLen = data[index] & 0xFF;
		String ip = StringUtils.convetString(data,index);
		index+=ipLen+1;

		int nameLen = data[index] & 0xFF;
		String name = StringUtils.convetString(data,index);
		index+=nameLen+1;

		Log.i("SendThread", "handleReceiveCmdData: IP:"+ip+",ipLen"+ipLen+",Name:"+name+",nameLen"+nameLen+",packLen:"+packLen+",index:"+index);

		int macLen = data[index] & 0xFF;
		String mac = StringUtils.convetString(data,index);
		index+=macLen+1;

		int deviceState = data[index] & 0xFF;

		DeviceBean deviceBean = new DeviceBean(ip,name);
		deviceBean.setDevMac(mac);
		deviceBean.setDevStatus(deviceState);
		int beanindex = DeviceUtils.getDeviceBeanindex(mDeviceBeanList, deviceBean);

		if (beanindex >= 0){
			mDeviceBeanList.set(beanindex,deviceBean);
		}else
			mDeviceBeanList.add(deviceBean);
		TalkBackHandle.newInstance().getDeviceOnLineCallback().onDeviceLine(deviceBean);
	}

	public List<DeviceBean> getDeviceBeanList() {
		return mDeviceBeanList;
	}

	public void sendTalkbackData(DatagramPacket packet) throws IOException {
		mMulticastSocket.send(packet);
	}

	public void sendBroadData(byte[] data, int size, int port) {
		try {
			DatagramPacket datagramPacket = new DatagramPacket(data,size,InetAddress.getByName(TalkBackConfig.BROADCASE_ADDR),port);
			sendData(datagramPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendUnicastData(byte[] bytes,int len,String ip,int port){
		try {
			sendData(bytes,len,ip,port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendData(byte[] data, final int len, String iP, int port) throws IOException {
		final DatagramPacket packet = new DatagramPacket(data, len, InetAddress.getByName(iP),port);
		Log.i("sendData","port:"+packet.getPort()+",length:"+packet.getLength() +",len:"+len+(Thread.currentThread() == Looper.getMainLooper().getThread()));
		sendData(packet);
	}

	private void sendData(final DatagramPacket packet) throws IOException {
		if (Thread.currentThread() == Looper.getMainLooper().getThread()){
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
                        mMulticastSocket.send(packet);
						Thread.sleep(20);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}else
            mMulticastSocket.send(packet);
	}

	public void onDestroy(){
		if (mMulticastSocket != null){
			try {
				mMulticastSocket.leaveGroup(mInetAddress);
				mMulticastSocket.close();
				mMulticastSocket = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendMulticastData(final byte[] bytes, final String groupIP, final int transportTagetPort) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mInetAddress = InetAddress.getByName(groupIP);
					DatagramPacket datagramPacket = new DatagramPacket(bytes,bytes.length,mInetAddress,transportTagetPort);
					mMulticastSocket.joinGroup(mInetAddress);
					mMulticastSocket.send(datagramPacket);
					Thread.sleep(2);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}finally {
					try {
						mMulticastSocket.leaveGroup(mInetAddress);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
}
