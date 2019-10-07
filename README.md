C4 aPI Cordova Plugin
======

Cordova Plugin for ID Tronic C4 Red on Android.
You need cordova-android version > 7.0.0
The plugin use a hook to create an build-extras.gradle file in your app under platforms/android/app.

Use:

const that = this;

(<any>window).C4ApiCordovaPlugin.common.getFirmware(function(result: ArrayBuffer) {
    console.log('Firmware: ', result);

    // show on ui
    if (result) {
        that.zone.run(()=> {
            that.firmware = String.fromCharCode.apply(null, new Uint8Array(result));
        });
        
        console.log('Firmaware converted: ',  that.firmware);
    }
}, function(error) {
        console.log('Firmware error: ', error);
        that.firmware = error;
});


UHF inventory (TID):

const that = this;

(<any>window).C4ApiCordovaPlugin.uhf.startInventory(function(result) {

    that.zone.run(()=> {
        that.tidList = result;
        console.log('Inventory result : ', result);
        (<any>window).C4ApiCordovaPlugin.stopInventory();
    });

}, function(error) {
    console.log('startInventory error: ', error);
    
});    
