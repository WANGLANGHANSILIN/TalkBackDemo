package cn.com.talkbacklib.callback;

import cn.com.talkbacklib.bean.DeviceBean;

/**
 * Created by wang l on 2017/7/4.
 */

public interface QueryDeviceStatusCallback {
    void onDeviceStatus(DeviceBean deviceBean,boolean isBusy);
}
