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
        try {
            saveToken(refreshedToken);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveToken(String token) throws IOException {
        new AppFile(this).saveToken(token);
    }
}
