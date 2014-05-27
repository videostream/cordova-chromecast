package acidhax.cordova.chromecast;

import org.json.JSONObject;

public interface ChromecastOnMediaUpdatedListener {
	void onMediaLoaded(JSONObject media);
	void onMediaUpdated(JSONObject media);
}