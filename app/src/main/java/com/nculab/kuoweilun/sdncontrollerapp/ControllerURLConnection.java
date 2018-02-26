package com.nculab.kuoweilun.sdncontrollerapp;

import org.json.JSONArray;
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

    String urlstr = null;

    public ControllerURLConnection(String url) {
        urlstr = url;
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
        url = new URL("http://localhost:8080/RESTfulExample/json/product/post");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");

        OutputStream os = httpURLConnection.getOutputStream();
        os.write(input.getBytes());
        os.flush();

        if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
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

    public JSONArray getAllSwitch() throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/switches");
        return new JSONArray(get(url));
    }

    public JSONArray getDescStats(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/desc/" + dpid);
        return new JSONArray(get(url));
    }

    public JSONArray getAllFlowStats(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/flow/" + dpid);
        return new JSONArray(get(url));
    }

    public JSONArray getFlowStats(String dpid, String input) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/flow/" + dpid);
        return new JSONArray(post(url, input));
    }

    public JSONArray getAggregateFlowStats(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/aggregateflow/" + dpid);
        return new JSONArray(get(url));
    }

    public JSONArray getAggregateFlowStats(String dpid, String input) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/aggregateflow/" + dpid);
        return new JSONArray(post(url, input));
    }

    public JSONArray getTableStats(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/table/" + dpid);
        return new JSONArray(get(url));
    }

    public JSONArray getTableFeatures(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/tablefeatures/" + dpid);
        return new JSONArray(get(url));
    }

    public JSONArray getProtStats(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/port/" + dpid);
        return new JSONArray(get(url));
    }

    public JSONArray getProtDesc(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/portdesc/" + dpid);
        return new JSONArray(get(url));
    }

    public JSONArray getQueueStats(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/queue/" + dpid);
        return new JSONArray(get(url));
    }

    public JSONArray getQueueConfig(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/queueconfig/" + dpid);
        return new JSONArray(get(url));
    }

    public JSONArray getQueueDesc(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/queuedesc/" + dpid);
        return new JSONArray(get(url));
    }

    public JSONArray getGroupStats(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/group/" + dpid);
        return new JSONArray(get(url));
    }

    public JSONArray getGroupDesc(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/groupdesc/" + dpid);
        return new JSONArray(get(url));
    }

    public JSONArray getGroupFeatures(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/groupfeatures/" + dpid);
        return new JSONArray(get(url));
    }

    public JSONArray getMeterStats(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/meter/" + dpid);
        return new JSONArray(get(url));
    }

    public JSONArray getMeterConfig(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/meterconfig/" + dpid);
        return new JSONArray(get(url));
    }

    public JSONArray getMeterDesc(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/meterdesc/" + dpid);
        return new JSONArray(get(url));
    }

    public JSONArray getMeterFeatures(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/meterfeatures/" + dpid);
        return new JSONArray(get(url));
    }

    public JSONArray getRole(String dpid) throws IOException, JSONException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/role/" + dpid);
        return new JSONArray(get(url));
    }

    public void addFlowEntry(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/flowentry/add");
        post(url, input);
    }

    public void modifyFlowEntry(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/flowentry/modify");
        post(url, input);
    }

    public void modifyFlowEntry_strict(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/flowentry/modify_strict");
        post(url, input);
    }

    public void deleteFlowEntry(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/flowentry/delete");
        post(url, input);
    }

    public void deleteFlowEntry_strict(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/flowentry/delete_strict");
        post(url, input);
    }

    public void deleteAllFlowEntry(String dpid) throws IOException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/flowentry/clear");
        get(url);
    }

    public void addGroupEntry(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/groupentry/add");
        post(url, input);
    }

    public void modifyGroupEntry(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/groupentry/modify");
        post(url, input);
    }

    public void deleteGroupEntry(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/groupentry/delete");
        post(url, input);
    }

    public void modifyPortDesc(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/portdesc/modify");
        post(url, input);
    }

    public void addMeterEntry(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/meterentry/add");
        post(url, input);
    }

    public void modifyMeterEntry(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/meterentry/modify");
        post(url, input);
    }

    public void deleteMeterEntry(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/meterentry/delete");
        post(url, input);
    }

    public void modifyRole(String input) throws IOException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/role");
        post(url, input);
    }

    public void experimenter(String dpid, String input) throws IOException {
        // 初始化 URL
        URL url = new URL(urlstr + "/stats/experimenter/" + dpid);
        post(url, input);
    }

}
