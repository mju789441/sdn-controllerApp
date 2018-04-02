package com.nculab.kuoweilun.sdncontrollerapp;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * Created by kuo on 2018/3/25.
 */

public class Subscribe {
    AppFile appFile;
    ControllerURLConnection controllerURLConnection;

    public Subscribe(AppFile appFile, ControllerURLConnection controllerURLConnection) {
        this.appFile = appFile;
        this.controllerURLConnection = controllerURLConnection;
    }

    public void subscrbe() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject subscribe = new JSONObject();
                JSONObject unsubscribe = new JSONObject();
                //取得token
                String token = FirebaseInstanceId.getInstance().getToken();
                Log.d(TAG, "token:" + token);
                JSONObject setting;
                //傳送setting
                try {
                    setting = appFile.getSetting();
                } catch (Exception e) {
                    e.printStackTrace();
                    setting = new JSONObject();
                    try {
                        setting.put("swich_online", true);
                        setting.put("flow_warning", true);
                        appFile.saveSetting(setting);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                //包裝
                try {
                    subscribe.put("token", token);
                    //swich_online
                    if (setting.getBoolean("swich_online")) {
                        subscribe.put("EventSwitchEnter", appFile.getUuid("EventSwitchEnter"));
                        subscribe.put("EventSwitchLeave", appFile.getUuid("EventSwitchLeave"));
                        appFile.saveUuidTable(appFile.getUuid("EventSwitchEnter"), "EventSwitchEnter");
                        appFile.saveUuidTable(appFile.getUuid("EventSwitchLeave"), "EventSwitchLeave");
                    } else {
                        unsubscribe.put("EventSwitchEnter", appFile.getUuid("EventSwitchEnter"));
                        unsubscribe.put("EventSwitchLeave", appFile.getUuid("EventSwitchLeave"));
                    }
                    //flow_warning
                    JSONObject getFlowWarning = appFile.getFlowWarning();
                    if (getFlowWarning.names() != null) {
                        if (setting.getBoolean("flow_warning")) {
                            for (int i = 0; i < getFlowWarning.names().length(); i++) {
                                String switch_ID = getFlowWarning.names().getString(i);
                                for (int j = 0; j < getFlowWarning.getJSONObject(switch_ID).names().length(); j++) {
                                    String port_no = getFlowWarning.getJSONObject(switch_ID).names().getString(j);
                                    JSONObject flowWarning = getFlowWarning.getJSONObject(switch_ID).getJSONObject(port_no);
                                    appFile.saveUuidTable(flowWarning.getString("uuid")
                                            , new JSONObject().put(switch_ID, new JSONObject()
                                                    .put(port_no, flowWarning)).toString());
                                }
                                subscribe.put(switch_ID, getFlowWarning.getJSONObject(switch_ID));
                            }
                        } else {
                            for (int i = 0; i < getFlowWarning.names().length(); i++) {
                                String switch_ID = getFlowWarning.names().getString(i);
                                JSONObject flowWarning = getFlowWarning.getJSONObject(switch_ID);
                                unsubscribe.put(switch_ID, flowWarning);
                            }
                        }
                    }
                    Log.d(TAG, "subscribe: " + subscribe.toString());
                    Log.d(TAG, "unsubscribe: " + unsubscribe.toString());
                    controllerURLConnection.subscribe(subscribe.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
