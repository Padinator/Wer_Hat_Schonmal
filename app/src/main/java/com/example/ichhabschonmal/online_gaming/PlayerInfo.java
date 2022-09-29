package com.example.ichhabschonmal.online_gaming;

import java.io.Serializable;

public class PlayerInfo implements Serializable {

    String IP;
    String deviceName;

    PlayerInfo(String IP, String deviceName) {
        this.IP = IP;
        this.deviceName = deviceName;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public String toString() {
        return "PlayerInfo{" +
                "IP='" + IP + '\'' +
                ", deviceName='" + deviceName + '\'' +
                '}';
    }
}
