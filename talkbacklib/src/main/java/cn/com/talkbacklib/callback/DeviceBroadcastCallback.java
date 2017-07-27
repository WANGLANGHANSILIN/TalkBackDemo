package cn.com.talkbacklib.callback;

import cn.com.talkbacklib.bean.DeviceBean;

/**
 * Created by wang l on 2017/7/17.
 */

public interface DeviceBroadcastCallback {

    void onExitBroadcast(DeviceBean deviceBean);

    void onExitMulticast(DeviceBean deviceBean);


}
