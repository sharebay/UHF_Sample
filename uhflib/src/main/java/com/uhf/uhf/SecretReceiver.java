package com.uhf.uhf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SecretReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		System.out.println("SecretReceiver onReceive");
		String action = intent.getAction();
		if ("android.provider.Telephony.SECRET_CODE".equals(action)) {
			Intent startIntent = new Intent(Intent.ACTION_MAIN);
			startIntent.setClass(context, ConnectRs232.class);
			startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(startIntent);
		}
	}

}
