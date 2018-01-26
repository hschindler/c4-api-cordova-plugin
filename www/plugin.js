
var exec = require('cordova/exec');

var PLUGIN_NAME = 'C4ApiCordovaPlugin';

var C4ApiCordovaPlugin = {
  common: {
    getFirmware: function (cb, errorCb) {
      exec(cb, errorCb, PLUGIN_NAME, 'getFirmware', []);
    }
  },
  uhf: {
    startInventory: function (cb, errorCb) {
      exec(cb, errorCb, PLUGIN_NAME, 'startInventory', []);
    },
    stopInventory: function () {
      exec(null, null, PLUGIN_NAME, 'stopInventory', []);
    },
    setOutputPower: function (power, cb, errorCb) {
      exec(cb, errorCb, PLUGIN_NAME, 'setOutputPower', [power]);
    }
  },
  barcode: {
    scan: function (cb, errorCb) {
      exec(cb, errorCb, PLUGIN_NAME, 'scanBarcode', []);
    }
    // open: function (cb, errorCb) {
    //   exec(cb, errorCb, PLUGIN_NAME, 'openBarcode', []);
    // },
    // close: function () {
    //   exec(null, null, PLUGIN_NAME, 'closeBarcode', []);
    // }

  }


};

module.exports = C4ApiCordovaPlugin;
