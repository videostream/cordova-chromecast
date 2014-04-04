cordova-chromecast
==================

Chromecast running in Cordova

```
Chromecast.on("device", function (deviceId, deviceName) {
});
Chromecast.on("deviceRemoved", function (deviceId, deviceName) {
});
Chromecast.on("volumeChanged", function (volume) {
});
Chromecast.on("applicationStatusChanged", function (status) {
});
Chromecast.on("disconnect", function () {
});
Chromecast.on("message", function (namespace, message) {
});

Chromecast.launch(chromecastId, callback);
Chromecast.loadUrl(url, callback);
Chromecast.play(callback);
Chromecast.pause(callback);
Chromecast.stop(callback);
Chromecast.seek(seekPosition, callback);
Chromecast.setVolume(volumePercentage, callback);

All of the functions follow the node err style callbacks.

function callback(err, result) {
	
}
```

You will need to import the projects:
```
	adt-bundle\sdk\extras\google\google_play_services\libproject\google-play-services_lib
	adt-bundle\sdk\extras\android\support\v7\appcompat
	adt-bundle\sdk\extras\android\support\v7\mediarouter
```
MediaRouter depends on Appcompat
```
Right click 'android-support-v7-mediarouter'
Select Android
Add Library 'android-support-v7-appcompat'
...
Profit!
```