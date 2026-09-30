package com.productleague.deeplinkbridge;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

public class DeepLinkActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
        bringAppToForeground();
        finish();
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        Uri uri = intent.getData();
        if (uri != null) {
            DeepLinkBridgePlugin.handleDeepLink(uri.toString());
        }
    }

    private void bringAppToForeground() {
        try {
            PackageManager pm = getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(getPackageName());
            if (launchIntent != null) {
                launchIntent.setFlags(
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                                | Intent.FLAG_ACTIVITY_NEW_TASK
                );
                startActivity(launchIntent);
            }
        } catch (Exception e) {

        }
    }
}
