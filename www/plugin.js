
var exec = require('cordova/exec');

var PLUGIN_NAME = 'C4ApiCordovaPlugin';

var C4ApiCordovaPlugin = {
  echo: function(phrase, cb) {
    exec(cb, null, PLUGIN_NAME, 'echo', [phrase]);
  },
  getFirmware: function(cb, errorCb) {
    exec(cb, errorCb, PLUGIN_NAME, 'getFirmware', []);
  },
  startInventory: function(cb, errorCb) {
    exec(cb, errorCb, PLUGIN_NAME, 'startInventory', []);
  },
  stopInventory: function() {
    exec(null, null, PLUGIN_NAME, 'stopInventory', []);
  }
};

module.exports = C4ApiCordovaPlugin;
