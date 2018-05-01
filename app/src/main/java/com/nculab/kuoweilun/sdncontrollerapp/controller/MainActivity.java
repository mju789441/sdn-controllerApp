package com.nculab.kuoweilun.sdncontrollerapp.controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.nculab.kuoweilun.sdncontrollerapp.AppFile;
import com.nculab.kuoweilun.sdncontrollerapp.R;
import com.nculab.kuoweilun.sdncontrollerapp.database.MyDbHelper;
import com.nculab.kuoweilun.sdncontrollerapp.database.URL_table;
import com.nculab.kuoweilun.sdncontrollerapp.switcher.SwitchActivity;
import com.nculab.kuoweilun.sdncontrollerapp.topology.TopologyActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //Component
    private Toolbar toolbar;
    private ListView listView;
    private ArrayList<String> list;
    private ControllerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
        new AppFile(this).deleteCurrentURL();
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listView = (ListView) findViewById(R.id.listView_controller);
        list = new ArrayList<String>();
        adapter = new ControllerAdapter(MainActivity.this, list);
        listView.setAdapter(adapter);
        //載入url_table的url
        try {
            URL_table url_table = new URL_table(this);
            JSONArray urlArray = url_table.getAll();
            for (int i = 0; i < urlArray.length(); i++)
                list.add(urlArray.getJSONObject(i).getString(URL_table.URL_COLUMN));
            url_table.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setListeners() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {
                PopupMenu popupmenu = new PopupMenu(MainActivity.this, view);
                popupmenu.getMenuInflater().inflate(R.menu.menu_controller, popupmenu.getMenu());
                popupmenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        final String controller = (String) parent.getItemAtPosition(position);
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        switch (item.getItemId()) {
                            case R.id.watch_switch:
                                intent.setClass(MainActivity.this, SwitchActivity.class);
                                bundle.putString("controller_URL", controller);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                break;
                            case R.id.watch_topology:
                                intent.setClass(MainActivity.this, TopologyActivity.class);
                                bundle.putString("controller_URL", controller);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                break;
                            case R.id.delete:
                                list.remove(controller);
                                adapter.notifyDataSetChanged();
                                //delete item
                                try {
                                    URL_table url_table = new URL_table(getApplicationContext());
                                    url_table.delete(controller);
                                    url_table.close();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
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
                        String controller = input.getText().toString();
                        list.add(controller);
                        adapter.notifyDataSetChanged();
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
