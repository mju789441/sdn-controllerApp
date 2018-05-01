package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

/**
 * Created by kuo on 2018/3/24.
 */

public class AppFile {

    Context context = null;

    public AppFile(Context context) {
        this.context = context;
    }

    private void saveFile(String filename, String input) throws IOException {
        FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
        outputStream.write(input.getBytes());
        outputStream.close();
    }

    private String readFile(String filename) throws IOException {
        InputStream instream = context.openFileInput(filename);
        if (instream != null) {
            InputStreamReader inputreader = new InputStreamReader(instream);
            BufferedReader buffreader = new BufferedReader(inputreader);
            String str = buffreader.readLine();
            inputreader.close();
            buffreader.close();
            return str;
        }
        return null;
    }

    public void saveSetting(JSONObject input) throws IOException {
        saveFile("setting.txt", input.toString());
    }

    public JSONObject getSetting() throws IOException, JSONException {
        JSONObject setting = null;
        try {
            setting = new JSONObject(readFile("setting.txt"));
        } catch (Exception e) {
            e.printStackTrace();
            setting = new JSONObject();
            try {
                setting.put("swich_online", true);
                setting.put("flow_warning", true);
                saveSetting(setting);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return setting;
    }

    public void saveCurrentURL(String input) throws IOException {
        saveFile("URL.txt", input);
    }

    public String getCurrentURL() throws IOException {
        return readFile("URL.txt");
    }

    public void deleteCurrentURL() {
        File file = new File(context.getFilesDir(), "URL.txt");
        file.delete();
    }

}
