package com.nculab.kuoweilun.sdncontrollerapp;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.Serializable;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class Host implements Serializable {
    String ID = null;
    String port = null;
    String mac = null;
    String curr_speed = null;

    public Host(String ID, JSONObject hostObject) {
        this.ID = ID;
        try {
            port = hostObject.getString("port_no");
            mac = hostObject.getString("hw_addr");
            curr_speed = hostObject.getString("curr_speed");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
