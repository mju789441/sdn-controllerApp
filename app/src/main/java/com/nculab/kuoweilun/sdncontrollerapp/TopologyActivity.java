package com.nculab.kuoweilun.sdncontrollerapp;

import android.annotation.SuppressLint;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Kuo Wei Lun on 2018/1/18.
 */

public class TopologyActivity extends AppCompatActivity {

    //Component
    public ControllerURLConnection controllerURLConnection;
    private View view_topology;
    private View view_settings;
    private WebView webView;
    private WebSettings webSettings;
    private Toolbar toolbar;
    private Button button_backToController;
    public ArrayList<Host> hostArrayList;
    private boolean urlLoad = false;
    //Settings
    private android.widget.Switch switch_online;
    private android.widget.Switch switch_flowError;
    private Button button_backToTopology;
    private Boolean online = true;
    private Boolean flowError = true;
    //Handler
    private Handler handler = new Handler();
    //Thread
    private Thread thread_getTopology;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view_topology = getLayoutInflater().inflate(R.layout.layout_topology, null);
        view_settings = getLayoutInflater().inflate(R.layout.layout_topologysettings, null);
        setContentView(view_topology);
        Bundle bundle = this.getIntent().getExtras();
        String connect_IP = bundle.getString("controller.IP");
        initView();
        setListeners();
        setThread();
        controllerURLConnection = new ControllerURLConnection(connect_IP);
    }

    @Override
    public void onResume() {
        super.onResume();
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
        //settings
        switch_online = (android.widget.Switch) view_settings.findViewById(R.id.switch_online);
        switch_online.setChecked(online);
        switch_flowError = (android.widget.Switch) view_settings.findViewById(R.id.switch_flowError);
        switch_flowError.setChecked(flowError);
        button_backToTopology = (Button) view_settings.findViewById(R.id.button_backToTopology);
    }

    private void setListeners() {
        //topology
        button_backToController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //settings
        switch_online.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                online = isChecked;
            }
        });
        switch_flowError.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                flowError = isChecked;
            }
        });
        button_backToTopology.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(view_topology);
            }
        });
    }

    private void setThread() {
        thread_getTopology = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //等待網頁載入
                    while (!urlLoad) {
                    }
                    //取得controller
                    final JSONArray getTopology = new JSONArray();
                    getTopology.put(new JSONObject("{ group: 'nodes', data: { id: 'controller' } }"));
                    getTopology.put(new JSONObject("{ group: 'nodes', data: { id: 'switch' } }"));
                    getTopology.put(new JSONObject("{ group: 'nodes', data: { id: 'host' } }"));
                    getTopology.put(new JSONObject("{ group: 'nodes', data: { id: 'c0', parent: 'controller' } }"));
                    JSONArray switchArray = controllerURLConnection.getAllSwitch();
                    for (int i = 0; i < switchArray.length(); i++) {
                        String switch_ID = String.valueOf(switchArray.getInt(i));
                        JSONObject switchObject = new JSONObject("{ group: 'nodes', data: { id: 's"
                                + switch_ID + "', parent: 'switch' } }");
                        getTopology.put(switchObject);
                        JSONObject switchFlowStats = controllerURLConnection
                                .getAggregateFlowStats(switch_ID);
                        JSONObject switchEdge = new JSONObject("{ group: 'edges', data: { id: 'ec0s"
                                + switch_ID + "', source: 'c0', target: 's" +
                                switch_ID + "', flow: '" + switchFlowStats
                                .getJSONArray(switch_ID)
                                .getJSONObject(0)
                                .getString("byte_count") + "' } }");
                        getTopology.put(switchEdge);
                        JSONArray hostPort = controllerURLConnection.getProtDesc(switch_ID).getJSONArray(switch_ID);
                        for (int j = 0; j < hostPort.length(); j++) {
                            String port_no = hostPort.getJSONObject(j).getString("port_no");
                            JSONObject hostID = new JSONObject("{ group: 'nodes', data: { id: 'h" + port_no + "', parent: 'host' } }");
                            getTopology.put(hostID);
                            JSONObject hostEdge = new JSONObject("{ group: 'edges', data: { id: 'ec" + switch_ID + "h" + port_no +
                                    "', source: 's" + switch_ID + "', target: 'h" + port_no + "' } }");
                            getTopology.put(hostEdge);
                            hostArrayList.add(new Host(switch_ID, hostPort.getJSONObject(j)));
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
                    while (true) {
                        switchArray = controllerURLConnection.getAllSwitch();
                        for (int i = 0; i < switchArray.length(); i++) {
                            //等待網頁載入
                            while (!urlLoad) {
                            }
                            final String switch_ID = String.valueOf(switchArray.getInt(i));
                            final JSONObject switchFlowStats = controllerURLConnection
                                    .getAllFlowStats(switch_ID);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        webView.loadUrl("javascript:set_edge_flow( 'ec0s"
                                                + switch_ID + "', '" + switchFlowStats
                                                .getJSONArray(switch_ID)
                                                .getJSONObject(0)
                                                .getString("byte_count") + "')");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                        Thread.sleep(100);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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
            setContentView(view_settings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}