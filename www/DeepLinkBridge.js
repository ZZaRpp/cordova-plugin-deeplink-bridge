var exec = require('cordova/exec');

var DeepLinkBridge = {
    /**
     * Registers a handler that fires every time the app receives a deep
     * link on the custom URL scheme configured at install time (see the
     * URL_SCHEME plugin variable). Fires for links received both while the
     * app is running and links that cold-launched the app (the native side
     * queues one URL if it arrives before a listener is registered).
     *
     * Only one listener is kept at a time; calling this again replaces the
     * previous one.
     *
     * @param {function(string)} onDeepLink - called with the full URL
     *        string, e.g. "osdlbridge://register-complete?code=..."
     */
    addListener: function (onDeepLink) {
        exec(
            function (url) {
                if (typeof onDeepLink === 'function') {
                    onDeepLink(url);
                }
            },
            function (err) {
                console.error('DeepLinkBridge error', err);
            },
            'DeepLinkBridge',
            'registerListener',
            []
        );
    }
};

module.exports = DeepLinkBridge;
