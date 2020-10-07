var BionSmartLockPlugin = function(){};

BionSmartLockPlugin.prototype.storePassword = function(success, failure, args){
    cordova.exec(success, failure, "BionSmartLockPlugin", "storePassword", args);
};

BionSmartLockPlugin.prototype.retrievePassword = function(success, failure, args){
    cordova.exec(success, failure, "BionSmartLockPlugin", "retrievePassword", args);
};

BionSmartLockPlugin.prototype.deletePassword = function(success, failure, args){
    cordova.exec(success, failure, "BionSmartLockPlugin", "deletePassword", args);
};

//Plug in to Cordova
cordova.addConstructor(function() {
    if (!window.Cordova) {
        window.Cordova = cordova;
    };
    if(!window.plugins) window.plugins = {};
    window.plugins.BionSmartLockPlugin = new BionSmartLockPlugin();
});