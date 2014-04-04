package acidhax.cordova.chromecast;

import java.io.IOException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.cast.Cast.Listener;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.media.MediaRouter.RouteInfo;

/*
 * All of the Chromecast session specific functions should start here. 
 */
public class ChromecastSession extends Cast.Listener implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	private String id = null;
	private String name = null;
	private String sessionId = null;
	private RouteInfo routeInfo = null;
	private GoogleApiClient mApiClient = null;	
	private RemoteMediaPlayer mRemoteMediaPlayer = new RemoteMediaPlayer();
	private CordovaInterface cordova = null;
	private CastDevice device = null;
	private ChromecastMediaController chromecastMediaController = new ChromecastMediaController(mRemoteMediaPlayer);
	private String appId;
	
	private CallbackContext launchContext;
	
	public ChromecastSession(RouteInfo routeInfo, CordovaInterface cordovaInterface) {
		this.cordova = cordovaInterface;
        
        this.routeInfo = routeInfo;
		this.device = CastDevice.getFromBundle(this.routeInfo.getExtras());
	}

	
	/**
	 * Sets the wheels in motion - connects to the Chromecast and launches the given app
	 * @param appId
	 */
	public void launch(String appId, CallbackContext launchContext) {
		this.appId = appId;
		this.launchContext = launchContext;
		this.connectToDevice();
	}
	
	public void kill (final CallbackContext killContext) {
		this.mRemoteMediaPlayer.stop(mApiClient).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
			@Override
			public void onResult(MediaChannelResult result) {
				mApiClient.disconnect();
				killContext.success();
			}
		});
		Cast.CastApi.stopApplication(mApiClient);
	}
	
	public boolean loadUrl(String url, final CallbackContext loadUrlContext) {
		try {
			MediaInfo mediaInfo = chromecastMediaController.createLoadUrlRequest(url);
			
			mRemoteMediaPlayer.load(mApiClient, mediaInfo, true)
				.setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
					@Override
					public void onResult(MediaChannelResult result) {
						if (result.getStatus().isSuccess()) {
							System.out.println("Media loaded successfully");
							loadUrlContext.success();
						} else {
							loadUrlContext.error("Unable to load");
						}
				    }
				});
    	} catch (IllegalStateException e) {
    		e.printStackTrace();
    		System.out.println("Problem occurred with media during loading");
    		loadUrlContext.error("Problem occurred with media during loading");
    		return false;
    	} catch (Exception e) {
    		e.printStackTrace();
    		loadUrlContext.error("Problem opening media during loading");
    		System.out.println("Problem opening media during loading");
    		return false;
    	}
    	return true;
	}
	
	public void play(CallbackContext callbackContext) {
		chromecastMediaController.play(mApiClient, callbackContext);
	}
	
	public void pause(CallbackContext callbackContext) {
		chromecastMediaController.pause(mApiClient, callbackContext);
	}
	
	public void stop(CallbackContext callbackContext) {
		chromecastMediaController.stop(mApiClient, callbackContext);
	}
	
	public void seek(long seekPosition, CallbackContext callbackContext) {
		chromecastMediaController.seek(seekPosition, mApiClient, callbackContext);
	}
	
	public void setVolume(double volume, CallbackContext callbackContext) {
		chromecastMediaController.setVolume(volume, mApiClient, callbackContext);
	}
	
	public void setMute(boolean muted, CallbackContext callbackContext) {
		chromecastMediaController.setMute(muted, mApiClient, callbackContext);
	}
	
	private void connectToDevice() {
		Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
                .builder(this.device, this);
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
			Status status = result.getStatus();
			if (status.isSuccess()) {
				try {
					connectRemoteMediaPlayer();
				} catch (IllegalStateException | IOException e) {
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
	
	

	/* GoogleApiClient.ConnectionCallbacks implementation
	 * Called when we successfully connect to the API
	 * (non-Javadoc)
	 * @see com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks#onConnected(android.os.Bundle)
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		this.launchApplication();
		this.launchContext.success();
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
		this.launchContext.error("Connection failed to Chromecast");
	}
	
	/**
	 * Cast.Listener implementation
	 * When Chromecast application status changed
	 */
	@Override
	public void onApplicationStatusChanged() {
		
	}
	
	/**
	 * Cast.Listener implementation
	 * When the volume is changed on the Chromecast
	 */
	@Override
	public void onVolumeChanged() {
		
	}
	
	/**
	 * Cast.Listener implementation
	 * When the application is disconnected
	 */
	@Override
	public void onApplicationDisconnected(int errorCode) {
		
	}
}
