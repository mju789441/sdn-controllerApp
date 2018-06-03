package com.nculab.kuoweilun.sdncontrollerapp.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.nculab.kuoweilun.sdncontrollerapp.AppFile;
import com.nculab.kuoweilun.sdncontrollerapp.LoginActivity;
import com.nculab.kuoweilun.sdncontrollerapp.database.URL_table;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by kuo on 2018/2/20.
 */

public class ControllerURLConnection {

    public String urlstr = null;
    public String hostname = null;
    public String ssid = "";
    private AppCompatActivity appCompatActivity = null;

    public ControllerURLConnection(String url, AppCompatActivity appCompatActivity) {
        urlstr = url;
        hostname = "http://" + url + ":8080";
        this.appCompatActivity = appCompatActivity;
        try {
            if (appCompatActivity != null)
                ssid = new AppFile(appCompatActivity.getApplicationContext()).getSSID();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String get(URL url) throws IOException {
        // 取得連線物件
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        // 設定開啟自動轉址
        httpURLConnection.setInstanceFollowRedirects(true);
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setRequestProperty("Accept", "application/json");

        if (httpURLConnection.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + httpURLConnection.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(
                (httpURLConnection.getInputStream())));

        String temp;
        String output = "";
        while ((temp = br.readLine()) != null) {
            output += temp;
        }

        httpURLConnection.disconnect();
        return output;
    }

    public String post(URL url, JSONObject input) throws IOException, JSONException {
        if (appCompatActivity != null)
            input.put("ssid", ssid);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");

        OutputStream os = httpURLConnection.getOutputStream();
        os.write(input.toString().getBytes());
        os.flush();
        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_CREATED
                && responseCode != HttpURLConnection.HTTP_OK) {
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                loginActivity();
            }
            throw new RuntimeException("Failed : HTTP error code : "
                    + httpURLConnection.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(
                (httpURLConnection.getInputStream())));

        String temp;
        String output = "";
        while ((temp = br.readLine()) != null) {
            output += temp;
        }

        httpURLConnection.disconnect();
        return output;
    }

    public void put(URL url, String input) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("PUT");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");

        OutputStream os = httpURLConnection.getOutputStream();
        os.write(input.getBytes());
        os.flush();
        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_CREATED
                && responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + httpURLConnection.getResponseCode());
        }

        httpURLConnection.disconnect();
    }

    public void delete(URL url, String input) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("DELETE");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");

        OutputStream os = httpURLConnection.getOutputStream();
        os.write(input.getBytes());
        os.flush();
        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_CREATED
                && responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + httpURLConnection.getResponseCode());
        }

        httpURLConnection.disconnect();
    }

    private void loginActivity() {
        if (appCompatActivity == null)
            Toast.makeText(appCompatActivity.getApplicationContext(), "can't login, something wrong", Toast.LENGTH_SHORT).show();
        else {
            Intent intent = new Intent();
            intent.setClass(appCompatActivity.getApplicationContext(), LoginActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("urlstr",urlstr);
            intent.putExtras(bundle);
            appCompatActivity.startActivity(intent);
        }
    }

    public JSONArray login(JSONObject input) throws IOException, JSONException {
        URL url = new URL(hostname + "/login");
        return new JSONArray(post(url, input));
    }

    public void changeToken(Context context, String token) throws JSONException {
        URL_table url_table = new URL_table(context);
        JSONObject item = url_table.get(urlstr);
        if (item != null) {
            JSONObject input = new JSONObject().put(item.getString(URL_table.TOKEN_COLUMN), token);
            try {
                modify_suscribe(input.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!url_table.update(item)) {
            url_table.insert(item);
        }
    }

    public JSONObject getDBflow(JSONObject input) throws IOException, JSONException {
        URL url = new URL(hostname + "/db/flow");
        return new JSONObject(post(url, input));
    }

    public void subscribe(JSONObject input) throws IOException, JSONException {
        URL url = new URL(hostname + "/subscribe");
        post(url, input);
    }

    public JSONObject publish() throws IOException, JSONException {
        URL url = new URL(hostname + "/publish");
        return new JSONObject(get(url));
    }

    public void unsubscribe(String input) throws IOException {
        URL url = new URL(hostname + "/unsubscribe");
        delete(url, input);
    }

    public void modify_suscribe(String input) throws IOException {
        URL url = new URL(hostname + "/modify_suscribe");
        put(url, input);
    }

    public JSONObject getAllSpeed() throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/speed/port");
        return new JSONObject(get(url));
    }

    public JSONArray getTopologySwitch() throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/v1.0/topology/switches");
        return new JSONArray(get(url));
    }

    public JSONArray getTopologySwitch(String dpid) throws IOException, JSONException {
        // 初始化 URL
        dpid = Integer.toHexString(Integer.parseInt(dpid));
        while (dpid.length() < 16) {
            dpid = '0' + dpid;
        }
        URL url = new URL(hostname + "/v1.0/topology/switches/" + dpid);
        return new JSONArray(get(url));
    }

    public JSONArray getTopologyLink() throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/v1.0/topology/links");
        return new JSONArray(get(url));
    }

    public JSONArray getTopologyLink(String dpid) throws IOException, JSONException {
        // 初始化 URL
        dpid = Integer.toHexString(Integer.parseInt(dpid));
        while (dpid.length() < 16) {
            dpid = '0' + dpid;
        }
        URL url = new URL(hostname + "/v1.0/topology/links/" + dpid);
        return new JSONArray(get(url));
    }

    public JSONArray getTopologyHost() throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/v1.0/topology/hosts");
        return new JSONArray(get(url));
    }

    public JSONArray getTopologyHost(String dpid) throws IOException, JSONException {
        // 初始化 URL
        dpid = Integer.toHexString(Integer.parseInt(dpid));
        while (dpid.length() < 16) {
            dpid = '0' + dpid;
        }
        URL url = new URL(hostname + "/v1.0/topology/hosts/" + dpid);
        return new JSONArray(get(url));
    }

    public JSONArray getAllSwitch() throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/switches");
        return new JSONArray(get(url));
    }

    public JSONObject getDescStats(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/desc/" + dpid);
        return new JSONObject(get(url));
    }

    public JSONObject getAllFlowStats(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/flow/" + dpid);
        return new JSONObject(get(url));
    }

    public JSONObject getFlowStats(String dpid, JSONObject input) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/flow/" + dpid);
        return new JSONObject(post(url, input));
    }

    public JSONObject getAggregateFlowStats(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/aggregateflow/" + dpid);
        return new JSONObject(get(url));
    }

    public JSONObject getAggregateFlowStats(String dpid, JSONObject input) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/aggregateflow/" + dpid);
        return new JSONObject(post(url, input));
    }

    public JSONObject getTableStats(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/table/" + dpid);
        return new JSONObject(get(url));
    }

    public JSONObject getTableFeatures(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/tablefeatures/" + dpid);
        return new JSONObject(get(url));
    }

    public JSONObject getPortStats(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/port/" + dpid);
        return new JSONObject(get(url));
    }

    public JSONObject getPortDesc(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/portdesc/" + dpid);
        return new JSONObject(get(url));
    }

    public JSONObject getQueueStats(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/queue/" + dpid);
        return new JSONObject(get(url));
    }

    public JSONObject getQueueConfig(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/queueconfig/" + dpid);
        return new JSONObject(get(url));
    }

    public JSONObject getQueueDesc(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/queuedesc/" + dpid);
        return new JSONObject(get(url));
    }

    public JSONObject getGroupStats(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/group/" + dpid);
        return new JSONObject(get(url));
    }

    public JSONObject getGroupDesc(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/groupdesc/" + dpid);
        return new JSONObject(get(url));
    }

    public JSONObject getGroupFeatures(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/groupfeatures/" + dpid);
        return new JSONObject(get(url));
    }

    public JSONObject getMeterStats(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/meter/" + dpid);
        return new JSONObject(get(url));
    }

    public JSONObject getMeterConfig(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/meterconfig/" + dpid);
        return new JSONObject(get(url));
    }

    public JSONObject getMeterDesc(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/meterdesc/" + dpid);
        return new JSONObject(get(url));
    }

    public JSONObject getMeterFeatures(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/meterfeatures/" + dpid);
        return new JSONObject(get(url));
    }

    public JSONObject getRole(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/role/" + dpid);
        return new JSONObject(get(url));
    }

    public void addFlowEntry(JSONObject input) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/flowentry/add");
        post(url, input);
    }

    public void modifyFlowEntry(JSONObject input) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/flowentry/modify");
        post(url, input);
    }

    public void modifyFlowEntry_strict(JSONObject input) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/flowentry/modify_strict");
        post(url, input);
    }

    public void deleteFlowEntry(JSONObject input) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/flowentry/delete");
        post(url, input);
    }

    public void deleteFlowEntry_strict(JSONObject input) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/flowentry/delete_strict");
        post(url, input);
    }

    public void deleteAllFlowEntry(String dpid) throws IOException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/flowentry/clear");
        get(url);
    }

    public void addGroupEntry(JSONObject input) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/groupentry/add");
        post(url, input);
    }

    public void modifyGroupEntry(JSONObject input) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/groupentry/modify");
        post(url, input);
    }

    public void deleteGroupEntry(JSONObject input) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/groupentry/delete");
        post(url, input);
    }

    public void modifyPortDesc(JSONObject input) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/portdesc/modify");
        post(url, input);
    }

    public void addMeterEntry(JSONObject input) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/meterentry/add");
        post(url, input);
    }

    public void modifyMeterEntry(JSONObject input) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/meterentry/modify");
        post(url, input);
    }

    public void deleteMeterEntry(JSONObject input) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/meterentry/delete");
        post(url, input);
    }

    public void modifyRole(JSONObject input) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/role");
        post(url, input);
    }

    public void experimenter(String dpid, JSONObject input) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/experimenter/" + dpid);
        post(url, input);
    }

}