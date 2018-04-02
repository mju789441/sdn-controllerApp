package com.nculab.kuoweilun.sdncontrollerapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * Created by kuo on 2018/3/19.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        String msg_payload = null;
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            msg_payload = remoteMessage.getData().toString();
            Log.d(TAG, "Message data payload: " + msg_payload);
            try {
                JSONObject uuid = new JSONObject(msg_payload);
                for (int i = 0; i < uuid.names().length(); i++) {
                    String content = new AppFile(this).getUuidTable(uuid.names().getString(i));
                    String title;
                    try {
                        JSONObject jsonObject = new JSONObject(content);
                        String switch_ID = jsonObject.names().getString(0);
                        String port_no = jsonObject.getJSONObject(switch_ID).names().getString(0);
                        title = "FlowWarning: { switch: " + switch_ID
                                + " port_no: " + port_no + " }";
                    } catch (JSONException e) {
                        title = content;
                    }
                    //前端時 notification
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_group_collapse_00)
                            .setContentTitle(title)
                            .setContentText(title)
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(title))
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                    NotificationManager notificationManager;
                    //Android 8.0以上
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mBuilder.setChannelId("sdn app");
                        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
                        NotificationChannel channel = new NotificationChannel("sdn app",
                                "sdn application",
                                NotificationManager.IMPORTANCE_DEFAULT);
                        channel.setDescription("sdn notification");
                        notificationManager.createNotificationChannel(channel);
                    } else {//Android 8.0以下
                        notificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    }
                    notificationManager.notify(1, mBuilder.build());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            //前端時 notification
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_group_collapse_00)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(remoteMessage.getNotification().getBody()))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManager notificationManager;
            //Android 8.0以上
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBuilder.setChannelId("sdn app");
                notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel channel = new NotificationChannel("sdn app",
                        "sdn application",
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("sdn notification");
                notificationManager.createNotificationChannel(channel);
            } else {//Android 8.0以下
                notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            }
            notificationManager.notify(1, mBuilder.build());
        }
    }
}
