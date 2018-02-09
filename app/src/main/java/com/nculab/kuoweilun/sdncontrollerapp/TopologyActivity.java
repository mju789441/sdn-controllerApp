package com.nculab.kuoweilun.sdncontrollerapp;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Kuo Wei Lun on 2018/1/18.
 */

public class TopologyActivity extends AppCompatActivity {

    //Component
    public ControllerSocket controllerSocket;
    private WebView webView;
    private WebSettings webSettings;
    private Button button_backToController;
    private boolean urlLoad = false;
    public ArrayList<Switch> switchArrayList;
    public ArrayList<Host> hostArrayList;
    //Handler
    private Handler handler = new Handler();
    //Thread
    private Thread thread_getTopology;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_topology);
        Bundle bundle = this.getIntent().getExtras();
        String connect_IP = bundle.getString("controller.IP");
        initView();
        setListeners();
        setThread();
        controllerSocket = new ControllerSocket(connect_IP, TopologyActivity.this, switchArrayList, hostArrayList);
    }

    @Override
    public void onResume() {
        super.onResume();
        controllerSocket.thread_connect.start();
        thread_getTopology.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        thread_getTopology.interrupt();
        controllerSocket.thread_connect.interrupt();
        controllerSocket.reset();
    }

    @SuppressLint("JavascriptInterface")
    private void initView() {
        switchArrayList = new ArrayList<Switch>();
        hostArrayList = new ArrayList<Host>();
        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient() {
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                urlLoad = false;
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

    private void setThread() {
        thread_getTopology = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!urlLoad) {
                    }
                    //取得controller
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            JSONArray getSetting = new JSONArray();
                            try {
                                getSetting.put(new JSONObject("{ group: 'nodes', data: { id: 'c0', parent: 'controller' } }"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            webView.loadUrl("javascript:add_eles(" + getSetting.toString() + ")");
                        }
                    });
                    controllerSocket.getSwitchArrayList();
                    controllerSocket.getHostArrayList();
                    final JSONArray getSwitch = new JSONArray();
                    for (int i = 0; i < switchArrayList.size(); i++) {
                        JSONObject switchID = new JSONObject("{ group: 'nodes', data: { id: 's" + switchArrayList.get(i).ID + "', parent: 'switch' } }");
                        getSwitch.put(switchID);
                        JSONObject switchEdge = new JSONObject("{ group: 'edges', data: { id: 'ec0s" + switchArrayList.get(i).ID + "', source: 'c0', target: 's" + switchArrayList.get(i).ID + "' } }");
                        getSwitch.put(switchEdge);
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl("javascript:add_eles(" + getSwitch.toString() + ")");
                        }
                    });
                    final JSONArray getHost = new JSONArray();
                    for (int i = 0; i < hostArrayList.size(); i++) {
                        JSONObject hostID = new JSONObject("{ group: 'nodes', data: { id: 'h" + hostArrayList.get(i).port + "', parent: 'host' } }");
                        getHost.put(hostID);
                        JSONObject hostEdge = new JSONObject("{ group: 'edges', data: { id: 'ec" + hostArrayList.get(i).ID + "h" + hostArrayList.get(i).port + "', source: 's" + hostArrayList.get(i).ID + "', target: 'h" + hostArrayList.get(i).port + "' } }");
                        getHost.put(hostEdge);
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl("javascript:add_eles(" + getHost.toString() + ")");
                            webView.loadUrl("javascript:rearrange()");
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    controllerSocket.disconnection();
                }
            }
        });
    }

}