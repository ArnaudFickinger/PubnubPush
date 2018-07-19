
public class ConnectActivity extends BaseActivity implements
        PubnubChannelClient.PubnubChannelEvents,
        ConnectedFragment.ConnectedFragmentListener,
        DisconnectedFragment.DisconnectedFragmentListener,
        IncomingCallFragment.IncomingCallResponseListener,
        PubnubCallHistoryAdapter.OnCallHistoryItemClickListener {


    private void StartRegistrationService(boolean register, String channel){
            //Starting intent to register device
            Intent intent = new Intent(this, RegistrationIntentService.class);
            intent.putExtra("channel", channel);
            intent.putExtra("fromClient", true);
            if(true) intent.putExtra("isReg",true);
            else intent.putExtra("isReg",false);
            startService(intent);
    }


    @Override
    public void onDisconnectPubnub() {
        setFullScreenImmersive(fragmentContainer);
        String chnl = pubnubChannelClient.getChannel();
        pubnubChannelClient.disconnect();
        StartRegistrationService(false, chnl);

    }

    /* DisconnectedFragment.DisconnectedFragmentListener, */

    @Override
    public void onConnectPubnub(String username) {
        connectPubnub(username);
        StartRegistrationService(true, username);
    }

    /* PubnubChannelClient.PubnubChannelEvents */


    private void connectPubnub(String username) {
        if (username.isEmpty()) {
            logAndToast("Invalid username " + username, Toast.LENGTH_SHORT);
            return;
        }
        pubnubChannelClient.connect(username);
    }

    }
}
