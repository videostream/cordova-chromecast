cordova-chromecast
==================

Chromecast in Cordova - The Beginning

##Installation
For now, add the plugin from this repository, we'll publish soon with more progress.

```
cordova plugin add https://github.com/acidhax/cordova-chromecast.git
```

The module will be on the window, no need to require it!

You will need to import the following projects as Library Projects in order for this plugin to work:

- `adt-bundle\sdk\extras\google\google_play_services\libproject\google-play-services_lib`
- `adt-bundle\sdk\extras\android\support\v7\appcompat`
- `adt-bundle\sdk\extras\android\support\v7\mediarouter`

##Usage

Use the global `chromecast` object. All callback functions should be implemented with the 
style of `function(err, obj) {}`

```javascript
chromecast.getDevices(appId); // This will begin the probing for possible Chromecast devices
chromecast.launch(chromecastId, appId, callback); // Launches an app on the given Chromecast
```

If your Chromecast app uses the MediaReceiver functionality - you may use these as well:
```javascript
chromecast.loadUrl(url, callback); // Starts playing a file on your Chromecast at the given URL
chromecast.play(callback); // Resumes playback on the Chromecast
chromecast.pause(callback); // Pauses playback on the Chromecast
chromecast.stop(callback); // Stops and unloads the current video on the Chromecast
chromecast.seek(seekPosition, callback); // Seeks to the given number of seconds
chromecast.setVolume(volumePercentage, callback); // Sets the volume, a value from 0 to 1
```

##Events
```javascript
chromecast.on("device", function (deviceId, deviceName) {}); // When a device is found
chromecast.on("deviceRemoved", function (deviceId, deviceName) {}); // When a device is lost
chromecast.on("volumeChanged", function (volume) {}); // When the MediaReceiver volume changes
chromecast.on("applicationStatusChanged", function (status) {}); // When the loaded app's status changes
chromecast.on("disconnect", function () {}); // When the app is disconnected
chromecast.on("message", function (namespace, message) {}); // When the app responds with a message

```

