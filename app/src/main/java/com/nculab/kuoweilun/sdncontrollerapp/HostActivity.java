package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class HostActivity extends AppCompatActivity {

    ControllerSocket controllerSocket;
    String switchID;
    ListView listView;
    private ArrayList<Host> list;
    private HostAdapter adapter;
    private ArrayList<String> HostID;
    Button button_backToSwitch;
    //Handler
    Handler handler = new Handler();
    //Thread
    public Thread thread_getHost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_hostlist);
        Bundle bundle = this.getIntent().getExtras();
        String IP = bundle.getString("controller.IP");
        switchID = bundle.getString("switchID");
        controllerSocket = new ControllerSocket(IP, HostActivity.this);
        controllerSocket.thread_connect.start();
        initView();
        setListeners();
        setThread();
        thread_getHost.start();
    }


    private void initView() {
        listView = (ListView) findViewById(R.id.list_host);
        list = new ArrayList<Host>();
        adapter = new HostAdapter(HostActivity.this, list);
        listView.setAdapter(adapter);
        HostID = new ArrayList<String>();
        button_backToSwitch = (Button) findViewById(R.id.button_backToSwitch);
    }

    private void setListeners() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {

            }

        });

        button_backToSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thread_getHost.interrupt();
                controllerSocket.thread_connect.interrupt();
                controllerSocket.close();
                finish();
            }
        });
    }

    private void setThread() {
        thread_getHost = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        //未連線時拋出例外
                        while (true) {
                            if (controllerSocket.isConnected()) {
                                break;
                            }
                        }
                        controllerSocket.sendEncryptedMsg("GET /v1.0/topology/hosts/");
                        final String str = controllerSocket.getMsg();
                        if (str == null) {
                            throw new Exception();
                        }
                        final String msg = controllerSocket.rsa.decrypt(str.getBytes());
                        // 接收訊息
                        String[] temp = msg.split("\n");
                        if (temp[0].equals("host") && temp[temp.length - 1].equals("/host") && temp.length != 2) {
                            for (int i = 1; i < temp.length - 1; i++) {
                                final String[] temp2 = temp[i].split(" ");
                                if (HostID.contains(temp2[0])) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            list.set(HostID.indexOf(temp2[0]), new Host(temp2[0], temp2[1], temp2[temp2.length - 1]));
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                } else {
                                    HostID.add(temp2[0]);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            list.add(new Host(temp2[0], temp2[1], temp2[temp2.length - 1]));
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }
                        } else {
                            throw new Exception();
                        }
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    } catch (final Exception e) {
                        e.printStackTrace();
                        System.out.println(e.getMessage());
                        controllerSocket.disconnection();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(HostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    }
                }
            }
        });
    }

}
