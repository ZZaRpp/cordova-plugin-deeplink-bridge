# cordova-plugin-deeplink-bridge

Registers a custom URL scheme and delivers any incoming deep link to
JavaScript as a repeating event, whether the app was already running or was
cold-launched by the link.

It exists to solve one specific problem: a page opened via
`openInSystemBrowser` / `openInWebView` (or `RedirectToExternalURL`) runs in
a separate browser context that **cannot** call Cordova/Capacitor APIs — so
it can't call `cordova.plugins.OSInAppBrowser.close()` itself. The only way
back into the app from that page is a native mechanism: navigating to a
custom URL scheme the app has registered, which the OS routes to the app,
closing the browser as it does. This plugin is that mechanism.

## Install

Not published to npm — install from a git URL or a local path:

```
cordova plugin add https://github.com/<your-org>/cordova-plugin-deeplink-bridge --variable URL_SCHEME=myappregister
```

Pick a scheme that isn't used by anything else in the app already. **Do
not** reuse `app.outsystems.sallanddev.apalmasandboxmobile` — that's the
scheme OutSystems' own Auth plugin (`com.outsystems.plugins.auth.RedirectActivity`)
already owns for the login flow, and its `RedirectActivity` is written to
expect the login flow's own PKCE state, not an arbitrary URL. Keeping this
plugin on its own scheme avoids any conflict or ambiguity with that
activity.

### In OutSystems ODC

Add it as a Cordova dependency in the app's Extensibility Configuration
(the same place you'd add any other Cordova plugin by git URL), passing
`URL_SCHEME` as a variable if the UI supports it — otherwise fork the repo
and hardcode your scheme in `plugin.xml` before adding it.

## JavaScript API

```javascript
cordova.plugins.DeepLinkBridge.addListener(function (url) {
    console.log("Deep link received:", url);
});
```

Call this once, early (e.g. on `deviceready` in a JavaScript node run at app
start). Only one listener is kept at a time.

## Wiring it into the registration flow

1. **Pick a scheme**, e.g. `myappregister`, and install the plugin with it.
2. **Open the Entra registration URL** with `openInSystemBrowser`, as
   before — no change needed there.
3. **On your `PostRegister` page** (the https page Entra redirects to,
   still loaded inside the system browser), add a script that immediately
   navigates to your new scheme, carrying along whatever `code`/`state`
   the page received:

   ```html
   <script>
     window.location.replace(
       "myappregister://register-complete" + window.location.search
     );
   </script>
   ```

   The OS resolves that scheme to your app, `DeepLinkActivity` (Android) /
   the URL-open notification (iOS) fires, the system browser closes, and
   the app comes to the foreground.

4. **In the app**, a JavaScript node that ran `addListener` earlier
   receives the full URL (e.g.
   `myappregister://register-complete?code=...&state=register.c1a7...`).
   Parse `code`/`state` out of it if you need them, then continue —
   typically by starting the normal login flow (`GetExternalLoginURL` +
   `RedirectToExternalURL`) so the user ends up authenticated the same way
   a returning user would.

5. **Explicitly close the system browser** from the app side too, in case
   the OS left it in the back stack:

   ```javascript
   if (window.cordova && cordova.plugins.OSInAppBrowser) {
       cordova.plugins.OSInAppBrowser.close(
           function () {},
           function () {}
       );
   }
   ```

## What isn't verified

- This hasn't been built or run against a real ODC-generated native project
  — test it on both platforms before relying on it.
- `deliver()`'s "queue the last URL if no listener yet" logic only holds
  one pending URL. That's enough for this use case (one register flow at a
  time) but isn't a general-purpose deep-link queue.
- On iOS, confirm your Cordova-iOS version is new enough to compile a Swift
  plugin file without extra bridging-header setup (4.3+ handles this
  automatically; older forks may not).
