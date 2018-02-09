package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.zip.Inflater;

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
    public void click_node(final String node, String parent) {
        this.node = node;
        System.out.println("JS调用了Android的hello方法\n" + node + "\n" + parent + "\n");
        View view;
        popupWindow = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        switch (parent) {
            case "controller":
                if (node == "switch") {
                    break;
                }
                break;
            case "switch":
                if (node == "host") {
                    break;
                }
                view = layoutInflater.inflate(R.layout.layout_popup_host, null);
                popupWindow.setContentView(view);
                break;
            case "host":
                view = layoutInflater.inflate(R.layout.layout_popup_host, null);
                popupWindow.setContentView(view);
                popupWindow.showAsDropDown(webView);
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

    @Override
    public void onClick(View view) {
        popupWindow.dismiss();
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        final Host host = topologyActivity.hostArrayList.get(topologyActivity.controllerSocket.hostPortArrayList.indexOf(node.substring(1)));
        switch (view.getId()) {
            case R.id.button_property:
                intent.setClass(topologyActivity, HostPropertyActivity.class);
                bundle.putString("controller.IP", topologyActivity.controllerSocket.IP);
                bundle.putSerializable("host", host);
                intent.putExtras(bundle);
                topologyActivity.startActivity(intent);
                break;
            case R.id.button_ban:
                if (host.IP == "None") {
                    break;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        topologyActivity.controllerSocket.sendEncryptedMsg("POST /ban/" + host.IP);
                        try {
                            topologyActivity.controllerSocket.getMsg();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            case R.id.button_unban:
                if (host.IP == "None") {
                    break;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        topologyActivity.controllerSocket.sendEncryptedMsg("POST /unban/" + host.IP);
                        try {
                            topologyActivity.controllerSocket.getMsg();
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
