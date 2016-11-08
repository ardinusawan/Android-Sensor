package com.its.ardi.accelerometer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by ardi on 08/11/16.
 */
class GeocoderHandler extends Handler {
    @Override
    public void handleMessage(Message message) {
        String locationAddress;
        switch (message.what) {
            case 1:
                Bundle bundle = message.getData();
                locationAddress= bundle.getString("address");
                break;
            default:
                locationAddress= null;
        }
        MainActivity.loc = locationAddress;
        //tvAddress.setText(locationAddress);
        //Log.d("address",locationAddress);
    }
}
