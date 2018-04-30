package com.nculab.kuoweilun.sdncontrollerapp.controller;

import android.content.Context;

import com.nculab.kuoweilun.sdncontrollerapp.AppFile;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by kuo on 2018/2/20.
 */

public class ControllerURLConnection {

    public String urlstr = null;
    public String hostname = null;

    public ControllerURLConnection(String url) {
        urlstr = url;
        hostname = "http://" + url + ":8080";
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

    public String post(URL url, String input) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
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

    public void changeToken(Context context, String token) throws IOException, JSONException {
        AppFile appFile = new AppFile(context);
        JSONObject URL_table = appFile.getURL_table();
        if (!URL_table.isNull(urlstr)) {
            JSONObject input = new JSONObject().put(URL_table.getString(urlstr), token);
            modify_suscribe(input.toString());
        }
        appFile.saveUuidTable(urlstr, token);
    }

    public void subscribe(String input) throws IOException {
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

    public JSONObject getFlowStats(String dpid, String input) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/flow/" + dpid);
        return new JSONObject(post(url, input));
    }

    public JSONObject getAggregateFlowStats(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/aggregateflow/" + dpid);
        return new JSONObject(get(url));
    }

    public JSONObject getAggregateFlowStats(String dpid, String input) throws IOException, JSONException {
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

    public void addFlowEntry(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/flowentry/add");
        post(url, input);
    }

    public void modifyFlowEntry(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/flowentry/modify");
        post(url, input);
    }

    public void modifyFlowEntry_strict(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/flowentry/modify_strict");
        post(url, input);
    }

    public void deleteFlowEntry(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/flowentry/delete");
        post(url, input);
    }

    public void deleteFlowEntry_strict(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/flowentry/delete_strict");
        post(url, input);
    }

    public void deleteAllFlowEntry(String dpid) throws IOException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/flowentry/clear");
        get(url);
    }

    public void addGroupEntry(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/groupentry/add");
        post(url, input);
    }

    public void modifyGroupEntry(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/groupentry/modify");
        post(url, input);
    }

    public void deleteGroupEntry(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/groupentry/delete");
        post(url, input);
    }

    public void modifyPortDesc(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/portdesc/modify");
        post(url, input);
    }

    public void addMeterEntry(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/meterentry/add");
        post(url, input);
    }

    public void modifyMeterEntry(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/meterentry/modify");
        post(url, input);
    }

    public void deleteMeterEntry(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/meterentry/delete");
        post(url, input);
    }

    public void modifyRole(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/role");
        post(url, input);
    }

    public void experimenter(String dpid, String input) throws IOException {
        // 初始化 URL
        URL url = new URL(hostname + "/stats/experimenter/" + dpid);
        post(url, input);
    }

}