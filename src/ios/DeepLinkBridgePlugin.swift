import Foundation

@objc(DeepLinkBridgePlugin)
class DeepLinkBridgePlugin: CDVPlugin {

    private var callbackId: String?
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

    @objc(registerListener:)
    func registerListener(_ command: CDVInvokedUrlCommand) {
        self.callbackId = command.callbackId
        if let pending = pendingUrl {
            pendingUrl = nil
            deliver(url: pending)
        }
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
