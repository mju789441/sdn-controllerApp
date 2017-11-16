package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listview;
    private ArrayList<ControllerIP> list;
    private ControllerAdapter adapter;
    private String m_Text = "";
    private Handler handler = new Handler();
    private View view_main, view_watchpkt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getLayoutInflater();
        view_main = inflater.inflate(R.layout.activity_main, null);
        view_watchpkt = inflater.inflate(R.layout.watch_pkt_layout, null);
        setContentView(view_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initView();
        setListeners();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_add_black_24dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                plusIP_Dialog();
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void initView() {
        listview = (ListView) findViewById(R.id.list_controller);
        list = new ArrayList<ControllerIP>();
        adapter = new ControllerAdapter(this, list);
        listview.setAdapter(adapter);
    }

    private void setListeners() {
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {
                PopupMenu popupmenu = new PopupMenu(MainActivity.this, view);
                popupmenu.getMenuInflater().inflate(R.menu.menu_instruction, popupmenu.getMenu());
                popupmenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        final ControllerIP controllerIP = (ControllerIP) parent.getItemAtPosition(position);
                        switch (item.getItemId()) {
                            case R.id.delete:
                                controllerIP.close();
                                list.remove(parent.getItemAtPosition(position));
                                adapter.notifyDataSetChanged();
                                break;
                            case R.id.packet_watch:
                                setContentView(view_watchpkt);
                                Button back = (Button) findViewById(R.id.button_back);
                                back.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        setContentView(view_main);
                                    }
                                });
                                controllerIP.watchPkt((TextView) findViewById(R.id.textview_msg));
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

    private void plusIP_Dialog() {
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("add Controller IP :")
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // 在此處理 input
                        list.add(new ControllerIP(input.getText().toString()));
                        adapter.notifyDataSetChanged();
                    }
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
