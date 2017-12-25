package com.nculab.kuoweilun.sdncontrollerapp;

import java.io.Serializable;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class Flow implements Serializable {
    String switchID = "";
    String port = "";
    String speed = "";
    String rx = "";
    String dx = "";
    String duration = "";
    String duration_nsec = "";
    String rx_errors = "";

    public Flow(String switchID, String port, String speed, String rx, String dx, String duration, String duration_nsec, String rx_errors) {
        this.switchID = switchID;
        this.port = port;
        this.speed = speed;
        this.rx = rx;
        this.dx = dx;
        this.duration = duration;
        this.duration_nsec = duration_nsec;
        this.rx_errors = rx_errors;
    }
}
