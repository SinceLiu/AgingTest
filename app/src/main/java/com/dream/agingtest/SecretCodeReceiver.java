package com.dream.agingtest;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class SecretCodeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		android.util.Log.e("TEST", "intent = " + intent);
		android.util.Log.e("TEST", "intent.getAction() = " + intent.getAction());
		android.util.Log.e("TEST", "intent.getDataString() = " + intent.getDataString());
		Intent activityIntent = new Intent();
		activityIntent.setComponent(new ComponentName("com.dream.agingtest", "com.dream.agingtest.MainActivity"));
		activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(activityIntent);
	}
}
