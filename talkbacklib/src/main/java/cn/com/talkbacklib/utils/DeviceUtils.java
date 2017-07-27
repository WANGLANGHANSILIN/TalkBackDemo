package cn.com.talkbacklib.utils;

import java.util.List;

import cn.com.talkbacklib.TalkBackHandle;
import cn.com.talkbacklib.bean.DeviceBean;
import cn.com.talkbacklib.client.SendTransportThread;

/**
 * Created by wang l on 2017/7/4.
 */

public class DeviceUtils {
    public static DeviceBean getDevicebyIP(String ip){
        SendTransportThread sendTransportThread = TalkBackHandle.newInstance().getSendTransportThread();
        if (sendTransportThread != null){
            List<DeviceBean> deviceBeanList = sendTransportThread.getDeviceBeanList();
            if (deviceBeanList != null && deviceBeanList.size() > 0){
                for (DeviceBean deviceBean : deviceBeanList) {
                    if (ip.equals(deviceBean.getDevIP()))
                        return deviceBean;
                }
            }
        }
        return null;
    }

    public static int getDeviceBeanindex(List<DeviceBean> deviceBeanList,DeviceBean deviceBean) {
        if (deviceBeanList.size() > 0){
            for (int i = 0; i < deviceBeanList.size(); i++) {
                if (deviceBeanList.get(i).getDevIP().equals(deviceBean.getDevIP()))
                    return i;
            }
        }
//        int indexOf = deviceBeanList.indexOf(deviceBean);
        return -1;
    }
}
