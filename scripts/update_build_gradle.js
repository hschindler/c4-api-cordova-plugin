module.exports = function (context) {
    if (context.opts.cordova.platforms.indexOf('android') < 0) {
        return;
    }
    console.log("Starting gradle modifications");
    // const path = context.requireCordovaModule('path');
    // const fs = context.requireCordovaModule('fs');
    const path = require('path');
    const fs = require('fs');
    const gradlePath = path.join(context.opts.projectRoot, 'platforms/android/app/build-extras.gradle');
    const gradleExtraPath = path.join(context.opts.projectRoot, 'plugins/c4-api-cordova-plugin/build-extras.gradle');
    return new Promise(function (resolve, reject) {
        fs.copyFile(gradleExtraPath, gradlePath, function (err) {
            if (err) {
                console.error("Failed to copy to " + gradlePath + " from " + gradleExtraPath);
                reject(err);
            } else {
                console.log("Copied to " + gradlePath + " successfully");
                resolve();
            }
        });
    });
};