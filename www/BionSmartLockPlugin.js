var exec = require('cordova/exec');

exports.storePassword = function (arg0, success, error) {
    exec(success, error, 'BionSmartLockPlugin', 'storePassword', [arg0]);
};

exports.retrievePassword = function (arg0, success, error) {
    exec(success, error, 'BionSmartLockPlugin', 'retrievePassword', [arg0]);
};

exports.deletePassword = function (arg0, success, error) {
    exec(success, error, 'BionSmartLockPlugin', 'deletePassword', [arg0]);
};