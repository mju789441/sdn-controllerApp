package com.nculab.kuoweilun.sdncontrollerapp;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.io.IOException;

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
                //傳送token
                String token;
                JSONObject setting;
                try {
                    token = appFile.getToken();
                } catch (IOException e) {
                    e.printStackTrace();
                    token = FirebaseInstanceId.getInstance().getToken();
                }
                //看看token長相
                System.out.println("token:" + token);
                //傳送setting
                try {
                    setting = appFile.getSetting();
                } catch (Exception e) {
                    e.printStackTrace();
                    setting = new JSONObject();
                    try {
                        setting.put("swich_online", true);
                        setting.put("flow_error", true);
                        appFile.saveSetting(setting);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                //包裝
                try {
                    String uuid = appFile.getUuid();
                    subscribe.put("token", token);
                    if (setting.getBoolean("swich_online")) {
                        subscribe.put("EventSwitchEnter", uuid);
                        subscribe.put("EventSwitchLeave", uuid);
                    } else {
                        unsubscribe.put("EventSwitchEnter", uuid);
                        unsubscribe.put("EventSwitchLeave", uuid);
                    }
                    if (setting.getBoolean("flow_error")) {

                    } else {

                    }
                    controllerURLConnection.subscribe(subscribe.toString());
                    controllerURLConnection.unsubscribe(unsubscribe.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
