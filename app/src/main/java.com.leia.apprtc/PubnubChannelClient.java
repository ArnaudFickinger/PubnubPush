

    /* should be called from pubnub thread */
    private void transmitMessage(final String channel,
                                 final MessageType type,
                                 final JSONObject payload,
                                 final TransmitMessageCallback callback) {
        checkIfCalledOnValidThread();

        JSONObject json = new JSONObject();
        JSONObject fcmPayload = new JSONObject();
        JSONObject data = new JSONObject();

        jsonPut(json, "publisher", this.channel);
        jsonPut(json, "publisherUuid", this.pubnub.getConfiguration().getUuid());
        jsonPut(json, "timestamp", System.currentTimeMillis());
        jsonPut(json, "type", type);
        jsonPut(json, "payload", payload);

        jsonPut(data, "publisher", this.channel);
        jsonPut(data, "publisherUuid", this.pubnub.getConfiguration().getUuid());
        jsonPut(data, "timestamp", System.currentTimeMillis());
        jsonPut(data, "type", type);
        jsonPut(data, "payload", payload);


        jsonPut(fcmPayload, "data", data);

        jsonPut(json, "pn_gcm", fcmPayload);


        this.pubnub.publish().message(json).channel(channel)
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        if(status.isError()) {
                            Log.d(TAG,"publish error: " + status.getErrorData().toString());
                            if (callback != null)
                                callback.onError(result, status);
                        } else {
                            Log.d(TAG,"publish: " + result.toString());
                            if (callback != null)
                                callback.onSuccess(result, status);
                        }
                    }
                });
    }

    

}
