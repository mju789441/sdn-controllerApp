package com.nculab.kuoweilun.sdncontrollerapp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class Host implements Serializable {
    String ID = null;
    String port = null;
    String mac = null;

    public Host(String ID, JSONObject hostObject) {
        this.ID = ID;
        try {
            this.port = hostObject.getString("port_no");
            this.mac = hostObject.getString("hw_addr");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
