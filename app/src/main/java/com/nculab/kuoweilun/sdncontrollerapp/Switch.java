package com.nculab.kuoweilun.sdncontrollerapp;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class Switch {
    public String ID = null;
    public String speed = null;

    public Switch(String ID, int speed) {
        this.ID = ID;
        this.speed = String.valueOf(speed);
    }
}
