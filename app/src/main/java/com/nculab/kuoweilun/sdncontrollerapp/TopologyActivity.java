package com.nculab.kuoweilun.sdncontrollerapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Kuo Wei Lun on 2018/1/18.
 */

public class TopologyActivity extends AppCompatActivity {

    //Component
    public String connect_IP;
    public ControllerURLConnection controllerURLConnection;
    private WebView webView;
    private WebSettings webSettings;
    private Toolbar toolbar;
    private Button button_backToController;
    public ArrayList<Host> hostArrayList;
    private boolean urlLoad = false;
    //Handler
    private Handler handler = new Handler();
    //Thread
    private Runnable runnable_getTopology;
    private Thread thread_getTopology;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_topology);
        Bundle bundle = this.getIntent().getExtras();
        connect_IP = bundle.getString("controller_IP");
        controllerURLConnection = new ControllerURLConnection(connect_IP);
        //Subscribe
        try {
            new AppFile(this).saveCurrentIP(connect_IP);
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Subscribe(new AppFile(this), controllerURLConnection).subscrbe();
        initView();
        setListeners();
        setRunnable();
    }

    @Override
    public void onResume() {
        super.onResume();
        thread_getTopology = new Thread(runnable_getTopology);
        thread_getTopology.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        thread_getTopology.interrupt();
    }

    @SuppressLint("JavascriptInterface")
    private void initView() {
        hostArrayList = new ArrayList<Host>();
        //topology
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient() {
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                urlLoad = false;
                super.onPageStarted(view, url, favicon);
            }

            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                urlLoad = true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webView.loadUrl("file:///android_asset/topology.html");
        button_backToController = (Button) findViewById(R.id.button_backToController);
        webView.addJavascriptInterface(new JSInterface(TopologyActivity.this, getLayoutInflater(), webView), "android");
    }

    private void setListeners() {
        button_backToController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setRunnable() {
        runnable_getTopology = new Runnable() {
            @Override
            public void run() {
                try {
                    //等待網頁載入
                    while (!urlLoad) {
                    }
                    //取得controller
                    final JSONArray getTopology = new JSONArray();
                    JSONArray switchArray = controllerURLConnection.getAllSwitch();
                    JSONObject allSpeed = controllerURLConnection.getAllSpeed();
                    int switch_length = switchArray.length();
                    //紀錄避免重複的edge
                    JSONObject switch_link = new JSONObject();
                    //host編號
                    int host_num = 1;
                    //所有switch
                    for (int i = 0; i < switch_length; i++) {
                        String switch_ID = String.valueOf(switchArray.getInt(i));
                        JSONObject switchObject = new JSONObject("{ group: 'nodes', data: { id: 's"
                                + switch_ID + "', type: 'switch' } }");
                        getTopology.put(switchObject);

                        JSONArray portArray = controllerURLConnection.getPortDesc(switch_ID)
                                .getJSONArray(switch_ID);
                        JSONArray getSwitch = controllerURLConnection.getTopologySwitch(switch_ID)
                                .getJSONObject(0).getJSONArray("ports");
                        //Switch有幾個port
                        for (int j = 0; j < getSwitch.length(); j++) {
                            String hw_addr = getSwitch.getJSONObject(j).getString("hw_addr");
                            JSONArray getSwitchLink = controllerURLConnection.getTopologyLink(switch_ID);

                            Boolean findLink = false;
                            //檢查有沒有switch相連
                            for (int k = 0; k < getSwitchLink.length(); k++) {
                                JSONObject getLink = getSwitchLink.getJSONObject(k);
                                String src_hw_addr = getLink.getJSONObject("src")
                                        .getString("hw_addr");
                                String src_port_no = String.valueOf(Integer.parseInt(getLink.getJSONObject("src")
                                        .getString("port_no"), 16));
                                String dest_hw_addr = getLink.getJSONObject("dst").getString("hw_addr");
                                String dst_port_no = String.valueOf(Integer.parseInt(getLink.getJSONObject("dst")
                                        .getString("port_no"), 16));
                                if (hw_addr.equals(src_hw_addr)) {
                                    findLink = true;
                                    //避免重複的edge
                                    if (!switch_link.isNull(src_hw_addr)) {
                                        if (!switch_link.getJSONObject(src_hw_addr).isNull(src_port_no)) {
                                            if (switch_link.getJSONObject(src_hw_addr).get(src_port_no) == dest_hw_addr) {
                                                break;
                                            }
                                        }
                                    }
                                    int dst_dpid = Integer.parseInt(getLink.getJSONObject("dst")
                                            .getString("dpid"), 16);
                                    JSONObject switchEdge = new JSONObject("{ group: 'edges', data: { id: 'es"
                                            + switch_ID + "s" + dst_dpid + "', source: 's" + switch_ID
                                            + "', target: 's" + dst_dpid + "', port_no: '" + src_port_no
                                            + "', flow: '" + allSpeed.getJSONObject(switch_ID)
                                            .getInt("" + src_port_no) + "' } }");
                                    getTopology.put(switchEdge);
                                    //紀錄連線
                                    switch_link.put(src_hw_addr, new JSONObject().put(src_port_no, dest_hw_addr));
                                    switch_link.put(dest_hw_addr, new JSONObject().put(dst_port_no, src_hw_addr));
                                    break;
                                }
                            }
                            //預設沒有相連switch的都是單一獨立的host
                            if (!findLink) {
                                int port_no = Integer.parseInt(getSwitch.getJSONObject(j)
                                        .getString("port_no"), 16);
                                int speed = allSpeed.getJSONObject(switch_ID).getInt("" + port_no);
                                JSONObject hostObject = new JSONObject("{ group: 'nodes', data: { id: 'h"
                                        + host_num + "', type: 'host' } }");
                                getTopology.put(hostObject);
                                JSONObject hostEdge = new JSONObject("{ group: 'edges', data: { id: 'ec"
                                        + switch_ID + "h" + host_num + "', source: 's" + switch_ID
                                        + "', target: 'h" + host_num + "', port_no: '" + port_no
                                        + "', flow: '" + speed + "' } }");
                                getTopology.put(hostEdge);
                                for (int k = 0; k < portArray.length(); k++) {
                                    if (portArray.getJSONObject(k).getString("port_no").equals(String.valueOf(port_no))) {
                                        hostArrayList.add(new Host(switch_ID, portArray.getJSONObject(k), speed));
                                        break;
                                    }
                                }
                                host_num++;
                            }
                        }
                    }
                    //等待網頁載入
                    while (!urlLoad) {
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl("javascript:add_eles(" + getTopology.toString() + ")");
                            webView.loadUrl("javascript:rearrange()");
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_topology, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.settings) {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            intent.setClass(TopologyActivity.this, TopologySettingActivity.class);
            bundle.putString("controller_IP", connect_IP);
            intent.putExtras(bundle);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}