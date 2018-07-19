package com.leia.apprtc.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.leia.apprtc.activity.BaseActivity;
import com.leia.apprtc.activity.ConnectActivity;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.enums.PNLogVerbosity;
import com.pubnub.api.enums.PNPushType;
import com.pubnub.api.enums.PNReconnectionPolicy;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.push.PNPushAddChannelResult;
import com.pubnub.api.models.consumer.push.PNPushRemoveChannelResult;

import java.util.Arrays;
import java.util.logging.Logger;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RegistrationIntentService extends IntentService {


    private static final String TAG = "RegIntentService";
    boolean isReg;
    String token;
    String channel;
    public static final String REGISTRATION_SUCCESS = "RegistrationSuccess";
    public static final String REGISTRATION_ERROR = "RegistrationError";
    private PubNub mPubnub;

    private static final String PUB_KEY = "pub-c-57562a41-c32c-44e8-9a89-aa97d90fa3c6";
    private static final String SUB_KEY = "sub-c-cea68fca-2940-11e8-8f89-fe0057f68997";

    private boolean fromClient;

    public RegistrationIntentService() {
        super("RegistrationIntentService");
    }





    @Override
    protected void onHandleIntent(Intent intent) {

        isReg = intent.getExtras().getBoolean("isReg");
        fromClient = intent.getExtras().getBoolean("fromClient");
        if (fromClient) channel = intent.getExtras().getString("channel");
        //String refreshedToken = FirebaseInstanceId.getInstance().getInstanceId().getResult().getToken();
        //punnub configuration
        PNConfiguration pnConfiguration = new PNConfiguration().
                setSubscribeKey(SUB_KEY).
                setPublishKey(PUB_KEY).
                setReconnectionPolicy(PNReconnectionPolicy.LINEAR).
                setMaximumReconnectionRetries(3).
                setSecure(true).
                setLogVerbosity(PNLogVerbosity.BODY);
        this.mPubnub = new PubNub(pnConfiguration);



        if(!fromClient) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    String newtoken = instanceIdResult.getToken();
                    Log.e("newToken",newtoken);
                    if (newtoken!=null && !token.equals(newtoken) && channel != null){
                        disablePushOnChannel(token, channel);
                        enablePushOnChannel(newtoken, channel);
                    }
                    if (newtoken!=null) token = newtoken;
                }
            });

        }

        if(fromClient) {
            if (token != null) {
                if (isReg) enablePushOnChannel(token, channel);
                else disablePushOnChannel(token, channel);
            }
            else{
                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        String newtoken = instanceIdResult.getToken();
                        Log.e("newToken",newtoken);
                        if (newtoken!=null){
                            token = newtoken;
                            if (isReg) enablePushOnChannel(newtoken, channel);
                            else disablePushOnChannel(token, channel);
                        }
                    }
                });


            }
        }


        //Log.d("refreshedToken", refreshedToken);
        /*FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                Log.e("newToken",newToken);

            }
        });*/

    }

    /*private void registerGCM() {
        //Registration complete intent initially null
        Intent registrationComplete = null;

        //Register token is also null
        //we will get the token on successfull registration
        try {
            //Creating an instanceid


            //Getting the token from the instance id
            String newtoken = FirebaseInstanceId.getInstance().getInstanceId().getResult().getToken();


            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("token", token);
            editor.apply();

            //EventBus.getDefault().post(new AppEvent("msg","token received"));

            //Displaying the token in the log
            Log.d("token", token);

            //on registration complete creating intent with success
            registrationComplete = new Intent(REGISTRATION_SUCCESS);

            //Putting the token to the intent
            registrationComplete.putExtra("token", token);

            enablePushOnChannel(token,channel);

        } catch (Exception e) {
            //If any error occurred
            Log.d("GCMRegIntentService", "Registration error");
            registrationComplete = new Intent(REGISTRATION_ERROR);
        }

        //Sending the broadcast that registration is completed
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }*/

   /* private void UnRegisterGCM() {
        //Registration complete intent initially null
        Intent registrationComplete = null;

        //Register token is also null
        //we will get the token on successfull registration
        try {

            token = FirebaseInstanceId.getInstance().getInstanceId().getResult().getToken();
            //Creating an instanceid
            *//*FirebaseInstanceId.getInstance().deleteInstanceId();

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("token", "");
            editor.apply();

            //EventBus.getDefault().post(new AppEvent("msg","token removed"));

            //on registration complete creating intent with success
            registrationComplete = new Intent(REGISTRATION_SUCCESS);*//*
            disablePushOnChannel(token, channel);

        } catch (Exception e) {
            //If any error occurred
            Log.d("GCMRegIntentService", "Registration error");
            registrationComplete = new Intent(REGISTRATION_ERROR);
        }

        //Sending the broadcast that registration is completed
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);

    }*/

    private void enablePushOnChannel(String regId, String channelName) {
        //adding regId to pubnub channel
        mPubnub.addPushNotificationsOnChannels()
                .pushType(PNPushType.GCM)
                .channels(Arrays.asList(channelName))
                .deviceId(regId)
                .async(new PNCallback<PNPushAddChannelResult>() {
                    @Override
                    public void onResponse(PNPushAddChannelResult result, PNStatus status) {
                        if (status.isError()) {
                            Log.d("T","Error on push notification" + status.getErrorData());
                        } else {
                            Log.d("T","Push notification added ");
                        }
                    }
                });

    }

    private void disablePushOnChannel(String regId, String channelName){
        //removing regId to pubnub channel
        mPubnub.removePushNotificationsFromChannels()
                .deviceId(regId)
                .channels(Arrays.asList(channelName))
                .pushType(PNPushType.GCM)
                .async(new PNCallback<PNPushRemoveChannelResult>() {
                    @Override
                    public void onResponse(PNPushRemoveChannelResult result, PNStatus status) {
                        if (status.isError()) {
                            Log.d("T","Error on push notification" + status.getErrorData());
                        } else {
                            Log.d("T","Push notification removed ");
                        }
                    }
                });
    }


}
