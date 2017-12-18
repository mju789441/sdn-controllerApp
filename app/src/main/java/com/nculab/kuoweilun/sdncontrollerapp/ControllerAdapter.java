package com.nculab.kuoweilun.sdncontrollerapp;

/**
 * Created by Kuo Wei Lun on 2017/11/6.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ControllerAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private ArrayList<Controller> list;

    ControllerAdapter(Context context, ArrayList<Controller> list) {
        layoutInflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
         ControlerViewHolder holder;
        //取得View component
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.layout_controlleritem, parent, false);
            holder = new ControlerViewHolder();
            holder.textView_IP = (TextView) convertView.findViewById(R.id.textView_IP);
            holder.textView_status = (TextView) convertView.findViewById(R.id.textView_status);
            convertView.setTag(holder);
        } else {
            holder = (ControlerViewHolder) convertView.getTag();
        }
        //Controller設定
        Controller controller = (Controller) getItem(position);
        //View 內容設定
        holder.textView_IP.setText(controller.IP);
        holder.textView_status.setText(controller.status);
        return convertView;
    }
    static class ControlerViewHolder {
        public TextView textView_IP = null;
        public TextView textView_status = null;
    }
}
