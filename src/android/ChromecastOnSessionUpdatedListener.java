package acidhax.cordova.chromecast;

import org.json.JSONObject;

public interface ChromecastOnSessionUpdatedListener {
	void onSessionUpdated(boolean isAlive, JSONObject properties);
}