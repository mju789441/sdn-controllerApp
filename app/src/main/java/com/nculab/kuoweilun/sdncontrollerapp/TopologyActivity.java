package com.nculab.kuoweilun.sdncontrollerapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Object;

/**
 * Created by Kuo Wei Lun on 2018/1/18.
 */

public class TopologyActivity extends AppCompatActivity {

    //Component
    private ControllerSocket controllerSocket;
    private WebView webView;
    private WebSettings webSettings;
    private Button button_backToController;
    private Boolean loadedUrl = false;
    //Handler
    private Handler handler = new Handler();
    //Thread
    private Thread thread_getTopology;

    JSONObject test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_topology);
        Bundle bundle = this.getIntent().getExtras();
        String connect_IP = bundle.getString("controller.IP");
        controllerSocket = new ControllerSocket(connect_IP, TopologyActivity.this);
        initView();
        setListeners();
        setThread();
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
        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webView.loadUrl("file:///android_asset/topology.html");
        button_backToController = (Button) findViewById(R.id.button_backToController);
        try {
            test = new JSONObject("{\"group\":\"nodes\",\"data\":{\"id\":\"n0\"}}");
            webView.loadUrl("javascript:add_eles(" + test.toString() + ")");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        webView.addJavascriptInterface(new JSInterface(TopologyActivity.this, getLayoutInflater(), webView), "android_click");
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
                while (true) {
                    try {
                        //等待連線
                        while (true) {
                            if (controllerSocket.isConnected()) {
                                break;
                            }
                            if (controllerSocket.failConnected()) {
                                Toast.makeText(TopologyActivity.this, "斷線", Toast.LENGTH_SHORT).show();
                                throw new Exception();
                            }
                        }
                        //傳送請求
                        controllerSocket.sendEncryptedMsg("GET /v1.0/topology/hosts/");
                        //接收回復
                        final String msg = controllerSocket.getDncryptedMsg();
                        if (msg == null) {
                            throw new Exception();
                        }
                        String[] temp = msg.split("\n");
                        if (temp.length > 2 && temp[0].equals("host") && temp[temp.length - 1].equals("/host")) {
                            boolean hostChanged = false;
                            if (list.size() > temp.length - 2) {
                                hostChanged = true;
                                for (int i = temp.length - 2; i < list.size(); i++) {
                                    list.remove(i);
                                }
                            }
                            for (int i = 1; i < temp.length - 1; i++) {
                                final String[] temp2 = temp[i].split(" ");
                                if (list.size() < temp.length - 2) {
                                    hostChanged = true;
                                    list.add(new Host(temp2[0], temp2[1], temp2[2], temp2[3]));
                                } else if (!list.get(i - 1).equals(new Host(temp2[0], temp2[1], temp2[2], temp2[3]))) {
                                    hostChanged = true;
                                    list.set(i - 1, new Host(temp2[0], temp2[1], temp2[2], temp2[3]));
                                }
                            }
                            if (hostChanged) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(HostActivity.this, "取得資料錯誤", Toast.LENGTH_SHORT).show();
                                }
                            });
                            throw new Exception();
                        }
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    } catch (final Exception e) {
                        e.printStackTrace();
                        controllerSocket.disconnection();
                        break;
                    }
                }
            }
        });
    }

}
