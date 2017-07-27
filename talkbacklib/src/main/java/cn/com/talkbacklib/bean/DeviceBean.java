package cn.com.talkbacklib.bean;

/**
 * Created by wang l on 2017/6/19.
 */

public class DeviceBean {
    private String devIP;
    private String devName;
    private String devMac;
    private int devStatus;
    private boolean isChecked;

    public DeviceBean(String devIP, String devName) {
        this.devIP = devIP;
        this.devName = devName;
    }

    public String getDevIP() {
        return devIP;
    }

    public void setDevIP(String devIP) {
        this.devIP = devIP;
    }

    public String getDevName() {
        return devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }

    public String getDevMac() {
        return devMac;
    }

    public void setDevMac(String devMac) {
        this.devMac = devMac;
    }

    public int getDevStatus() {
        return devStatus;
    }

    public void setDevStatus(int devStatus) {
        this.devStatus = devStatus;
    }

    @Override
    public String toString() {
        return "DeviceBean{" +
                "devIP='" + devIP + '\'' +
                ", devName='" + devName + '\'' +
                ", devMac='" + devMac + '\'' +
                ", devStatus=" + devStatus +
                '}';
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
