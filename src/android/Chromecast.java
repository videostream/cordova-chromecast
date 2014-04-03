package acidhax.cordova.chromecast;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.android.gms.cast.*;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.Cast.Listener;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.cast.RemoteMediaPlayer.OnStatusUpdatedListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
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

public class Chromecast extends CordovaPlugin {
	
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private ChromecastMediaRouterCallback mMediaRouterCallback = new ChromecastMediaRouterCallback();
    
    private ChromecastSession currentSession;
    

    public void initialize(final CordovaInterface cordova, CordovaWebView webView) {
    	super.initialize(cordova, webView);
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
    
    /**
     * Execute function - Just echos - yay test
     * @param args
     * @param cbContext
     * @return
     * @throws JSONException
     */
    public boolean echo (JSONArray args, CallbackContext cbContext) throws JSONException {
    	cbContext.success(args.getString(0));
    	return true;
    }
    
    /**
     * Execute function - Begins populating available Chromecast devices for a given appId 
     * @param args
     * @param cbContext
     * @return
     */
    public boolean getDevices (JSONArray args, final CallbackContext cbContext) throws JSONException {
    	final Activity activity = cordova.getActivity();
        final Chromecast that = this;
        final String appId = args.getString(0);
        activity.runOnUiThread(new Runnable() {
        	public void run() {
        		mMediaRouter = MediaRouter.getInstance(activity.getApplicationContext());
        		mMediaRouteSelector = new MediaRouteSelector.Builder()
        		.addControlCategory(CastMediaControlIntent.categoryForCast(appId))
        		.build();
        		mMediaRouterCallback.registerCallbacks(that);
        		mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
        		cbContext.success();
        	}
        });
        return true;
    }
    
    /**
     * Execute function - Returns more information on a specific Chromecast device
     * TODO: Make it work
     * @param args
     * @param cbContext
     * @throws JSONException
     */
    public void getRoute(JSONArray args, CallbackContext cbContext) throws JSONException {
    	int index = args.getInt(0);
    	RouteInfo info = mMediaRouterCallback.getRoute(index);
    	cbContext.success(info.getName());
    }
    
    /**
     * Execute function - Launches a specific AppId on a Chromecast device
     * @param args
     * @param cbContext
     * @return
     * @throws JSONException
     */
    public boolean launch (JSONArray args, CallbackContext callbackContext) throws JSONException {
    	String id = args.getString(0);
    	String appId = args.getString(1);
    	RouteInfo info = mMediaRouterCallback.getRoute(id);
    	
    	if (this.currentSession == null) {
    		if (info != null) {
    			this.currentSession = new ChromecastSession(info, this.cordova);
    			
    			// Launch the app.
    			// TODO: Create a callback interface so we don't have to throw around the CallbackContext
    			this.currentSession.launch(appId, callbackContext);
    		} else {
    			callbackContext.error("No route found by that ID");
    		}
    	} else {
    		callbackContext.error("Already have a session, disconnect first");
    	}
    	
    	return true;
    }
    
    
    /**
     * Execute function - Loads a URL in an app that supports the Chromecast MediaReceiver
     * @param args
     * @param cbContext
     * @return
     * @throws JSONException
     */
    public boolean loadUrl(JSONArray args, final CallbackContext callbackContext) throws JSONException {
    	String url = args.getString(0);
    	return this.currentSession.loadUrl(url, callbackContext);
    }
    
    
    public boolean play(JSONArray args, CallbackContext callbackContext) {
    	currentSession.play(callbackContext);
    	return true;
    }
    
    public boolean pause(JSONArray args, CallbackContext callbackContext) {
    	currentSession.pause(callbackContext);
    	return true;
    }
    
    public boolean stop(JSONArray args, CallbackContext callbackContext) {
    	currentSession.stop(callbackContext);
    	return true;
    }
    
    public boolean seek(JSONArray args, CallbackContext callbackContext) throws JSONException {
    	long seekTime = args.getLong(0);
    	currentSession.seek(seekTime, callbackContext);
    	return true;
    }
    
    public boolean setVolume(JSONArray args, CallbackContext callbackContext) throws JSONException {
    	double volume = args.getDouble(0);
    	currentSession.setVolume(volume, callbackContext);
    	return true;
    }
    
	
	protected void onRouteAdded(MediaRouter router, RouteInfo route) {
		this.webView.sendJavascript("chromecast.emit('device', '"+route.getId()+"', '" + route.getName() + "')");
	}

	protected void onRouteRemoved(MediaRouter router, RouteInfo route) {
		this.webView.sendJavascript("chromecast.emit('deviceRemoved', '"+route.getId()+"', '" + route.getName() + "')");
	}

	protected void onRouteSelected(MediaRouter router, RouteInfo route) {
		this.webView.sendJavascript("chromecast.emit('routeSelected', '"+route.getId()+"', '" + route.getName() + "')");
	}

	protected void onRouteUnselected(MediaRouter router, RouteInfo route) {
		this.webView.sendJavascript("chromecast.emit('routeUnselected', '"+route.getId()+"', '" + route.getName() + "')");
	}
}

