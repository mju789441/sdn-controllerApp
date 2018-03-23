package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

/**
 * Created by kuo on 2018/3/24.
 */

public class UuidFile {

    Context context = null;

    public UuidFile(Context context) {
        this.context = context;
    }

    public String getUuid() {
        FileOutputStream outputStream;
        try {
            InputStream instream = context.openFileInput("uuid.txt");
            if (instream != null) {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String str = buffreader.readLine();
                inputreader.close();
                buffreader.close();
                return str;
            }
        } catch (Exception e) {
            try {
                String str = UUID.randomUUID().toString();
                outputStream = context.openFileOutput("uuid.txt", Context.MODE_PRIVATE);
                outputStream.write(str.getBytes());
                outputStream.close();
                return str;
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }
}
