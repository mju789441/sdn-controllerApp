package com.nculab.kuoweilun.sdncontrollerapp.service;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.nculab.kuoweilun.sdncontrollerapp.AppFile;
import com.nculab.kuoweilun.sdncontrollerapp.controller.ControllerURLConnection;

import org.json.JSONException;

import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * Created by kuo on 2018/3/19.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    AppFile appFile = new AppFile(this);

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        sendToken(refreshedToken);
    }

    private void sendToken(String token) {
        try {
            String URL = appFile.getCurrentURL();
            ControllerURLConnection controllerURLConnection = new ControllerURLConnection(URL);
            controllerURLConnection.changeToken(this, token);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
