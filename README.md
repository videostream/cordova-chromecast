cordova-chromecast
==================

Chromecast in Cordova

##Installation
For now, add the plugin from this repository, we'll publish soon with more progress.

```
cordova plugin add https://github.com/acidhax/cordova-chromecast.git
```

You will need to import the following projects as Library Projects in order for this plugin to work:

- `adt-bundle\sdk\extras\google\google_play_services\libproject\google-play-services_lib`
- `adt-bundle\sdk\extras\android\support\v7\appcompat`
- `adt-bundle\sdk\extras\android\support\v7\mediarouter`

##Usage

This project attempts to implement the official Google Cast SDK for Chrome... in Cordova. We've made a lot of progress in making this possible, check out the offical docs for examples: https://developers.google.com/cast/docs/chrome_sender


##Supported API Bits and Bites

Static and class things
- `chrome.cast.SessionRequest`
- `chrome.cast.apiConfig`
- `chrome.cast.initialize`
- `chrome.cast.requestSession`
- `chrome.cast.media.MediaInfo`
- `chrome.cast.media.LoadRequest`
- `chrome.cast.Volume`
- `chrome.cast.media.VolumeRequest`

`chrome.cast.Session` things
- `.addUpdateListener`
- `.removeUpdateListener`
- `.loadMedia`
- `.stop`

`chrome.cast.media.Media` things
- `.addUpdateListener`
- `.removeUpdateListener`
- `.pause`
- `.play`
- `.seek`
- `.setVolume`
- `.stop`
