package com.nculab.kuoweilun.sdncontrollerapp;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by kuo on 2018/3/19.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.e("", "refresh token:"+refreshedToken);
    }

    public void sendRegistrationToServer(String token){

    }
}
