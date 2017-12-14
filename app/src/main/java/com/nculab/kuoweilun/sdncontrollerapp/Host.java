package com.nculab.kuoweilun.sdncontrollerapp;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class Host {
    String ID = "";
    String IP = "";
    String mac = "";

    public Host(String ID, String mac, String IP) {
        this.ID = ID;
        this.mac = mac;
        this.IP = IP;
    }
}
