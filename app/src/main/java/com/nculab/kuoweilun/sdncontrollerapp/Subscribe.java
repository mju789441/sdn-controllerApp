package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.Context;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.nculab.kuoweilun.sdncontrollerapp.controller.ControllerURLConnection;
import com.nculab.kuoweilun.sdncontrollerapp.database.FlowWarn_table;
import com.nculab.kuoweilun.sdncontrollerapp.database.UUID_table;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

import static android.content.ContentValues.TAG;

/**
 * Created by kuo on 2018/3/25.
 */

public class Subscribe {
    Context context;
    AppFile appFile;
    ControllerURLConnection controllerURLConnection;

    public Subscribe(Context context, ControllerURLConnection controllerURLConnection) {
        this.context = context;
        appFile = new AppFile(context);
        this.controllerURLConnection = controllerURLConnection;
    }

    public void subscrbe() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                UUID_table uuid_table = new UUID_table(context);
                FlowWarn_table flowWarn_table = new FlowWarn_table(context);
                JSONObject subscribe = new JSONObject();
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
                    JSONArray switch_enter = uuid_table
                            .getUUID(controllerURLConnection.urlstr, UUID_table.EVENT_SWITCHENTER);
                    JSONArray switch_leave = uuid_table
                            .getUUID(controllerURLConnection.urlstr, UUID_table.EVENT_SWITCHLEAVE);
                    //swich_online
                    if (setting.getBoolean("swich_online")) {
                        if (switch_enter.length() == 0) {
                            switch_enter.put(UUID.randomUUID().toString());
                            JSONObject input = new JSONObject()
                                    .put(UUID_table.URL_COLUMN, controllerURLConnection.urlstr)
                                    .put(UUID_table.UUID_COLUMN, switch_enter.getString(0))
                                    .put(UUID_table.EVENT_COLUMN, UUID_table.EVENT_SWITCHENTER);
                            uuid_table.insert(input);
                        }
                        if (switch_leave.length() == 0) {
                            switch_leave.put(UUID.randomUUID().toString());
                            JSONObject input = new JSONObject()
                                    .put(UUID_table.URL_COLUMN, controllerURLConnection.urlstr)
                                    .put(UUID_table.UUID_COLUMN, switch_leave.getString(0))
                                    .put(UUID_table.EVENT_COLUMN, UUID_table.EVENT_SWITCHLEAVE);
                            uuid_table.insert(input);
                        }
                        subscribe.put(UUID_table.EVENT_SWITCHENTER, switch_enter.getString(0));
                        subscribe.put(UUID_table.EVENT_SWITCHLEAVE, switch_leave.getString(0));
                    }
                    //flow_warning
                    if (setting.getBoolean("flow_warning")) {
                        JSONArray uuidArray = uuid_table.getUUID(controllerURLConnection.urlstr, UUID_table.EVENT_FLOWWARN);
                        for (int i = 0; i < uuidArray.length(); i++) {
                            String uuid = uuidArray.getString(i);
                            subscribe.put(uuid, flowWarn_table.getFlowWarn(uuid));
                        }
                    }
                    if (subscribe.length() != 0) {
                        //取得token
                        String token = FirebaseInstanceId.getInstance().getToken();
                        Log.d(TAG, "token:" + token);
                        subscribe.put("token", token);
                        controllerURLConnection.subscribe(subscribe.toString());
                    }
                    Log.d(TAG, "subscribe: " + subscribe.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void unsubscribe() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject unsubscribe = new JSONObject();
                UUID_table uuid_table = new UUID_table(context);
                FlowWarn_table flowWarn_table = new FlowWarn_table(context);
                JSONObject setting = null;
                //傳送setting
                try {
                    setting = appFile.getSetting();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //包裝
                try {
                    JSONArray switch_enter = uuid_table
                            .getUUID(controllerURLConnection.urlstr, UUID_table.EVENT_SWITCHENTER);
                    JSONArray switch_leave = uuid_table
                            .getUUID(controllerURLConnection.urlstr, UUID_table.EVENT_SWITCHLEAVE);
                    //swich_online
                    if (!setting.getBoolean("swich_online")) {
                        if (switch_enter.length() == 0) {
                            switch_enter.put(UUID.randomUUID().toString());
                            JSONObject input = new JSONObject()
                                    .put(UUID_table.URL_COLUMN, controllerURLConnection.urlstr)
                                    .put(UUID_table.UUID_COLUMN, switch_enter.getString(0))
                                    .put(UUID_table.EVENT_COLUMN, UUID_table.EVENT_SWITCHENTER);
                            uuid_table.insert(input);
                        }
                        if (switch_leave.length() == 0) {
                            switch_leave.put(UUID.randomUUID().toString());
                            JSONObject input = new JSONObject()
                                    .put(UUID_table.URL_COLUMN, controllerURLConnection.urlstr)
                                    .put(UUID_table.UUID_COLUMN, switch_leave.getString(0))
                                    .put(UUID_table.EVENT_COLUMN, UUID_table.EVENT_SWITCHLEAVE);
                            uuid_table.insert(input);
                        }
                        unsubscribe.put(UUID_table.EVENT_SWITCHENTER, switch_enter.getString(0));
                        unsubscribe.put(UUID_table.EVENT_SWITCHLEAVE, switch_leave.getString(0));
                    }
                    //flow_warning
                    if (!setting.getBoolean("flow_warning")) {
                        JSONArray uuidArray = uuid_table.getUUID(controllerURLConnection.urlstr, UUID_table.EVENT_FLOWWARN);
                        for (int i = 0; i < uuidArray.length(); i++) {
                            String uuid = uuidArray.getString(i);
                            unsubscribe.put(uuid, flowWarn_table.getFlowWarn(uuid));
                        }
                    }
                    if (unsubscribe.length() != 0) {
                        //取得token
                        String token = FirebaseInstanceId.getInstance().getToken();
                        Log.d(TAG, "token:" + token);
                        unsubscribe.put("token", token);
                        controllerURLConnection.unsubscribe(unsubscribe.toString());
                    }
                    Log.d(TAG, "unsubscribe: " + unsubscribe.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
