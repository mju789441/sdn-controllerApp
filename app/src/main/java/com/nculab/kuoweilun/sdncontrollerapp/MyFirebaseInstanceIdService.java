package com.nculab.kuoweilun.sdncontrollerapp;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * Created by kuo on 2018/3/19.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        sendToken(refreshedToken);
    }

    private void sendToken(String token) {
        try {
            String IP = new AppFile(this).getCurrentIP();
            ControllerURLConnection controllerURLConnection = new ControllerURLConnection(IP);
            new Subscribe(this, controllerURLConnection).subscrbe();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
