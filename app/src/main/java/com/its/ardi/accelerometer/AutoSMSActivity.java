package com.its.ardi.accelerometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

/**
 * Created by Fikri on 10/11/2016.
 */

public class AutoSMSActivity extends BroadcastReceiver {
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent){
        if(intent.getAction().equals(SMS_RECEIVED)){
            Bundle bundle = intent.getExtras();
            if (bundle!=null){
                Object[] pdus = (Object[])bundle.get("pdus");
                final SmsMessage[] sms = new SmsMessage[pdus.length];
                String content="gskfnknf", numberto="087853596908";
                for(int i=0; i<pdus.length; i++){
                    sms[i]=SmsMessage.createFromPdu((byte[])pdus[i]);
                    content = sms[i].getMessageBody();
                    numberto = sms[i].getOriginatingAddress();
                }
                String message = "Auto Reply: Maaf saya sedang dalam posisi berkendara";
                SmsManager smsSend = SmsManager.getDefault();
                smsSend.sendTextMessage(numberto,null,message,null,null);
            }
        }
    }
    public void onProviderDisabled(String arg0){

    }
}
