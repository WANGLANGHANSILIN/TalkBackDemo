package cn.com.talkbacklib.callback;

import cn.com.talkbacklib.bean.DeviceBean;

/**
 * Created by wang l on 2017/7/14.
 */

public interface DeviceTalkbackCallback {
    void onReceiveTalkback(DeviceBean deviceBean);//接受对讲

    void onExitTalkback(DeviceBean deviceBean);//退出对讲

    void onErrorTalkback(String errorMsg);

}
