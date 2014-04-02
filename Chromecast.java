package com.your.company.HelloWorld;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.android.gms.cast.*;
import com.google.android.gms.cast.Cast.Listener;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter.RouteInfo;

public class Chromecast extends CordovaPlugin implements Cast.MessageReceivedCallback, 
		GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private String AppID = "2A03915F";

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private ChromecastMediaRouterCallback mMediaRouterCallback = new ChromecastMediaRouterCallback();
    private CastDevice mSelectedDevice = null;

	private RemoteMediaPlayer mRemoteMediaPlayer = new RemoteMediaPlayer();
	private GoogleApiClient mApiClient = null;	
	private Listener mCastClientListener;
	
	CallbackContext launchCallback = null;

    public void initialize(final CordovaInterface cordova, CordovaWebView webView) {
    	super.initialize(cordova, webView);
    	webView.sendJavascript("");
        mCastClientListener = new Cast.Listener() {
        	public void onApplicationStatusChanged() {onChromecastStatusChanged();}
        	public void onVolumeChanged() {onChromecastVolumeChanged();}
        	public void onApplicationDisconnected(int errorCode) {onChromecastDisconnected(errorCode);}
        };
    }
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext cbContext) throws JSONException {
    	try {
    		Method[] list = this.getClass().getMethods();
    		Method methodToExecute = null;
    		for (Method method : list) {
    			if (method.getName().equals(action)) {
    				methodToExecute = method;
    				break;
    			}
    		}
    		if (methodToExecute != null) {
        		Class<?> r = methodToExecute.getReturnType();
        		if (r == boolean.class) {
            		return (boolean) methodToExecute.invoke(this, args, cbContext);
        		} else {
        			methodToExecute.invoke(this, args, cbContext);
        			return true;
        		}
    		} else {
    			return false;
    		}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}
    }
    
    public boolean echo (JSONArray args, CallbackContext cbContext) throws JSONException {
    	cbContext.success(args.getString(0));
    	return true;
    }
    
    public boolean getDevices (JSONArray args, final CallbackContext cbContext) {
    	final Activity activity = cordova.getActivity();
        final Chromecast that = this;
        activity.runOnUiThread(new Runnable() {
        	public void run() {
        		mMediaRouter = MediaRouter.getInstance(activity.getApplicationContext());
        		mMediaRouteSelector = new MediaRouteSelector.Builder()
        		.addControlCategory(CastMediaControlIntent.categoryForCast(AppID))
        		.build();
        		mMediaRouterCallback.registerCallbacks(that);
        		mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
        		cbContext.success();
        	}
        });
        return true;
    }
    
    public void getRoute(JSONArray args, CallbackContext cbContext) throws JSONException {
    	int index = args.getInt(0);
    	RouteInfo info = mMediaRouterCallback.getRoute(index);
    	cbContext.success(info.getName());
    }
    
    public boolean launch (JSONArray args, CallbackContext cbContext) throws JSONException {
    	int index = args.getInt(0);
    	this.launchCallback = cbContext;
    	RouteInfo info = mMediaRouterCallback.getRoute(index);
    	mSelectedDevice = CastDevice.getFromBundle(info.getExtras());
//    	String routeId = info.getId();
    	
    	Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
                .builder(mSelectedDevice, mCastClientListener);
    	final Activity activity = cordova.getActivity();
		mApiClient = new GoogleApiClient.Builder(activity.getApplicationContext())
                        .addApi(Cast.API, apiOptionsBuilder.build())
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
		mApiClient.connect();

		return true;
    }
    
    public boolean loadUrl (JSONArray args, CallbackContext cbContext) {
    	MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
    	mediaMetadata.putString(MediaMetadata.KEY_TITLE, "My video");
    	MediaInfo mediaInfo = new MediaInfo.Builder(
    	    "http://192.168.1.104:5556/")
    	    .setContentType("video/mp4")
    	    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
    	    .setMetadata(mediaMetadata)
    	              .build();
    	try {
			mRemoteMediaPlayer.load(mApiClient, mediaInfo, true)
			.setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
			    @Override
			    public void onResult(MediaChannelResult result) {
					if (result.getStatus().isSuccess()) {
						System.out.println("Media loaded successfully");
					}
			    }
			});
    	} catch (IllegalStateException e) {
    		e.printStackTrace();
    		System.out.println("Problem occurred with media during loading");
    	} catch (Exception e) {
    		e.printStackTrace();
    		System.out.println("Problem opening media during loading");
    	}
    	return true;
    }
	
    /*
     * Chromecast asynchronous callbacks
     */
    
	public void onChromecastStatusChanged() {
		if (mApiClient != null) {
			try {
				System.out.println("onApplicationStatusChanged: " + Cast.CastApi.getApplicationStatus(mApiClient));
				this.webView.sendJavascript("Chromecast.emit('applicationStatusChanged', '"+Cast.CastApi.getApplicationStatus(mApiClient)+"')");
			} catch (Exception ex) {
				
			}
		}
	}
	
	public void onChromecastVolumeChanged() {
	    if (mApiClient != null) {
	    	try {
	    		System.out.println("onVolumeChanged: " + Cast.CastApi.getVolume(mApiClient));
	    		this.webView.sendJavascript("Chromecast.emit('volumeChanged', '"+Cast.CastApi.getVolume(mApiClient)+"')");
	    	} catch (Exception ex) {
	    		
	    	}
	    }
	}
	
	public void onChromecastDisconnected(int errorCode) {
		  System.out.println("app disconnected");
	}

	@Override
	public void onMessageReceived(CastDevice castDevice, String namespace,
			String message) {
		System.out.println("Message Received: " + castDevice.getFriendlyName() + " " + message);
	}
	
	public String getNamespace() {
		return "urn:x-cast:com.example.custom";
	}

	boolean mWaitingForReconnect = false;
	@Override
	public void onConnected(Bundle connectionHint) {
		final CallbackContext cbContext = this.launchCallback;
		if (mWaitingForReconnect) {
			mWaitingForReconnect = false;
		} else {
			try {
				Cast.CastApi.launchApplication(mApiClient, AppID, false)
				.setResultCallback(
						new ResultCallback<Cast.ApplicationConnectionResult>() {
							@Override
							public void onResult(Cast.ApplicationConnectionResult result) {
								Status status = result.getStatus();
								if (status.isSuccess()) {
//									ApplicationMetadata applicationMetadata = result.getApplicationMetadata();
//									String sessionId = result.getSessionId();
//									String applicationStatus = result.getApplicationStatus();
//									boolean wasLaunched = result.getWasLaunched();
									if (cbContext != null && !cbContext.isFinished()) {
										cbContext.success("launchSuccess");
									}
								} else {
									if (cbContext != null && !cbContext.isFinished()) {
										cbContext.equals("launchFailed");
									}
								}
							}
						});
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void onConnectionSuspended(int cause) {
		System.out.println("Suspended");
		if (!this.launchCallback.isFinished()) {
			this.launchCallback.error("launchFailed");
		} else {
			this.webView.sendJavascript("Chromecast.emit('launchFailed')");
		}
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		System.out.println("Fail");
		if (!this.launchCallback.isFinished()) {
			this.launchCallback.error("launchFailed");
		} else {
			this.webView.sendJavascript("Chromecast.emit('launchFailed')");
		}
	}
	
	protected void onRouteAdded(MediaRouter router, RouteInfo route) {
		// Send to JS client.
		this.webView.sendJavascript("Chromecast.emit('device', '"+route.getId()+"', '" + route.getName() + "')");
	}

	protected void onRouteRemoved(MediaRouter router, RouteInfo route) {
		// Send to JS client.
		this.webView.sendJavascript("Chromecast.emit('deviceRemoved', '"+route.getId()+"', '" + route.getName() + "')");
	}

	protected void onRouteSelected(MediaRouter router, RouteInfo route) {
		// Send to JS client.
		this.webView.sendJavascript("Chromecast.emit('routeSelected', '"+route.getId()+"', '" + route.getName() + "')");
	}

	protected void onRouteUnselected(MediaRouter router, RouteInfo route) {
		// Send to JS client.
		this.webView.sendJavascript("Chromecast.emit('routeUnselected', '"+route.getId()+"', '" + route.getName() + "')");
	}
}

