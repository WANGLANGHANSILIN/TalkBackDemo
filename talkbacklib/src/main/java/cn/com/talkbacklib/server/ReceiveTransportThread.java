package cn.com.talkbacklib.server;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.com.auxdio.protocol.CmdDefine;
import cn.com.auxdio.protocol.TalkBackConfig;
import cn.com.talkbacklib.TalkBackHandle;
import cn.com.talkbacklib.bean.DeviceBean;
import cn.com.talkbacklib.callback.DeviceBroadcastCallback;
import cn.com.talkbacklib.utils.ByteUtils;
import cn.com.talkbacklib.utils.DeviceUtils;
import cn.com.talkbacklib.utils.IPUtils;
import cn.com.talkbacklib.utils.PackageUtils;
import cn.com.talkbacklib.utils.StringUtils;


/**
 * Created by wang l on 2017/6/22.
 */

public class ReceiveTransportThread extends Thread {

    private static final String TAG = ReceiveTransportThread.class.getSimpleName();
    private Context mContext;
    public MulticastSocket mDatagramSocket;
    private String mLocalIpAddress;
    public LinkedList<String> mGroupIPLinkedList;
    private List<DeviceBean> joinGroupDeviceList;

    public ReceiveTransportThread(Context context) {
        try {
            this.mContext = context;
            mDatagramSocket = new MulticastSocket(TalkBackConfig.TRANSPORT_TAGET_PORT);
            mLocalIpAddress = IPUtils.getLocalIpAddress(mContext).getHostAddress();
            mGroupIPLinkedList = new LinkedList<>();
            joinGroupDeviceList = new ArrayList<>();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();

        while (true){
            try {
                if (mDatagramSocket != null){
                    byte[] bytes = new byte[1024];
                    DatagramPacket datagramPacket = new DatagramPacket(bytes,bytes.length);
                    mDatagramSocket.receive(datagramPacket);

                    int length = datagramPacket.getLength();
                    if (length > 0){
                        handleCmdData(datagramPacket.getData(),length,datagramPacket.getAddress().getHostAddress(),datagramPacket.getPort());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleCmdData(byte[] data, int length, String hostAddress, int port) throws IOException {
        Log.i("ServerPort","receiveData---hostAddress:"+hostAddress+",port:"+port+",dataLen:"+length+"    "+data[0]+data[1]);
        Log.i("ServerPort","receiveData---"+ PackageUtils.printData(data,length));

        if (mLocalIpAddress == null)
            return;
        if (hostAddress.equals(mLocalIpAddress))//当前为本机设备，不执行任何操作
            return;
        DeviceBean deviceBean = DeviceUtils.getDevicebyIP(hostAddress);
        DeviceBroadcastCallback deviceBroadcastCallback = TalkBackHandle.newInstance().getDeviceBroadcastCallback();
        switch (PackageUtils.compoundCmd(data)){
            case CmdDefine.CMD_DEVICE_BRAOD_CAST://client设备广播搜索设备
                responseDeviceBroad(data, hostAddress, port);
                break;
            case CmdDefine.CMD_DEVICE_ONLINE://client设备上线通知
                TalkBackHandle.newInstance().getSendTransportThread().handleDeviceList(data);
                break;
            case CmdDefine.CMD_TALKBACK_STATE://client查询对讲状态和client主动广播对讲状态
                responseDeviceState(data, hostAddress, port);
                break;
            case CmdDefine.CMD_REQUEST_TALKBACK://client请求对讲
                if (deviceBean != null && !TalkBackHandle.newInstance().isTalkBackState())
                    TalkBackHandle.newInstance().getDeviceTalkbackCallback().onReceiveTalkback(deviceBean);
                else {
                    if (deviceBean != null)
                        TalkBackHandle.newInstance().responeTalkback(deviceBean,false);
                    Log.i("CMD_REQUEST_TALKBACK", "handleCmdData: 设备处于忙碌中...或者该设备不存在...");
//                    TalkBackHandle.newInstance().getDeviceTalkbackCallback().onErrorTalkback("设备处于忙碌中...或者该设备不存在...");
                }
                break;
            case CmdDefine.CMD_EXIT_TALKBACK://client退出对讲
                if (deviceBean != null) {
                    TalkBackHandle.newInstance().stopUnicastTalkback();
                    TalkBackHandle.newInstance().getDeviceTalkbackCallback().onExitTalkback(deviceBean);
                }
                break;

            case CmdDefine.CMD_REQUEST_BROAD://client请求广播
                if (deviceBean != null){
                    TalkBackHandle.newInstance().responeBroadcast(deviceBean,TalkBackHandle.newInstance().isTalkBackState());
                }
//                    TalkBackHandle.newInstance().getDeviceTalkbackCallback().onErrorTalkback("该设备不存在...");
                break;

            case CmdDefine.CMD_EXIT_BROAD://client退出广播
                String groupAddr1 = StringUtils.convetString(data, 6);
                TalkBackHandle.newInstance().stopPlayThread();
                if (deviceBroadcastCallback != null) {
                    deviceBroadcastCallback.onExitBroadcast(DeviceUtils.getDevicebyIP(hostAddress));
                }
                InetAddress inetAddress = InetAddress.getByName(groupAddr1);
                mDatagramSocket.leaveGroup(inetAddress);
                TalkBackHandle.newInstance().getDecodeRunnable().mMulticastSocket.joinGroup(inetAddress);
                TalkBackHandle.newInstance().getSendTransportThread().mMulticastSocket.leaveGroup(inetAddress);
                break;

            case CmdDefine.CMD_ADD_GROUP://client请求加入组播
                String groupAddr = StringUtils.convetString(data, 6);
                mDatagramSocket.joinGroup(InetAddress.getByName(groupAddr));
                TalkBackHandle.newInstance().startPlayThread(groupAddr);
                if (deviceBean != null){
                    joinGroupDeviceList.add(deviceBean);
                    TalkBackHandle.newInstance().responeJoinGroup(deviceBean,groupAddr);
                }
//                TalkBackHandle.newInstance().getDecodeRunnable().mMulticastSocket.joinGroup(InetAddress.getByName(groupAddr));
                break;

            case CmdDefine.CMD_EXIT_GROUP://client请求退出组播
                if (deviceBean != null){
                    joinGroupDeviceList.remove(deviceBean);
                    if (deviceBroadcastCallback != null)
                        deviceBroadcastCallback.onExitMulticast(deviceBean);
                    if (joinGroupDeviceList.size() == 0){
                        TalkBackHandle.newInstance().stopRecordThread();
                    }
                }
                Log.i(TAG, "handleCmdData: 设备IP为："+hostAddress+" 退出组播...");
                break;

            case CmdDefine.CMD_GROUP_ADDR_CHANGE://组播地址使用改变通知
                String groupAddr2 = StringUtils.convetString(data, 7);
                if ((data[6] & 0xFF) == 0x01){
                    mGroupIPLinkedList.add(groupAddr2);
                }else
                    mGroupIPLinkedList.remove(groupAddr2);

                break;
        }

    }

    //设备对讲状态响应
    public void responseDeviceState(byte[] data, String hostAddress, int port) throws IOException {
        if(CmdDefine.QueryStatus.STATUS_BROAD == data[6]){ //其他状态
            DeviceBean devicebyIP = DeviceUtils.getDevicebyIP(hostAddress);
            if (devicebyIP != null) {
                devicebyIP.setDevStatus(data[7] & 0xFF);
                TalkBackHandle.newInstance().getDeviceOnLineCallback().onDeviceLine(devicebyIP);
            }
        }else if(CmdDefine.QueryStatus.STATUS_QUERY == data[6]){ //查询状态
            byte[] bytes = PackageUtils.requestPackage(CmdDefine.CMD_TALKBACK_STATE, new byte[]{(byte) CmdDefine.QueryStatus.STATUS_BROAD,(byte) (TalkBackHandle.newInstance().isTalkBackState() ? 3 : 1)});
            sendData(hostAddress,port,bytes);
        }
    }

    //响应设备广播
    private void responseDeviceBroad(byte[] data, String hostAddress, int port) throws IOException {
        byte[] bytes1 = ByteUtils.compoundByte(mLocalIpAddress, IPUtils.getDeviceName(mContext));
        String localMacAddress = IPUtils.getLocalMacAddr();
        byte[] bytes4 = ByteUtils.compoundByte(localMacAddress);

        byte[] bytes5 = ByteUtils.compoundByte(bytes1, bytes1.length, bytes4, bytes4.length);
        byte[] bytes6 = new byte[bytes5.length+1];
        System.arraycopy(bytes5,0,bytes6,0,bytes5.length);
        bytes6[bytes5.length] = TalkBackHandle.newInstance().isTalkBackState()? CmdDefine.TalkBackStatus.STATUS_BUSY: CmdDefine.TalkBackStatus.STATUS_IDLE;

        Log.i("responseDeviceBroad", bytes4.length+",responseDeviceBroad: localMacAddress:"+localMacAddress+",bytes6:"+bytes6.length);

        byte[] bytes3 = new byte[6];
        System.arraycopy(data,0,bytes3,0,5);
        bytes3[5] = (byte) bytes6.length;

        byte[] bytes2 = ByteUtils.compoundByte(bytes3,bytes3.length,bytes6,bytes6.length);
        sendData(hostAddress, TalkBackConfig.TRANSPORT_SEND_PORT, bytes2);
    }

    private void sendData(String hostAddress, int port, byte[] bytes) throws IOException {
        DatagramPacket packet = new DatagramPacket(bytes,bytes.length, InetAddress.getByName(hostAddress),port);
        Log.i("handleCmdData","sendData---hostAddress:"+packet.getAddress().getHostAddress()+",port:"+packet.getPort()+",dataLen:"+packet.getLength()+"    "+packet.getData()[1]);
        Log.i("handleCmdData","sendData---"+ PackageUtils.printData(packet.getData(),packet.getLength()));
        sendData(packet);
    }

    private void sendData(DatagramPacket packet) throws IOException {
        if (mDatagramSocket != null)
            mDatagramSocket.send(packet);
    }
}
