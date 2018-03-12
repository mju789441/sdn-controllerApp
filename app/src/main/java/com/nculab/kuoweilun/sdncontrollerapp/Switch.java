package com.nculab.kuoweilun.sdncontrollerapp;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class Switch {
    public String ID = null;
    public String curr_speed = null;
    public JSONObject jsonObject = null;

    public Switch(String ID, JSONObject switchObject) {
        this.ID = ID;
        jsonObject = switchObject;
        try {
            curr_speed = switchObject.getJSONArray(ID)
                    .getJSONObject(0)
                    .getString("curr_speed");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
