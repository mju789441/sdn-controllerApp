package com.nculab.kuoweilun.sdncontrollerapp;

import java.io.Serializable;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class Host implements Serializable {
    String ID = "";
    String port = "";
    String mac = "";
    String IP = "";

    public Host(String ID, String port, String mac, String IP) {
        this.ID = ID;
        this.port = port;
        this.mac = mac;
        this.IP = IP;
    }
}
