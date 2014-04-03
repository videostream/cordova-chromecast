cordova-chromecast
==================

Chromecast running in Cordova

```
Chromecast.launch(chromecastId, callback);
Chromecast.loadUrl(url, callback);
Chromecast.on("device", callback);
Chromecast.on("deviceRemoved", callback);
Chromecast.on("launch", callback);
Chromecast.on("launchFailed", callback);
Chromecast.on("volumeChanged", callback);
Chromecast.on("applicationStatusChanged", callback);
Chromecast.on("disconnect", callback);
Chromecast.on("message", callback);
Chromecast.play(callback);
Chromecast.pause(callback);
Chromecast.stop(callback);
Chromecast.seek(seekPosition, callback);
Chromecast.setVolume(volumePercentage, callback);
```
