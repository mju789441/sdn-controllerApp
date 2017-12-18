package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    LayoutInflater inflater;
    //View
    private View view_main;
    //Component
    private Toolbar toolbar;
    private ListView listView;
    private ArrayList<Controller> list;
    private ControllerAdapter adapter;
    private Controller connecting_controller = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //設定View
        inflater = getLayoutInflater();
        view_main = inflater.inflate(R.layout.activity_main, null);
        setContentView(view_main);

        initView();
        setListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(connecting_controller != null) {
            connecting_controller.thread_connect.start();
        }
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listView = (ListView) findViewById(R.id.list_controller);
        list = new ArrayList<Controller>();
        adapter = new ControllerAdapter(MainActivity.this, list);
        listView.setAdapter(adapter);
    }

    private void setListeners() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {
                adapter.notifyDataSetChanged();
                PopupMenu popupmenu = new PopupMenu(MainActivity.this, view);
                popupmenu.getMenuInflater().inflate(R.menu.menu_controller, popupmenu.getMenu());
                popupmenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        final Controller controller = (Controller) parent.getItemAtPosition(position);
                        switch (item.getItemId()) {
                            case R.id.delete:
                                controller.thread_connect.interrupt();
                                controller.close();
                                //目標是已連線的controller
                                if (connecting_controller != null) {
                                    if (connecting_controller.equals(controller)) {
                                        connecting_controller = null;
                                    }
                                }
                                list.remove(parent.getItemAtPosition(position));
                                adapter.notifyDataSetChanged();
                                break;
                            case R.id.connect:
                                //把已連線的controller停止連線
                                if (connecting_controller != null) {
                                    //目標是已連線的controller
                                    if (connecting_controller.equals(controller) && controller.isConnected()) {
                                        break;
                                    }

                                    controller.thread_connect.interrupt();
                                    connecting_controller.disconnection();
                                }
                                controller.thread_connect.start();
                                connecting_controller = controller;
                                break;
                            case R.id.watch_switch:
                                if (!controller.isConnected()) {
                                    Toast.makeText(MainActivity.this, "尚未連線", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                connecting_controller.reset();
                                Intent intent = new Intent();
                                intent.setClass(MainActivity.this, SwitchActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("controller.IP", controller.IP);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                popupmenu.show();
            }
        });
    }

    private void plusController_Dialog() {
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("add Controller please input your ID :")
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // 在此處理 input
                        Controller controller = new Controller(input.getText().toString(), MainActivity.this, adapter);
                        list.add(controller);
                        adapter.notifyDataSetChanged();
                        //如果有未連線的controller自動連線
                        if (connecting_controller == null) {
                            controller.thread_connect.start();
                            connecting_controller = controller;
                        }
                    }
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_plus) {
            plusController_Dialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
