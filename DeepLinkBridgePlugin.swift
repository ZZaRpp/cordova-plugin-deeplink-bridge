import Foundation

/*
 Requires Cordova-iOS >= 4.3 (Swift plugin support with automatic bridging
 header). Registers for the notification Cordova's own AppDelegate posts
 whenever application(_:open:options:) is called for a custom URL scheme
 registered in Info.plist (see plugin.xml, CFBundleURLTypes).
 */
@objc(DeepLinkBridgePlugin)
class DeepLinkBridgePlugin: CDVPlugin {

    private var callbackId: String?

    // Deep link that arrived before any JS listener registered (e.g. a
    // cold start), parked for delivery once registerListener runs.
    private var pendingUrl: String?

    override func pluginInitialize() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleOpenURLNotification(_:)),
            name: NSNotification.Name("CDVPluginHandleOpenURLNotification"),
            object: nil
        )
    }

    @objc func handleOpenURLNotification(_ notification: NSNotification) {
        guard let url = notification.object as? URL else { return }
        deliver(url: url.absoluteString)
    }

    // JS-facing action: cordova.plugins.DeepLinkBridge.addListener(...)
    // calls exec(..., 'DeepLinkBridge', 'registerListener', [])
    @objc(registerListener:)
    func registerListener(_ command: CDVInvokedUrlCommand) {
        self.callbackId = command.callbackId
        if let pending = pendingUrl {
            pendingUrl = nil
            deliver(url: pending)
        }
        // No result sent here on purpose — this callback stays open and
        // acts as a repeating JS event channel; each deep link is sent
        // via deliver(url:) below with keepCallback = true.
    }

    private func deliver(url: String) {
        guard let callbackId = callbackId else {
            pendingUrl = url
            return
        }
        let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: url)
        result?.setKeepCallbackAs(true)
        self.commandDelegate.send(result, callbackId: callbackId)
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
    }
}
