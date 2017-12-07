package com.nculab.kuoweilun.sdncontrollerapp;

import android.widget.TextView;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class Switch {
    String switchID = "";
    String flow = "";
    SwitchAdapter adapter;

    public Switch(String switchID, String flow, SwitchAdapter adapter) {
        this.switchID = switchID;
        this.flow = flow;
        this.adapter = adapter;
    }
}
