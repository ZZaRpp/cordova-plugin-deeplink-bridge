package com.example.deeplinkbridge;

import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;

import java.lang.ref.WeakReference;

public class DeepLinkBridgePlugin extends CordovaPlugin {

    // Holds the one active instance so the static DeepLinkActivity can
    // reach back into the running webview without a broadcast.
    private static WeakReference<DeepLinkBridgePlugin> activeInstance;

    // If a deep link arrives before any JS listener has registered
    // (e.g. a cold start), it is parked here and delivered as soon as
    // registerListener runs.
    private static String pendingUrl;

    private CallbackContext listenerCallback;

    @Override
    protected void pluginInitialize() {
        activeInstance = new WeakReference<>(this);

        // Covers the cold-start case: the app's launch Intent itself
        // carried the deep link data.
        Intent launchIntent = cordova.getActivity().getIntent();
        if (launchIntent != null && launchIntent.getData() != null) {
            handleDeepLink(launchIntent.getData().toString());
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        if ("registerListener".equals(action)) {
            this.listenerCallback = callbackContext;
            if (pendingUrl != null) {
                String url = pendingUrl;
                pendingUrl = null;
                deliver(url);
            }
            // No PluginResult sent here on purpose: this callback is kept
            // open and used as a repeating JS event channel. Each incoming
            // deep link is delivered via deliver() below with
            // setKeepCallback(true), so the JS success handler fires once
            // per deep link instead of once total.
            return true;
        }
        return false;
    }

    /**
     * Called by DeepLinkActivity (a different Activity, same process) when
     * a deep link Intent comes in while the app is already running, and by
     * onNewIntent below if the OS instead redelivers to this Activity
     * directly.
     */
    public static void handleDeepLink(String url) {
        DeepLinkBridgePlugin instance = activeInstance != null ? activeInstance.get() : null;
        if (instance != null) {
            instance.deliver(url);
        } else {
            pendingUrl = url;
        }
    }

    private void deliver(final String url) {
        if (listenerCallback == null) {
            pendingUrl = url;
            return;
        }
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PluginResult result = new PluginResult(PluginResult.Status.OK, url);
                result.setKeepCallback(true);
                listenerCallback.sendPluginResult(result);
            }
        });
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getData() != null) {
            handleDeepLink(intent.getData().toString());
        }
    }
}
