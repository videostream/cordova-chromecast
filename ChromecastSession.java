package com.your.company.HelloWorld;

import java.io.IOException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.CastDevice;
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
public class ChromecastSession implements GoogleApiClient.ConnectionCallbacks, OnConnectionFailedListener {
	private String id = null;
	private String name = null;
	private String sessionId = null;
	private RouteInfo routeInfo = null;
	private GoogleApiClient mApiClient = null;	
	private RemoteMediaPlayer mRemoteMediaPlayer = new RemoteMediaPlayer();
	private CordovaInterface cordova = null;
	private CastDevice device = null;
	private ChromecastMediaController controller = new ChromecastMediaController(mRemoteMediaPlayer);
	private Listener mCastClientListener;
	private String AppID;
	
	public ChromecastSession(CordovaInterface cordovaInterface) {
		this.cordova = cordovaInterface;
		mCastClientListener = new Cast.Listener() {
        	public void onApplicationStatusChanged() {}
        	public void onVolumeChanged() {}
        	public void onApplicationDisconnected(int errorCode) {}
        };
	}
	
	public void setRouteInfo(RouteInfo routeInfo) {
		this.routeInfo = routeInfo;
		this.device = CastDevice.getFromBundle(this.routeInfo.getExtras());
	}
	
	public void launch(String AppID) {
		this.AppID = AppID;
		this.connectToDevice();
	}
	
	private void connectToDevice() {
		Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
                .builder(this.device, mCastClientListener);
		this.mApiClient = new GoogleApiClient.Builder(cordova.getActivity().getApplicationContext())
			.addApi(Cast.API, apiOptionsBuilder.build())
	        .addConnectionCallbacks(this)
	        .addOnConnectionFailedListener(this)
	        .build();
		
		this.mApiClient.connect();
	}
	
	private void launchApplication() {
		Cast.CastApi.launchApplication(mApiClient, this.AppID, false)
		.setResultCallback(launchApplicationResultCallback);
	}
	
	private void connectRemoteMediaPlayer() throws IllegalStateException, IOException {
		Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
		mRemoteMediaPlayer.requestStatus(mApiClient)
		.setResultCallback(connectRemoteMediaPlayerCallback);
	}
	
	private ResultCallback<Cast.ApplicationConnectionResult> launchApplicationResultCallback = new ResultCallback<Cast.ApplicationConnectionResult>() {
		@Override
		public void onResult(ApplicationConnectionResult result) {
			Status status = result.getStatus();
			if (status.isSuccess()) {
				
			} else {
				
			}
		}
		
	};
	
	private ResultCallback<RemoteMediaPlayer.MediaChannelResult> connectRemoteMediaPlayerCallback = new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
		@Override
		public void onResult(MediaChannelResult result) {
			if (!result.getStatus().isSuccess()) {
				System.out.println("Failed to request status.");
			}
		}
	};
	
	/*
	 * For Google API Client.
	 * (non-Javadoc)
	 * @see com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks#onConnected(android.os.Bundle)
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		this.launchApplication();
	}
	@Override
	public void onConnectionSuspended(int cause) {
		
	}
	/*
	 * When Google API fails to connect.
	 * (non-Javadoc)
	 * @see com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener#onConnectionFailed(com.google.android.gms.common.ConnectionResult)
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		
	}
}
