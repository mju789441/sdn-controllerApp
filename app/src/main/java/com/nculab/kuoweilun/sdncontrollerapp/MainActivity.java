package com.nculab.kuoweilun.sdncontrollerapp;

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
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    //View
    private View view_main, view_watchpkt;
    //Component
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private ListView listview;
    private ArrayList<Controller> list;
    private ControllerAdapter adapter;

    @Override
    public void setTheme(int resid) {
        super.setTheme(resid);
    }

    @Override
    public View findViewById(int id) {
        return super.findViewById(id);
    }

    @Override
    public MenuInflater getMenuInflater() {
        return super.getMenuInflater();
    }

    private Controller connecting_controller = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //設定View
        LayoutInflater inflater = getLayoutInflater();
        view_main = inflater.inflate(R.layout.activity_main, null);
        view_watchpkt = inflater.inflate(R.layout.watch_pkt_layout, null);
        setContentView(view_main);

        initView();
        setListeners();
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_add_black_24dp);
        listview = (ListView) findViewById(R.id.list_controller);
        list = new ArrayList<Controller>();
        adapter = new ControllerAdapter(this, list);
        listview.setAdapter(adapter);
    }

    private void setListeners() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                plusController_Dialog();
                Snackbar.make(view, "Input your controller IP", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {
                PopupMenu popupmenu = new PopupMenu(MainActivity.this, view);
                popupmenu.getMenuInflater().inflate(R.menu.menu_instruction, popupmenu.getMenu());
                popupmenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        final Controller controller = (Controller) parent.getItemAtPosition(position);
                        switch (item.getItemId()) {
                            case R.id.delete:
                                controller.close();
                                if (connecting_controller.equals(controller)) {
                                    connecting_controller = null;
                                }
                                list.remove(parent.getItemAtPosition(position));
                                adapter.notifyDataSetChanged();
                                break;
                            case R.id.connect:
                                if (connecting_controller != null) {
                                    connecting_controller.close();
                                    connecting_controller.setStatus("未連線");
                                }
                                if (!connecting_controller.equals(controller)) {
                                    connecting_controller = null;
                                }
                                controller.connect();
                                connecting_controller = controller;
                                break;
                            case R.id.packet_watch:
                                setContentView(R.layout.watch_pkt_layout);
                                controller.sendInstruction("watch_pkt");
                                controller.setTextView_msg((TextView) findViewById(R.id.textview_msg));
                                controller.watch_pkt.start();

                                Button back = (Button) findViewById(R.id.button_back);
                                back.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        controller.watch_pkt.interrupt();
                                        setContentView(view_main);
                                    }
                                });
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
                        Controller controller = new Controller(input.getText().toString(), MainActivity.this);
                        list.add(controller);
                        if (connecting_controller == null) {
                            controller.connect();
                            connecting_controller = controller;
                        }
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
