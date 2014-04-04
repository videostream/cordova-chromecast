cordova-chromecast
==================

Chromecast running in Cordova

```
Chromecast.on("device", callback);
Chromecast.on("deviceRemoved", callback);
Chromecast.on("volumeChanged", callback);
Chromecast.on("applicationStatusChanged", callback);
Chromecast.on("disconnect", callback);
Chromecast.on("message", callback);

Chromecast.launch(chromecastId, callback);
Chromecast.loadUrl(url, callback);
Chromecast.play(callback);
Chromecast.pause(callback);
Chromecast.stop(callback);
Chromecast.seek(seekPosition, callback);
Chromecast.setVolume(volumePercentage, callback);
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