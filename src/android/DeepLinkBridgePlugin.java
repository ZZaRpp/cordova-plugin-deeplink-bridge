package com.productleague.deeplinkbridge;

import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;

import java.lang.ref.WeakReference;

public class DeepLinkBridgePlugin extends CordovaPlugin {

    private static WeakReference<DeepLinkBridgePlugin> activeInstance;
    private static String pendingUrl;
    private CallbackContext listenerCallback;

    @Override
    protected void pluginInitialize() {
        activeInstance = new WeakReference<>(this);

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
            return true;
        }
        return false;
    }

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
