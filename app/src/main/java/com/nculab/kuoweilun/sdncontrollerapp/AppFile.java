package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
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

    private void saveFile(String filename, String input) throws IOException {
        FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
        outputStream.write(input.getBytes());
        outputStream.close();
    }

    public String getUuid() throws IOException {
        try {
            return readFile("uuid.txt");
        } catch (IOException e) {
            String uuid = UUID.randomUUID().toString();
            saveFile("uuid.txt", uuid);
            return uuid;
        }
    }

    public void saveToken(String token) throws IOException {
        saveFile("token.txt", token);
    }

    public String getToken() throws IOException {
        return readFile("token.txt");
    }

    public void saveSetting(JSONObject input) throws IOException {
        saveFile("setting.txt", input.toString());
    }

    public JSONObject getSetting() throws IOException, JSONException {
        return new JSONObject(readFile("setting.txt"));
    }

}
