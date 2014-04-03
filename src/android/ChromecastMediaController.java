package acidhax.cordova.chromecast;

import org.apache.cordova.CallbackContext;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;

public class ChromecastMediaController {
	private RemoteMediaPlayer remote = null;
	public ChromecastMediaController(RemoteMediaPlayer mRemoteMediaPlayer) {
		this.remote = mRemoteMediaPlayer;
	}
	
	public MediaInfo createLoadUrlRequest(String url) {
		MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
    	mediaMetadata.putString(MediaMetadata.KEY_TITLE, "My video");

    	MediaInfo mediaInfo = new MediaInfo.Builder(url)
    	    .setContentType("video/mp4")
    	    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
    	    .setMetadata(mediaMetadata)
    	    .build();
    	
    	return mediaInfo;
	}
	
	public void play(GoogleApiClient apiClient, final CallbackContext callbackContext) {
		PendingResult<MediaChannelResult> res = this.remote.play(apiClient);
		res.setResultCallback(this.createMediaCallback(callbackContext));
	}
	
	public void pause(GoogleApiClient apiClient, final CallbackContext callbackContext) {
		PendingResult<MediaChannelResult> res = this.remote.pause(apiClient);
		res.setResultCallback(this.createMediaCallback(callbackContext));
	}
	
	public void stop(GoogleApiClient apiClient, final CallbackContext callbackContext) {
		PendingResult<MediaChannelResult> res = this.remote.stop(apiClient);
		res.setResultCallback(this.createMediaCallback(callbackContext));
	}
	
	public void seek(long seekPosition, GoogleApiClient apiClient, final CallbackContext callbackContext) {
		PendingResult<MediaChannelResult> res = this.remote.seek(apiClient, seekPosition);
		res.setResultCallback(this.createMediaCallback(callbackContext));
	}
	
	public void setVolume(double volume, GoogleApiClient apiClient, final CallbackContext callbackContext) {
		PendingResult<MediaChannelResult> res = this.remote.setStreamVolume(apiClient, volume);
		res.setResultCallback(this.createMediaCallback(callbackContext));
	}
	
	public void setMute(boolean muted, GoogleApiClient apiClient, final CallbackContext callbackContext) {
		PendingResult<MediaChannelResult> res = this.remote.setStreamMute(apiClient, muted);
		res.setResultCallback(this.createMediaCallback(callbackContext));
	}
	
	private ResultCallback<RemoteMediaPlayer.MediaChannelResult> createMediaCallback(final CallbackContext callbackContext) {
		return new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
		    @Override
		    public void onResult(MediaChannelResult result) {
				if (result.getStatus().isSuccess()) {
					System.out.println("Media loaded successfully");
					callbackContext.success();
				} else {
					callbackContext.error("request failed");
				}
		    }
		};
	}
}
