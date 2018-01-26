package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.IOException;
import java.util.zip.Inflater;

/**
 * Created by Kuo Wei Lun on 2018/1/21.
 */

public class JSInterface extends Object {

    public String click_msg = "";
    private Context context;
    private LayoutInflater layoutInflater;
    private WebView webView;
    private Handler handler = new Handler();

    JSInterface(Context context, LayoutInflater layoutInflater, WebView webView) {
        this.context = context;
        this.layoutInflater = layoutInflater;
        this.webView = webView;
    }

    @JavascriptInterface
    public void click_msg(String msg) {
        click_msg = msg;
        System.out.println("JS调用了Android的hello方法\n" + msg);
        View view = layoutInflater.inflate(R.layout.popup_test, null);
        PopupWindow popup = new PopupWindow(view,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
//        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);
        popup.showAsDropDown(webView);
    }
}
