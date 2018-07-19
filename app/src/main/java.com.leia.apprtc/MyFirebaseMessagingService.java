package com.leia.apprtc.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.leia.apprtc.R;
import com.leia.apprtc.activity.CallActivity;
import com.leia.apprtc.activity.ConnectActivity;
import com.leia.apprtc.rtc.PubnubChannelClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;
import static com.leia.apprtc.util.AppUtils.formatTimeStamp;
import static com.leia.apprtc.util.JsonUtils.jsonConvert;
import static com.leia.apprtc.util.JsonUtils.jsonGetBool;
import static com.leia.apprtc.util.JsonUtils.jsonGetString;

public class MyFirebaseMessagingService extends FirebaseMessagingService{

    private String TAG = "MyFirebaseMessagingService";
    private static final String NOTIF_CHANNEL = "leia.apprtc.NOTIF_CHANNEL";
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //todo sendRegistrationToServer(token);

        Intent intent = new Intent(this, RegistrationIntentService.class).putExtra("isReg",true).putExtra("fromClient",false);
        startService(intent);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        super.onMessageReceived(remoteMessage);

        uiHandler.post(() -> {
                    JSONObject msgjson = new JSONObject(remoteMessage.getData());
                    PubnubChannelClient.MessageType type = null;
                    String callerChannel="";
                    String roomId="";
                    try {
                        msgjson = jsonConvert(msgjson);
                        callerChannel = msgjson.getString("publisher");
                        type = PubnubChannelClient.MessageType.valueOf(msgjson.getString("type"));
                        JSONObject payload = jsonConvert(msgjson.getJSONObject("payload"));
                        roomId = jsonGetString(payload, "roomId", "");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (type == PubnubChannelClient.MessageType.CALL_REQUEST) {
                        if (roomId.isEmpty()) {
                            Log.e(TAG, "Unexpected empty roomId CALL_REQUEST message: channel = " + callerChannel);
                            return;
                        }
                        sendIncomingCallNotification(roomId, callerChannel);
                    }
                });



        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private void sendIncomingCallNotification(final String roomId, final String peerChannel) {
        Intent intent = new Intent(this, ConnectActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIF_CHANNEL)
                .setSmallIcon(R.drawable.holochat_bwg)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Push call from " + peerChannel)
                .setContentIntent(pendingIntent)
                .setVisibility(VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify((int)System.currentTimeMillis(), builder.build());
    }
}
