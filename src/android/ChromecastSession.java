package acidhax.cordova.chromecast;

import java.io.IOException;
import java.util.List;

import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.cast.RemoteMediaPlayer.OnMetadataUpdatedListener;
import com.google.android.gms.cast.RemoteMediaPlayer.OnStatusUpdatedListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.images.WebImage;

import android.os.Bundle;
import android.support.v7.media.MediaRouter.RouteInfo;

/*
 * All of the Chromecast session specific functions should start here. 
 */
public class ChromecastSession extends Cast.Listener implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMetadataUpdatedListener, OnStatusUpdatedListener {

	private RouteInfo routeInfo = null;
	private volatile GoogleApiClient mApiClient = null;	
	private volatile RemoteMediaPlayer mRemoteMediaPlayer;
	private CordovaInterface cordova = null;
	private CastDevice device = null;
	private ChromecastMediaController chromecastMediaController;
	private ChromecastOnMediaUpdatedListener onMediaUpdatedListener;
	private ChromecastOnSessionUpdatedListener onSessionUpdatedListener;
	
	private volatile String appId;
	private volatile String displayName;
	private volatile List<WebImage> appImages;
	private volatile String sessionId = null;
	
	private ChromecastSessionCallback launchCallback;
	
	public ChromecastSession(RouteInfo routeInfo, CordovaInterface cordovaInterface, 
			ChromecastOnMediaUpdatedListener onMediaUpdatedListener, ChromecastOnSessionUpdatedListener onSessionUpdatedListener) {
		this.cordova = cordovaInterface;
        this.onMediaUpdatedListener = onMediaUpdatedListener;
        this.onSessionUpdatedListener = onSessionUpdatedListener;
        this.routeInfo = routeInfo;
		this.device = CastDevice.getFromBundle(this.routeInfo.getExtras());
		
		this.mRemoteMediaPlayer = new RemoteMediaPlayer();
		this.mRemoteMediaPlayer.setOnMetadataUpdatedListener(this);
		this.mRemoteMediaPlayer.setOnStatusUpdatedListener(this);
		
		this.chromecastMediaController = new ChromecastMediaController(mRemoteMediaPlayer);
	}

	
	/**
	 * Sets the wheels in motion - connects to the Chromecast and launches the given app
	 * @param appId
	 */
	public void launch(String appId, ChromecastSessionCallback launchCallback) {
		this.appId = appId;
		this.launchCallback = launchCallback;
		this.connectToDevice();
	}
	
	public void kill (final ChromecastSessionCallback callback) {
		this.mRemoteMediaPlayer.stop(mApiClient).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
			@Override
			public void onResult(MediaChannelResult result) {
				try {
					Cast.CastApi.stopApplication(mApiClient);
					mApiClient.disconnect();
				} catch(Exception e) {
					
				}
				
				callback.onSuccess();
			}
		});
		Cast.CastApi.stopApplication(mApiClient);
	}
	
	public boolean loadMedia(String contentId, String contentType, long duration, String streamType, boolean autoPlay, double currentTime, final ChromecastSessionCallback callback) {
		try {
			MediaInfo mediaInfo = chromecastMediaController.createLoadUrlRequest(contentId, contentType, duration, streamType, autoPlay, currentTime);
			
			mRemoteMediaPlayer.load(mApiClient, mediaInfo, true)
				.setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
					@Override
					public void onResult(MediaChannelResult result) {
						if (result.getStatus().isSuccess()) {
							System.out.println("Media loaded successfully");

							callback.onSuccess(ChromecastSession.this.createMediaObject());
						
						} else {
							callback.onError("session_error");
						}
				    }
				});
    	} catch (IllegalStateException e) {
    		e.printStackTrace();
    		System.out.println("Problem occurred with media during loading");
    		callback.onError("session_error");
    		return false;
    	} catch (Exception e) {
    		e.printStackTrace();
    		callback.onError("session_error");
    		System.out.println("Problem opening media during loading");
    		return false;
    	}
    	return true;
	}
	
	public void mediaPlay(ChromecastSessionCallback callback) {
		chromecastMediaController.play(mApiClient, callback);
	}
	
	public void mediaPause(ChromecastSessionCallback callback) {
		chromecastMediaController.pause(mApiClient, callback);
	}
	
	public void mediaSeek(long seekPosition, String resumeState, ChromecastSessionCallback callback) {
		chromecastMediaController.seek(seekPosition, resumeState, mApiClient, callback);
	}
	
	public void mediaSetVolume(double level, ChromecastSessionCallback callback) {
		chromecastMediaController.setVolume(level, mApiClient, callback);
	}
	
	public void mediaSetMuted(boolean muted, ChromecastSessionCallback callback) {
		chromecastMediaController.setMuted(muted, mApiClient, callback);
	}
	
	public void mediaStop(ChromecastSessionCallback callback) {
		chromecastMediaController.stop(mApiClient, callback);
	}
	
//	public void setVolume(double volume, CallbackContext callbackContext) {
//		// chromecastMediaController.setVolume(volume, mApiClient, callbackContext);
//		try {
//			Cast.CastApi.setVolume(mApiClient, volume);
//			callbackContext.success();
//		} catch (Exception e) {
//			e.printStackTrace();
//			callbackContext.error("Failure");
//		}
//	}
//	
//	public void setMute(boolean muted, CallbackContext callbackContext) {
//		chromecastMediaController.setMute(muted, mApiClient, callbackContext);
//	}
	
	private void connectToDevice() {
		Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions.builder(this.device, this);
		this.mApiClient = new GoogleApiClient.Builder(this.cordova.getActivity().getApplicationContext())
			.addApi(Cast.API, apiOptionsBuilder.build())
	        .addConnectionCallbacks(this)
	        .addOnConnectionFailedListener(this)
	        .build();
		
		this.mApiClient.connect();
	}
	
	private void launchApplication() {
		Cast.CastApi.launchApplication(mApiClient, this.appId, false)
		.setResultCallback(launchApplicationResultCallback);
	}
	
	private void connectRemoteMediaPlayer() throws IllegalStateException, IOException {
		Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
		mRemoteMediaPlayer.requestStatus(mApiClient)
		.setResultCallback(connectRemoteMediaPlayerCallback);
	}
	
	
	/**
	 * launchApplication callback
	 */
	private ResultCallback<Cast.ApplicationConnectionResult> launchApplicationResultCallback = new ResultCallback<Cast.ApplicationConnectionResult>() {
		@Override
		public void onResult(ApplicationConnectionResult result) {
			
			ApplicationMetadata metadata = result.getApplicationMetadata();
			ChromecastSession.this.sessionId = result.getSessionId();
			ChromecastSession.this.displayName = metadata.getName();
			ChromecastSession.this.appImages = metadata.getImages();
		
			Status status = result.getStatus();
			
			if (status.isSuccess()) {
				try {
					ChromecastSession.this.launchCallback.onSuccess(createSessionObject());
					connectRemoteMediaPlayer();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				
			}
		}
		
	};
	
	/**
	 * connectRemoteMediaPlayer callback
	 */
	private ResultCallback<RemoteMediaPlayer.MediaChannelResult> connectRemoteMediaPlayerCallback = new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
		@Override
		public void onResult(MediaChannelResult result) {
			if (result.getStatus().isSuccess()) {
				
			} else {
				System.out.println("Failed to request status.");
			}
		}
	};
	
	private JSONObject createSessionObject() {		
		JSONObject out = new JSONObject();
		try {
			out.put("appId", this.appId);
		
			if (this.appImages != null) {
				JSONArray appImages = new JSONArray();
				for(WebImage o : this.appImages) {
					appImages.put(o.toString());
				}
			}
			
			out.put("appImages", appImages);
			out.put("sessionId", this.sessionId);
			out.put("displayName", this.displayName);
			
			JSONObject receiver = new JSONObject();
			receiver.put("friendlyName", this.device.getFriendlyName());
			receiver.put("label", this.device.getDeviceId());
			
			JSONObject volume = new JSONObject();
			volume.put("level", Cast.CastApi.getVolume(mApiClient));
			volume.put("muted", Cast.CastApi.isMute(mApiClient));
			
			receiver.put("volume", volume);
			
			out.put("receiver", receiver);
			
		} catch(JSONException e) {
			
		}
		
		return out;
	}
	
	private JSONObject createMediaObject() {
		JSONObject out = new JSONObject();
		JSONObject objInfo = new JSONObject();
		
		MediaStatus mediaStatus = mRemoteMediaPlayer.getMediaStatus();
		if (mediaStatus == null) {
			return out;
		}
		
		MediaInfo mediaInfo = mediaStatus.getMediaInfo();
		try {
			out.put("media", objInfo);
			out.put("mediaSessionId", 1);
			out.put("currentTime", mediaStatus.getStreamPosition() / 1000.0);
			out.put("playbackRate", mediaStatus.getPlaybackRate());
			out.put("customData", mediaStatus.getCustomData());
			
			switch(mediaStatus.getPlayerState()) {
				case MediaStatus.PLAYER_STATE_BUFFERING:
					out.put("playerState", "BUFFERING"); break;
				case MediaStatus.PLAYER_STATE_IDLE:
					out.put("playerState", "IDLE"); break;
				case MediaStatus.PLAYER_STATE_PAUSED:
					out.put("playerState", "PAUSED"); break;
				case MediaStatus.PLAYER_STATE_PLAYING:
					out.put("playerState", "PLAYING"); break;
				case MediaStatus.PLAYER_STATE_UNKNOWN:
					out.put("playerState", "UNKNOWN"); break;
			}
			
			switch(mediaStatus.getIdleReason()) {
				case MediaStatus.IDLE_REASON_CANCELED:
					out.put("idleReason", "canceled"); break;
				case MediaStatus.IDLE_REASON_ERROR:
					out.put("idleReason", "error"); break;
				case MediaStatus.IDLE_REASON_FINISHED:
					out.put("idleReason", "finished"); break;
				case MediaStatus.IDLE_REASON_INTERRUPTED:
					out.put("idleReason", "iterrupted"); break;
				case MediaStatus.IDLE_REASON_NONE:
					out.put("idleReason", "none"); break;
			}
			
			JSONObject volume = new JSONObject();
			volume.put("level", mediaStatus.getStreamVolume());
			volume.put("muted", mediaStatus.isMute());
			
			out.put("volume", volume);
			
			objInfo.put("duration", mediaInfo.getStreamDuration() / 1000.0);
			
			switch(mediaInfo.getStreamType()) {
				case MediaInfo.STREAM_TYPE_BUFFERED:
					objInfo.put("streamType", "buffered"); break;
				case MediaInfo.STREAM_TYPE_LIVE:
					objInfo.put("streamType", "live"); break;
				case MediaInfo.STREAM_TYPE_NONE:
					objInfo.put("streamType", "other"); break;
			}
			
		} catch(JSONException e) {
			
		}
		
		return out;
	}
	
	

	/* GoogleApiClient.ConnectionCallbacks implementation
	 * Called when we successfully connect to the API
	 * (non-Javadoc)
	 * @see com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks#onConnected(android.os.Bundle)
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		this.launchApplication();
		//		this.launchContext.success();
	}
	
	
	/* GoogleApiClient.ConnectionCallbacks implementation
	 * (non-Javadoc)
	 * @see com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks#onConnectionSuspended(android.os.Bundle)
	 */
	@Override
	public void onConnectionSuspended(int cause) {
		
	}
	
	/*
	 * GoogleApiClient.OnConnectionFailedListener implementation
	 * When Google API fails to connect.
	 * (non-Javadoc)
	 * @see com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener#onConnectionFailed(com.google.android.gms.common.ConnectionResult)
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		this.launchCallback.onError("channel_error");
	}
	
	/**
	 * Cast.Listener implementation
	 * When Chromecast application status changed
	 */
	@Override
	public void onApplicationStatusChanged() {
		this.onSessionUpdatedListener.onSessionUpdated(true, createSessionObject());
	}
	
	/**
	 * Cast.Listener implementation
	 * When the volume is changed on the Chromecast
	 */
	@Override
	public void onVolumeChanged() {
		this.onSessionUpdatedListener.onSessionUpdated(true, createSessionObject());
	}
	
	/**
	 * Cast.Listener implementation
	 * When the application is disconnected
	 */
	@Override
	public void onApplicationDisconnected(int errorCode) {
		this.onSessionUpdatedListener.onSessionUpdated(false, null);
	}


	@Override
	public void onMetadataUpdated() {
		// On Media metadata updated
		this.onMediaUpdatedListener.onMediaUpdated(this.createMediaObject());
	}


	@Override
	public void onStatusUpdated() {
		// On Media status updated()
		this.onMediaUpdatedListener.onMediaUpdated(this.createMediaObject());
	}
}
