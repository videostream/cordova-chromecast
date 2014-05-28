package acidhax.cordova.chromecast;

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
	
	public MediaInfo createLoadUrlRequest(String contentId, String contentType, long duration, String streamType) {
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
	
	public void play(GoogleApiClient apiClient, ChromecastSessionCallback callback) {
		PendingResult<MediaChannelResult> res = this.remote.play(apiClient);
		res.setResultCallback(this.createMediaCallback(callback));
	}
	
	public void pause(GoogleApiClient apiClient, ChromecastSessionCallback callback) {
		PendingResult<MediaChannelResult> res = this.remote.pause(apiClient);
		res.setResultCallback(this.createMediaCallback(callback));
	}
	
	public void stop(GoogleApiClient apiClient, ChromecastSessionCallback callback) {
		PendingResult<MediaChannelResult> res = this.remote.stop(apiClient);
		res.setResultCallback(this.createMediaCallback(callback));
	}
	
	public void seek(long seekPosition, String resumeState, GoogleApiClient apiClient, final ChromecastSessionCallback callback) {
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
		
		res.setResultCallback(this.createMediaCallback(callback));
	}
	
	public void setVolume(double volume, GoogleApiClient apiClient, final ChromecastSessionCallback callback) {
		PendingResult<MediaChannelResult> res = this.remote.setStreamVolume(apiClient, volume);
		res.setResultCallback(this.createMediaCallback(callback));
	}
	
	public void setMuted(boolean muted, GoogleApiClient apiClient, final ChromecastSessionCallback callback) {
		PendingResult<MediaChannelResult> res = this.remote.setStreamMute(apiClient, muted);
		res.setResultCallback(this.createMediaCallback(callback));
	}
	
	private ResultCallback<RemoteMediaPlayer.MediaChannelResult> createMediaCallback(final ChromecastSessionCallback callback) {
		return new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
		    @Override
		    public void onResult(MediaChannelResult result) {
				if (result.getStatus().isSuccess()) {
					callback.onSuccess();
				} else {
					callback.onError("channel_error");
				}
		    }
		};
	}
}
