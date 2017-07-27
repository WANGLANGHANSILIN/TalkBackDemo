package cn.com.talkbacklib;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.com.auxdio.protocol.CmdDefine;
import cn.com.auxdio.protocol.TalkBackConfig;
import cn.com.talkbacklib.bean.DeviceBean;
import cn.com.talkbacklib.callback.DeviceBroadcastCallback;
import cn.com.talkbacklib.callback.DeviceOnLineCallback;
import cn.com.talkbacklib.callback.DeviceTalkbackCallback;
import cn.com.talkbacklib.callback.QueryDeviceStatusCallback;
import cn.com.talkbacklib.client.SendTransportThread;
import cn.com.talkbacklib.client.recorder.RecorderRunnable;
import cn.com.talkbacklib.server.ReceiveTransportThread;
import cn.com.talkbacklib.server.play.DecodeRunnable;
import cn.com.talkbacklib.utils.ByteUtils;
import cn.com.talkbacklib.utils.IPUtils;
import cn.com.talkbacklib.utils.PackageUtils;
import cn.com.talkbacklib.utils.StringUtils;

/**
 * Created by wang l on 2017/6/19.
 */

public class TalkBackHandle {

    private SendTransportThread mSendTransportThread;
    private Context context;
    private boolean isTalkBackState = false;//true为对讲状态，false为非对讲状态

    private static TalkBackHandle mBackHandle;
    private ReceiveTransportThread mReceiveTransportThread;
    private RecorderRunnable mRecorderRunnable;
    private DecodeRunnable mDecodeRunnable;
    private String mCurrentMultiAddres;
    private SparseBooleanArray groupIPUseArray;
    private HashMap<String,List<DeviceBean>> mDeviceHashMap;
    private DeviceBean mTalkbackDevice;

    private TalkBackHandle() {
    }

    /**
     * 初始化准备
     * @param talkbackCallback 对讲回调
     * @param broadcastCallback 广播回调
     * @return 返回实例
     */
    public TalkBackHandle startWorking(DeviceTalkbackCallback talkbackCallback,DeviceBroadcastCallback broadcastCallback) {
        startWorking();
        setDeviceTalkbackCallback(talkbackCallback);
        setDeviceBroadcastCallback(broadcastCallback);
        return this;
    }

    /**
     * 初始化准备
     * @return 返回实例
     */
    public TalkBackHandle startWorking() {

        if (context == null)
            throw new NullPointerException("context is null");

        //接受数据线程
        mReceiveTransportThread = new ReceiveTransportThread(context);
        mReceiveTransportThread.start();

        //发送数据线程
        mSendTransportThread = new SendTransportThread();
        mSendTransportThread.start();

        groupIPUseArray =  new SparseBooleanArray(100);
        for (int i = 0; i <= 100; i++) {
            groupIPUseArray.put(i,false);
        }
        mDeviceHashMap = new HashMap<>();
        //对讲录制线程
//        recorderThread = new RecorderThread(mSendTransportThread);

        //对讲接受播放线程
//        mPlayThread = new PlayThread();
        return this;
    }

    /**
     * new一个新的实例
     * @return 返回实例
     */
    public static TalkBackHandle newInstance(){
        if (mBackHandle == null) {
            synchronized (TalkBackHandle.class){
                if (mBackHandle == null){
                    mBackHandle = new TalkBackHandle();
                }
            }
        }
        return mBackHandle;
    }

    //搜索设备

    /**
     * 搜索设备
     * @return 返回当前实例
     */
    public TalkBackHandle sreachDevice(){
        sendBroadCastData(CmdDefine.CMD_DEVICE_BRAOD_CAST,new byte[]{});
        return this;
    }

    //设备上线
    public TalkBackHandle onLineDevice(){
        try {
            String hostAddress1 = IPUtils.getLocalIpAddress(context).getHostAddress();
            String decName = IPUtils.getDeviceName(context);
            String localMacAddress = IPUtils.getLocalMacAddr();

            byte[] bytes = ByteUtils.compoundByte(hostAddress1, decName);
            byte[] bytes1 = ByteUtils.compoundByte(localMacAddress);
            byte[] bytes2 = ByteUtils.compoundByte(bytes, bytes.length, bytes1, bytes1.length);

            byte[] bytes3 = new byte[bytes2.length+1];
            System.arraycopy(bytes2,0,bytes3,0,bytes2.length);
            bytes3[bytes2.length] = isTalkBackState()? CmdDefine.TalkBackStatus.STATUS_BUSY: CmdDefine.TalkBackStatus.STATUS_IDLE;
            Log.i("onLineDevice", "onLineDevice: "+bytes1.length+", localMacAddress:"+localMacAddress+",bytes3:"+bytes3.length);
            sendBroadCastData(CmdDefine.CMD_DEVICE_ONLINE,bytes3);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return this;
    }

    //启动单播对讲
    public void startUnicastTalkback(DeviceBean deviceBean){
        startRecordThread(deviceBean.getDevIP());
        startPlayThread(deviceBean.getDevIP());
    }

    //启动广播
    public void startBroadcastTalkback(String groupAddr){
        startRecordThread(groupAddr);
    }

    //停止单播对讲
    public void stopUnicastTalkback(){
        stopRecordThread();
        stopPlayThread();
    }

    //启动录制线程
    private void startRecordThread(String devIP){
        if (mRecorderRunnable == null)
            mRecorderRunnable = new RecorderRunnable(devIP);
        else
            if(!devIP.equals(mRecorderRunnable.getDevIP())){
                mRecorderRunnable = new RecorderRunnable(devIP);
            }
        mRecorderRunnable.setRecording(true);
        isTalkBackState = true;

        new Thread(mRecorderRunnable).start();
        broadTalkBackState();
    }

    //停止录制线程
    public void stopRecordThread(){
        isTalkBackState = false;
        broadTalkBackState();
        if (mRecorderRunnable != null) {
            mRecorderRunnable.setRecording(false);
            mRecorderRunnable = null;
        }
    }

    //启动播放线程
    public void startPlayThread(String byName){
        isTalkBackState = true;
        broadTalkBackState();
        if (mDecodeRunnable == null)
            mDecodeRunnable = new DecodeRunnable(byName);
        mDecodeRunnable.setStop(false);
        new Thread(mDecodeRunnable).start();
    }

    //停止播放线程
    public TalkBackHandle stopPlayThread(){
        isTalkBackState = false;
        broadTalkBackState();
        if (mDecodeRunnable != null) {
            mDecodeRunnable.setStop(true);
            mDecodeRunnable = null;
        }
        return this;
    }

    //主动广播设备对讲状态
    public TalkBackHandle broadTalkBackState(){
        sendBroadCastData(CmdDefine.CMD_TALKBACK_STATE,new byte[]{(byte) CmdDefine.QueryStatus.STATUS_BROAD,(byte) (isTalkBackState()?3:1)});
        return this;
    }

    //查询设备对讲状态
    public TalkBackHandle queryTalkbackState(DeviceBean deviceBean,QueryDeviceStatusCallback callback){
        setQueryDeviceStatusCallback(callback);
        sendUnistCastData(CmdDefine.CMD_TALKBACK_STATE,new byte[]{(byte) CmdDefine.QueryStatus.STATUS_QUERY},deviceBean.getDevIP());
        return this;
    }

    //发起对讲
    public TalkBackHandle requestTalkback(DeviceBean deviceBean){
        this.mTalkbackDevice = deviceBean;
        sendUnistCastData(CmdDefine.CMD_REQUEST_TALKBACK,new byte[]{},deviceBean.getDevIP());
        return this;
    }

    //响应对讲
    public TalkBackHandle responeTalkback(DeviceBean deviceBean,boolean isConsent){
        this.mTalkbackDevice = deviceBean;
        sendUnistCastDataToRespone(CmdDefine.CMD_REQUEST_TALKBACK,new byte[]{(byte) (isConsent?0x01:0x02)},deviceBean.getDevIP());
        if (isConsent)
            startUnicastTalkback(deviceBean);
        return this;
    }

    //停止对讲
    public TalkBackHandle stopTalkback(){
        if (mTalkbackDevice != null){
            sendUnistCastData(CmdDefine.CMD_EXIT_TALKBACK,new byte[]{},mTalkbackDevice.getDevIP());
            stopUnicastTalkback();
        }
        return this;
    }

    private String getMultiAddres(){
        LinkedList<String> groupIPLinkedList = mReceiveTransportThread.mGroupIPLinkedList;
        if (groupIPLinkedList != null && groupIPLinkedList.size() > 0){
            for (String s : groupIPLinkedList) {
                groupIPUseArray.put(StringUtils.getGroupAddressLastNum(s),true);
            }
        }
        int ofValue = groupIPUseArray.indexOfValue(false);
        return StringUtils.getGroupAddress__()+groupIPUseArray.keyAt(ofValue);
    }

    private String getMultiAddresByDeviceBean(DeviceBean deviceBean){
        if (mDeviceHashMap == null || mDeviceHashMap.size() == 0)
            return "";
        Iterator<Map.Entry<String, List<DeviceBean>>> entryIterator = mDeviceHashMap.entrySet().iterator();
        if (entryIterator == null)
            return "";
        while (entryIterator.hasNext()){
            Map.Entry<String, List<DeviceBean>> next = entryIterator.next();
            List<DeviceBean> value = next.getValue();
            for (DeviceBean bean : value) {
                if (deviceBean.getDevIP().equals(bean.getDevIP()))
                    return next.getKey();
            }
        }
        return "";
    }

    //发起广播
    public TalkBackHandle requestBroadCast(List<DeviceBean> deviceBeanList){
        mCurrentMultiAddres = getMultiAddres();
        Log.i("TalkBackHandle", "requestBroadCast: 发起广播所使用的组播地址-mCurrentMultiAddres:"+mCurrentMultiAddres);
        mDeviceHashMap.put(mCurrentMultiAddres,deviceBeanList);
        for (DeviceBean deviceBean : deviceBeanList) {
            requestBroadCast(deviceBean);
        }
        return this;
    }

    //发起广播
    private void requestBroadCast(DeviceBean deviceBean){
        sendUnistCastData(CmdDefine.CMD_REQUEST_BROAD,new byte[]{0x01},deviceBean.getDevIP());
    }

    //响应广播
    public TalkBackHandle responeBroadcast(DeviceBean deviceBean, boolean talkBackState){
        byte b = 0x00;
        if (talkBackState && isTalkBackState)
            b = 0x02;
        else
            b = 0x01;
        sendUnistCastDataToRespone(CmdDefine.CMD_REQUEST_BROAD,new byte[]{b},deviceBean.getDevIP());
        return this;
    }

    //停止广播
    public TalkBackHandle stopBroadCast(){
        try {
            if (mCurrentMultiAddres == null) {
                return this;
            }
            sendMulticastData(CmdDefine.CMD_EXIT_BROAD,ByteUtils.compoundByte(mCurrentMultiAddres),mCurrentMultiAddres);
            mDeviceHashMap.remove(mCurrentMultiAddres);
            stopRecordThread();
            TalkBackHandle.newInstance().notifyGroupAddressChanged(mCurrentMultiAddres,false);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return this;
    }

    //加入组播
    public void joinGroup(DeviceBean deviceBean){
        try {
            String multicastAddr = getMultiAddresByDeviceBean(deviceBean);
            Log.i("leaveGroup", "joinGroup: mCurrentMultiAddres:"+multicastAddr);
            byte[] bytes = ByteUtils.compoundByte(multicastAddr);
            sendUnistCastData(CmdDefine.CMD_ADD_GROUP,bytes,deviceBean.getDevIP());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    //响应加入组播
    public void responeJoinGroup(DeviceBean deviceBean, String groupAddr) throws UnsupportedEncodingException {
        mCurrentMultiAddres = groupAddr;
        Log.i("leaveGroup", "responeJoinGroup: mCurrentMultiAddres:"+mCurrentMultiAddres);
        byte[] bytes = ByteUtils.compoundByte(groupAddr);
        byte[] bytes1 = ByteUtils.compoundByte(new byte[]{0x01}, 1, bytes, bytes.length);
        sendUnistCastDataToRespone(CmdDefine.CMD_ADD_GROUP,bytes1,deviceBean.getDevIP());
    }

    //退出组播
    public void leaveGroup(){
        Log.i("leaveGroup", "leaveGroup: mCurrentMultiAddres:"+mCurrentMultiAddres);
        sendMulticastData(CmdDefine.CMD_EXIT_GROUP,new byte[0],mCurrentMultiAddres);
        stopPlayThread();
        /*
        try {
            Thread.sleep(20);
            InetAddress inetAddress = InetAddress.getByName(mCurrentMultiAddres);
            mDecodeRunnable.mMulticastSocket.leaveGroup(inetAddress);
            mReceiveTransportThread.mDatagramSocket.leaveGroup(inetAddress);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
    }

    public void notifyGroupAddressChanged(String groupAddr,boolean isUse){
        byte[] bytes = new byte[0];
        try {
            bytes = ByteUtils.compoundByte(groupAddr);
            byte[] bytes1 = ByteUtils.compoundByte(new byte[]{(byte) (isUse ? 1 : 2)}, 1, bytes, bytes.length);
            sendBroadCastData(CmdDefine.CMD_GROUP_ADDR_CHANGE,bytes1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void sendMulticastData(byte cmd, byte[] bytes1, String devIP) {
        mSendTransportThread.sendMulticastData(PackageUtils.requestPackage(cmd,bytes1),devIP, TalkBackConfig.TRANSPORT_TAGET_PORT);
    }

    private void sendBroadCastData(byte cmd, byte[] bytes){
        sendBroadCastData(PackageUtils.requestPackage(cmd,bytes));
    }

    private void sendBroadCastData(byte[] bytes){
        mSendTransportThread.sendBroadData(bytes,bytes.length, TalkBackConfig.TRANSPORT_TAGET_PORT);
    }

    private void sendUnistCastData(byte cmd,byte[] bytes,String devIP){
        sendUnistCastData(PackageUtils.requestPackage(cmd,bytes),devIP);
    }

    private void sendUnistCastData(byte[] bytes,String devIP){
        mSendTransportThread.sendUnicastData(bytes,bytes.length,devIP, TalkBackConfig.TRANSPORT_TAGET_PORT);
    }

    private void sendUnistCastDataToRespone(byte cmd,byte[] bytes,String devIP){
        sendUnistCastDataToRespone(PackageUtils.requestPackage(cmd,bytes),devIP);
    }

    private void sendUnistCastDataToRespone(byte[] bytes,String devIP){
        mSendTransportThread.sendUnicastData(bytes,bytes.length,devIP, TalkBackConfig.TRANSPORT_SEND_PORT);
    }

    public TalkBackHandle setContext(Context context) {
        this.context = context;
        return this;
    }

    public Context getContext() {
        return context;
    }

    public boolean isTalkBackState() {
        return isTalkBackState;
    }

    public SendTransportThread getSendTransportThread() {
        return mSendTransportThread;
    }

    public ReceiveTransportThread getReceiveTransportThread() {
        return mReceiveTransportThread;
    }

    public RecorderRunnable getRecorderRunnable() {
        return mRecorderRunnable;
    }

    public DecodeRunnable getDecodeRunnable() {
        return mDecodeRunnable;
    }

    /****************************************************/

    private DeviceOnLineCallback mDeviceOnLineCallback;
    private QueryDeviceStatusCallback mQueryDeviceStatusCallback;
    private DeviceTalkbackCallback mDeviceTalkbackCallback;
    private DeviceBroadcastCallback mDeviceBroadcastCallback;

    public DeviceOnLineCallback getDeviceOnLineCallback() {
        return mDeviceOnLineCallback;
    }

    public void setDeviceOnLineCallback(DeviceOnLineCallback deviceOnLineCallback) {
        mDeviceOnLineCallback = deviceOnLineCallback;
    }

    public QueryDeviceStatusCallback getQueryDeviceStatusCallback() {
        return mQueryDeviceStatusCallback;
    }

    public void setQueryDeviceStatusCallback(QueryDeviceStatusCallback queryDeviceStatusCallback) {
        mQueryDeviceStatusCallback = queryDeviceStatusCallback;
    }

    public DeviceTalkbackCallback getDeviceTalkbackCallback() {
        return mDeviceTalkbackCallback;
    }

    public void setDeviceTalkbackCallback(DeviceTalkbackCallback deviceTalkbackCallback) {
        mDeviceTalkbackCallback = deviceTalkbackCallback;
    }

    public DeviceBroadcastCallback getDeviceBroadcastCallback() {
        return mDeviceBroadcastCallback;
    }

    public void setDeviceBroadcastCallback(DeviceBroadcastCallback deviceBroadcastCallback) {
        mDeviceBroadcastCallback = deviceBroadcastCallback;
    }
}
