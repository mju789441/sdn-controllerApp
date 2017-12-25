package com.nculab.kuoweilun.sdncontrollerapp;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Kuo Wei Lun on 2017/12/3.
 */

public class FlowActivity extends AppCompatActivity {

    View activityView;
    ControllerSocket controllerSocket;
    String switchID;
    ListView listView;
    private ArrayList<Flow> list;
    private FlowAdapter adapter;
    private ArrayList<FlowCheck> flowChecks;
    Button button_backToSwitch;
    //Handler
    Handler handler = new Handler();
    //Thread
    public Thread thread_getFlow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityView = FlowActivity.this.getLayoutInflater().inflate(R.layout.layout_flowlist, null);
        setContentView(activityView);
        Bundle bundle = this.getIntent().getExtras();
        String IP = bundle.getString("controller.IP");
        switchID = bundle.getString("switchID");
        controllerSocket = new ControllerSocket(IP, FlowActivity.this);
        initView();
        setListeners();
        setThread();
    }

    @Override
    public void onResume() {
        super.onResume();
        controllerSocket.thread_connect.start();
        thread_getFlow.start();
    }


    private void initView() {
        listView = (ListView) findViewById(R.id.list_flow);
        list = new ArrayList<Flow>();
        adapter = new FlowAdapter(FlowActivity.this, list);
        listView.setAdapter(adapter);
        flowChecks = new ArrayList<FlowCheck>();
        button_backToSwitch = (Button) findViewById(R.id.button_backToSwitch);
    }

    private void setListeners() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {
                Flow flow = (Flow) adapter.getItem(position);
                //換View
                View view_temp = FlowActivity.this.getLayoutInflater().inflate(R.layout.layout_flowproperty, null);
                setContentView(view_temp);
                //取得Id
                TextView switchID = (TextView) view_temp.findViewById(R.id.textView_switchID);
                TextView port = (TextView) view_temp.findViewById(R.id.textView_port);
                TextView speed = (TextView) view_temp.findViewById(R.id.textView_speed);
                TextView rx = (TextView) view_temp.findViewById(R.id.textView_rx);
                TextView dx = (TextView) view_temp.findViewById(R.id.textView_dx);
                TextView duration = (TextView) view_temp.findViewById(R.id.textView_duration);
                TextView duration_nsec = (TextView) view_temp.findViewById(R.id.textView_duration_nsec);
                TextView rx_errors = (TextView) view_temp.findViewById(R.id.textView_rx_errors);
                Button backToFlow = (Button) view_temp.findViewById(R.id.button_backToFlow);
                //設定Text
                switchID.setText(switchID.getText() + flow.switchID);
                port.setText(port.getText() + flow.port);
                speed.setText(speed.getText() + flow.speed);
                rx.setText(rx.getText() + flow.rx);
                dx.setText(dx.getText() + flow.dx);
                duration.setText(duration.getText() + flow.duration);
                duration_nsec.setText(duration_nsec.getText() + flow.duration_nsec);
                rx_errors.setText(rx_errors.getText() + flow.rx_errors);
                //返回原先ViewView
                backToFlow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setContentView(activityView);
                    }
                });
            }
        });

        button_backToSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thread_getFlow.interrupt();
                controllerSocket.thread_connect.interrupt();
                controllerSocket.reset();
                finish();
            }
        });
    }

    private void setThread() {
        thread_getFlow = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        while (true) {
                            if (controllerSocket.isConnected()) {
                                break;
                            }
                            if (controllerSocket.failConnected()) {
                                Toast.makeText(FlowActivity.this, "斷線", Toast.LENGTH_SHORT).show();
                                throw new Exception();
                            }
                        }
                        controllerSocket.sendEncryptedMsg("GET /v1.0/traffic/hosts/");
                        final String msg = controllerSocket.getDncryptedMsg();
                        if (msg == null) {
                            throw new Exception();
                        }
                        // 接收訊息
                        String[] temp = msg.split("\n");
                        if (temp[0].equals("host_traffic") && temp[temp.length - 1].equals("/host_traffic") && temp.length != 2) {
                            boolean flowChanged = false;
                            for (int i = 1; i < temp.length - 1; i++) {
                                final String[] temp2 = temp[i].split(" ");
                                if (!flowChecks.contains(new FlowCheck(false, temp2[0], temp2[1]))) {
                                    flowChanged = true;
                                    flowChecks.add(new FlowCheck(true, temp2[0], temp2[1]));
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            list.add(new Flow(temp2[0], temp2[1], temp2[2], temp2[3], temp2[4], temp2[5], temp2[6], temp2[temp2.length - 1]));
                                        }
                                    });
                                } else {
                                    flowChecks.get(flowChecks.indexOf(new FlowCheck(false, temp2[0], temp2[1]))).alive = true;
                                }
                            }
                            //判斷,Flow是否還存在
                            for (int i = flowChecks.size() - 1; i >= 0; i--) {
                                if (flowChecks.get(i).alive == false) {
                                    flowChanged = true;
                                    flowChecks.remove(i);
                                    list.remove(i);
                                } else {
                                    flowChecks.get(i).alive = false;
                                }
                            }
                            if (flowChanged) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
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
                        controllerSocket.disconnection();
                        break;
                    }
                }
            }
        });
    }

    private class FlowCheck {
        boolean alive = false;
        String switchID;
        String port;

        FlowCheck(boolean alive, String switchID, String port) {
            this.alive = alive;
            this.switchID = switchID;
            this.port = port;
        }

    }

}
