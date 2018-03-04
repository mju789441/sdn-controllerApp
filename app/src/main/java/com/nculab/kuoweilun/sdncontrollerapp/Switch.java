package com.nculab.kuoweilun.sdncontrollerapp;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class Switch {
    public String ID = null;
    public String byte_count = null;

    public Switch(String ID, JSONObject switchObject) {
        this.ID = ID;
        try {
            byte_count = switchObject.getJSONArray(ID)
                    .getJSONObject(0)
                    .getString("byte_count");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
