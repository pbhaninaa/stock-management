package com.example.finance;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;

public class UninstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Check if the broadcasted action is for app removal
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
            // Get the package name from the intent's data (it's in the form of "package:com.example.finance")
            String packageName = intent.getData().getSchemeSpecificPart();

            // Check if the removed package is your app
            if (packageName.equals(context.getPackageName())) {
                Utils.handleAppUninstall(context);
            }
        }
    }
}
