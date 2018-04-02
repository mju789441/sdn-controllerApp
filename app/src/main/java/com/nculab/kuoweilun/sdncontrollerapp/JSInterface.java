package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.PopupWindow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * Created by Kuo Wei Lun on 2018/1/21.
 */

public class JSInterface extends Object implements View.OnClickListener {

    private TopologyActivity topologyActivity;
    private PopupWindow popupWindow;
    private LayoutInflater layoutInflater;
    private WebView webView;
    private String node;
    private Handler handler = new Handler();

    JSInterface(TopologyActivity topologyActivity, LayoutInflater layoutInflater, WebView webView) {
        this.topologyActivity = topologyActivity;
        this.layoutInflater = layoutInflater;
        this.webView = webView;
    }

    @JavascriptInterface
    public void click_node(final String node, String type) {
        this.node = node;
        Log.d(TAG, "click_node: node:" + node + " type: " + type);
        View view;
        popupWindow = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        switch (type) {
            case "switch":
                view = layoutInflater.inflate(R.layout.layout_popup_switch, null);
                popupWindow.setContentView(view);
                popupWindow.showAtLocation(webView, Gravity.BOTTOM, 0, -1 * view.getScrollY());
                Button button_watch_host = (Button) view.findViewById(R.id.button_watch_host);
                button_watch_host.setOnClickListener(this);
                Button button_watch_flow = (Button) view.findViewById(R.id.button_watch_flow);
                button_watch_flow.setOnClickListener(this);
                break;
            case "host":
                view = layoutInflater.inflate(R.layout.layout_popup_host, null);
                popupWindow.setContentView(view);
                popupWindow.showAtLocation(webView, Gravity.BOTTOM, 0, -1 * view.getScrollY());
                Button button_property = (Button) view.findViewById(R.id.button_property);
                button_property.setOnClickListener(this);
                Button button_ban = (Button) view.findViewById(R.id.button_ban);
                button_ban.setOnClickListener(this);
                Button button_unban = (Button) view.findViewById(R.id.button_unban);
                button_unban.setOnClickListener(this);
                break;
            default:
                break;
        }
    }

    @JavascriptInterface
    public void click_edge(final String edge, String source, String target, String port_no) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        Log.d(TAG, "click_edge: edge: " + edge + " source: " + source + " targe: " + target + " port_no: " + port_no);
        intent.setClass(topologyActivity, FlowWarningActivity.class);
        bundle.putString("connect_IP", topologyActivity.connect_IP);
        bundle.putString("switch_ID", source.substring(1));
        bundle.putString("port_no", port_no);
        intent.putExtras(bundle);
        topologyActivity.startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        popupWindow.dismiss();
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        final Host host = topologyActivity.hostArrayList.get(Integer.parseInt(node.substring(1)));
        switch (view.getId()) {
            case R.id.button_watch_host:
                intent.setClass(topologyActivity, HostActivity.class);
                bundle.putString("controller_IP", topologyActivity.controllerURLConnection.urlstr);
                bundle.putString("switch_ID", host.ID);
                intent.putExtras(bundle);
                topologyActivity.startActivity(intent);
                break;
            case R.id.button_watch_flow:
                break;
            case R.id.button_property:
                intent.setClass(topologyActivity, HostStatsActivity.class);
                bundle.putString("controller_IP", topologyActivity.controllerURLConnection.urlstr);
                bundle.putString("switch_ID", host.ID);
                bundle.putSerializable("host", host);
                intent.putExtras(bundle);
                topologyActivity.startActivity(intent);
                break;
            case R.id.button_ban:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject flow = new JSONObject();
                        try {
                            flow.put("dpid", host.ID);
                            flow.put("priority", "867");
                            flow.put("match", new JSONObject()
                                    .put("in_port", host.port));
                            flow.put("actions", new JSONArray("[]"));
                            topologyActivity.controllerURLConnection.addFlowEntry(flow.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            case R.id.button_unban:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject flow = new JSONObject();
                        try {
                            flow.put("dpid", host.ID);
                            flow.put("priority", "867");
                            flow.put("match", new JSONObject()
                                    .put("in_port", host.port));
                            flow.put("actions", new JSONArray("[]"));
                            topologyActivity.controllerURLConnection.deleteFlowEntry(flow.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            default:
                break;
        }
    }
}
