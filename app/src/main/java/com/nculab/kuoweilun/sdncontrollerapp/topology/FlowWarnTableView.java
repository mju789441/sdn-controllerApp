package com.nculab.kuoweilun.sdncontrollerapp.topology;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.nculab.kuoweilun.sdncontrollerapp.database.FlowWarn_table;
import com.nculab.kuoweilun.sdncontrollerapp.database.UUID_table;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class FlowWarnTableView extends TableLayout {
    public FlowWarnTableView(Context context, final String connect_URL, String switch_ID, String port_no) throws JSONException {
        super(context);
        final UUID_table uuid_table = new UUID_table(context);
        JSONArray uuid = uuid_table.getUUID(connect_URL, UUID_table.EVENT_FLOWWARN);
        final FlowWarn_table flowWarn_table = new FlowWarn_table(context);
        final JSONArray flowWarn = flowWarn_table.get(uuid, switch_ID, port_no);

        for (int i = 0; i < flowWarn.length(); i++) {
            //component
            final TableRow tableRow = new TableRow(context);
            TextView textView_flowWarn = new TextView(context);
            Button button_delete = new Button(context);
            View view = new View(context);
            //setting
            view.setBackgroundColor(Color.GRAY);
            view.setMinimumHeight(2);
            if (i != 0)
                addView(view);
            //margin
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
            layoutParams.setMargins(50, 0, 50, 0);
            //textView
            String text = "";
            try {
                JSONObject jsonObject = flowWarn.getJSONObject(i);
                text = "順時速度: " + jsonObject.getString(FlowWarn_table.SPEED_COLUMN);
                text += ", 持續時間: " + jsonObject.getString(FlowWarn_table.DURATION_COLUMN);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            textView_flowWarn.setLayoutParams(layoutParams);
            textView_flowWarn.setTextSize(20);
            textView_flowWarn.setText(text);
            tableRow.addView(textView_flowWarn);
            //button
            button_delete.setLayoutParams(layoutParams);
            button_delete.setTextSize(20);
            button_delete.setGravity(Gravity.RIGHT);
            button_delete.setText("delete");
            tableRow.addView(button_delete);
            final int j = i;
            button_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeView(tableRow);
                    String uuid = null;
                    try {
                        uuid = flowWarn.getJSONObject(j).getString(FlowWarn_table.UUID_COLUMN);
                        uuid_table.delete(connect_URL, uuid);
                        flowWarn_table.delete(uuid);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            addView(tableRow);
        }
    }
}

