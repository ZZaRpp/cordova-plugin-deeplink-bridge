package com.example.deeplinkbridge;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

/**
 * Registered in AndroidManifest.xml (via plugin.xml) as the sole handler
 * for the custom URL scheme. It has no UI: it exists purely to catch the
 * VIEW/BROWSABLE intent the OS sends when a Custom Tab (or any other app)
 * navigates to that scheme, hand the URL to DeepLinkBridgePlugin, bring the
 * app's own launcher Activity to the foreground, and immediately finish.
 *
 * Bringing the launcher Activity to the front this way is what closes the
 * Custom Tab / SafariViewController session, the same way it closes when a
 * federated-login redirect lands on this app's own scheme.
 */
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
            // If this ever fails, the URL is still queued in
            // DeepLinkBridgePlugin and will be delivered once the app's
            // own Activity is next foregrounded/resumed.
        }
    }
}
