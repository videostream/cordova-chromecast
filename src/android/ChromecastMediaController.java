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
	
	public MediaInfo createLoadUrlRequest(String contentId, String contentType, long duration, String streamType, boolean autoPlay, double currentTime) {
		MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
//    	mediaMetadata.putString(MediaMetadata.KEY_TITLE, "My video");

    	int _streamType = MediaInfo.STREAM_TYPE_BUFFERED;
    	if (streamType.equals("buffered")) {
    		
    	} else if (streamType.equals("live")) {
    		_streamType = MediaInfo.STREAM_TYPE_LIVE;
    	} else if (streamType.equals("other")) {
    		_streamType = MediaInfo.STREAM_TYPE_NONE;
    	}
    	
    	MediaInfo mediaInfo = new MediaInfo.Builder(contentId)
    	    .setContentType(contentType)
    	    .setStreamType(_streamType)
    	    .setStreamDuration(duration)
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
	
	public void seek(long seekPosition, String resumeState, GoogleApiClient apiClient, final CallbackContext callbackContext) {
		PendingResult<MediaChannelResult> res = null;
		if (resumeState != null && !resumeState.equals("")) {
			if (resumeState.equals("PLAYBACK_PAUSE")) {
				res = this.remote.seek(apiClient, seekPosition, RemoteMediaPlayer.RESUME_STATE_PAUSE);
			} else if (resumeState.equals("PLAYBACK_START")) {
				res = this.remote.seek(apiClient, seekPosition, RemoteMediaPlayer.RESUME_STATE_PLAY);
			} else {
				res = this.remote.seek(apiClient, seekPosition, RemoteMediaPlayer.RESUME_STATE_UNCHANGED);
			}
		}
		
		if (res == null) {
			res = this.remote.seek(apiClient, seekPosition);
		}
		
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
					callbackContext.success();
				} else {
					callbackContext.error("channel_error");
				}
		    }
		};
	}
}
