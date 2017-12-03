package com.nculab.kuoweilun.sdncontrollerapp;

import android.widget.TextView;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class Switch {
    String switchID = "";
    String flow = "";
    SwitchViewHolder holder = new SwitchViewHolder();

    public Switch(String switchID, String flow) {
        this.switchID = switchID;
        this.flow = flow;
    }

    public void setTextviewSwitchID(TextView textview_switchID) {
        holder.textView_switchID = textview_switchID;
    }

    public void setTextviewFlow(TextView textview_flow) {
        holder.textView_flow = textview_flow;
    }
}
