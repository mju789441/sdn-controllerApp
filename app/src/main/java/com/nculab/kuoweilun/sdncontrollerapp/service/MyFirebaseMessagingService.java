package com.nculab.kuoweilun.sdncontrollerapp.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nculab.kuoweilun.sdncontrollerapp.R;
import com.nculab.kuoweilun.sdncontrollerapp.database.FlowWarn_table;
import com.nculab.kuoweilun.sdncontrollerapp.database.UUID_table;

import org.json.JSONException;
import org.json.JSONObject;

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
                String text = "";
                UUID_table uuid_table = new UUID_table(this);
                FlowWarn_table flowWarn_table = new FlowWarn_table(this);
                for (int i = 0; i < uuid.names().length(); i++) {
                    JSONObject item = uuid_table.get(uuid.names().getString(i));
                    if (item.getString(UUID_table.EVENT_COLUMN).equals(UUID_table.EVENT_FLOWWARN)) {
                        JSONObject flowWarn = flowWarn_table.getFlowWarn(uuid.names().getString(i));
                        text += "s" + flowWarn.getString(FlowWarn_table.SWITCH_ID_COLUMN)
                                + " 的 port_no: " + flowWarn.getString(FlowWarn_table.PORT_NO_COLUMN)
                                + " 以 " + flowWarn.getString(FlowWarn_table.SPEED_COLUMN)
                                + " Kb/s 持續 " + flowWarn.getString(FlowWarn_table.DURATION_COLUMN)
                                + " 秒\n";
                    } else {
                        if (item.getString(UUID_table.EVENT_COLUMN).equals(UUID_table.EVENT_SWITCHENTER))
                            text += "有新的 switch 加入\n";
                        else if (item.getString(UUID_table.EVENT_COLUMN).equals(UUID_table.EVENT_SWITCHLEAVE))
                            text += "有 switch 離開\n";
                    }
                }
                uuid_table.close();
                flowWarn_table.close();
                //前端時 notification
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_group_collapse_00)
                        .setContentTitle("notification:")
                        .setContentText(text)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(text))
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
            } catch (Exception e) {
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
