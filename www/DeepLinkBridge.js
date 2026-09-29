var exec = require('cordova/exec');

var DeepLinkBridge = {

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
