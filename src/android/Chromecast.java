package acidhax.cordova.chromecast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import com.google.android.gms.cast.*;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.widget.ArrayAdapter;

public class Chromecast extends CordovaPlugin implements ChromecastOnMediaUpdatedListener, ChromecastOnSessionUpdatedListener {
	
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private volatile ChromecastMediaRouterCallback mMediaRouterCallback = new ChromecastMediaRouterCallback();
    private String appId;
    
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
    				Type[] types = method.getGenericParameterTypes();
    				if (args.length() + 1 == types.length) { // +1 is the cbContext
    					boolean isValid = true;
        				for (int i = 0; i < args.length(); i++) {
            				Class arg = args.get(i).getClass();
            				if (types[i] == arg) {
            					isValid = true;
            				} else {
            					isValid = false;
            					break;
            				}
        				}
        				if (isValid) {
            				methodToExecute = method;
            				break;
        				}
    				}
    			}
    		}
    		if (methodToExecute != null) {
    			Type[] types = methodToExecute.getGenericParameterTypes();
    			Object[] variableArgs = new Object[types.length];
    			for (int i = 0; i < args.length(); i++) {
    				variableArgs[i] = args.get(i);
    			}
    			variableArgs[variableArgs.length-1] = cbContext;
        		Class<?> r = methodToExecute.getReturnType();
        		if (r == boolean.class) {
            		return (Boolean) methodToExecute.invoke(this, variableArgs);
        		} else {
        			methodToExecute.invoke(this, variableArgs);
        			return true;
        		}
    		} else {
    			return false;
    		}
		} catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
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
    public boolean echo (String echo, CallbackContext cbContext) throws JSONException {
    	cbContext.success(echo);
    	return true;
    }
    
    /**
     * Execute function - Begins populating available Chromecast devices for a given appId 
     * @param args
     * @param cbContext
     * @return
     */
    public boolean getDevices (final String appId, final CallbackContext cbContext) throws JSONException {
    	final Activity activity = cordova.getActivity();
        final Chromecast that = this;
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
        for (RouteInfo route : mMediaRouterCallback.getRoutes()) {
            that.webView.sendJavascript("chromecast.emit('device', '"+route.getId()+"', '" + route.getName() + "')");
        }
        return true;
    }
    
    /**
     * Execute function - Returns more information on a specific Chromecast device
     * TODO: Make it work
     * @param args
     * @param cbContext
     * @throws JSONException
     */
    public boolean getRoute(final String index, final CallbackContext cbContext) throws JSONException {
    	final Activity activity = cordova.getActivity();

        activity.runOnUiThread(new Runnable() {
        	public void run() {
            	RouteInfo info = mMediaRouterCallback.getRoute(index);
                if (info != null) {
                    cbContext.success(info.getName());    
                } else {
                    cbContext.error("No route found in " + mMediaRouterCallback.getRoutes().size() + " route(s)");
                }
        	}
        });
    	
        return true;
    }
    
    /**
     * Execute function - Launches a specific AppId on a Chromecast device
     * @param args
     * @param cbContext
     * @return
     * @throws JSONException
     */
    public boolean launch (final String deviceId, final String appId, final CallbackContext callbackContext) throws JSONException {
    	final Activity activity = cordova.getActivity();
        
        activity.runOnUiThread(new Runnable() {
            public void run() {
                RouteInfo info = mMediaRouterCallback.getRoute(deviceId);
        
                if (Chromecast.this.currentSession == null) {
                    if (info != null) {
                        Chromecast.this.currentSession = new ChromecastSession(info, Chromecast.this.cordova, Chromecast.this, Chromecast.this);
                        
                        // Launch the app.
                        // TODO: Create a callback interface so we don't have to throw around the CallbackContext
                        Chromecast.this.currentSession.launch(appId, genericCallback(callbackContext));
                    } else {
                        callbackContext.error("No route found by that ID");
                    }
                } else {
                    callbackContext.error("Already have a session, disconnect first");
                }
            }
        });
                
        return true;
    }
    
    /**
     * Execute function - Loads a URL in an app that supports the Chromecast MediaReceiver
     * @param args
     * @param cbContext
     * @return
     * @throws JSONException
     */
    public boolean loadUrl(String url, final CallbackContext callbackContext) throws JSONException {
    	return this.currentSession.loadUrl(url, genericCallback(callbackContext));
    }
    


    /* NEW APIS **/
    /*   _   _                          _____ _____     
        | \ | |                   /\   |  __ \_   _|    
        |  \| | _____      __    /  \  | |__) || |  ___ 
        | . ` |/ _ \ \ /\ / /   / /\ \ |  ___/ | | / __|
        | |\  |  __/\ V  V /   / ____ \| |    _| |_\__ \
        |_| \_|\___| \_/\_/   /_/    \_\_|   |_____|___/
    */
   
   /**
    * Do everything you need to for "setup" - calling back sets the isAvailable and lets every function on the
    * javascript side actually do stuff.
    * @param  callbackContext
    */
    public boolean setup (CallbackContext callbackContext) {
        callbackContext.success();
        return true;
    }

    /**
     * Initialize all of the MediaRouter stuff with the AppId
     * For now, ignore the autoJoinPolicy and defaultActionPolicy; those will come later
     * @param  appId               The appId we're going to use for ALL session requests
     * @param  autoJoinPolicy      tab_and_origin_scoped | origin_scoped | page_scoped
     * @param  defaultActionPolicy create_session | cast_this_tab
     * @param  callbackContext
     */
    public boolean initialize (final String appId, String autoJoinPolicy, String defaultActionPolicy, final CallbackContext callbackContext) {
        final Activity activity = cordova.getActivity();
        final Chromecast that = this;
        this.appId = appId;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                mMediaRouter = MediaRouter.getInstance(activity.getApplicationContext());
                mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(appId))
                .build();
                mMediaRouterCallback.registerCallbacks(that);
                mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
                callbackContext.success();
            }
        });
        for (RouteInfo route : mMediaRouterCallback.getRoutes()) {
            that.webView.sendJavascript("chromecast.emit('device', '"+route.getId()+"', '" + route.getName() + "')");
        }
        return true;
    }

    /**
     * Request the session for the previously sent appId
     * THIS IS WHAT LAUNCHES THE CHROMECAST PICKER
     * @param  callbackContext
     */
    public boolean requestSession (final CallbackContext callbackContext) {
    	final Activity activity = cordova.getActivity();
        activity.runOnUiThread(new Runnable() {
            public void run() {
                mMediaRouter = MediaRouter.getInstance(activity.getApplicationContext());
                final List<RouteInfo> routeList = mMediaRouter.getRoutes();
                
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            	builder.setTitle("Choose a Chromecast");
            	CharSequence[] seq = new CharSequence[routeList.size() -1];
            	for (int n = 1; n < routeList.size(); n++) {
            		RouteInfo route = routeList.get(n);
            		if (!route.getName().equals("Phone")) {
            			seq[n-1] = route.getName();
            		}
            	}
            	
            	builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        callbackContext.error("cancel");
                    }
                });
            	builder.setItems(seq, new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        RouteInfo selectedRoute = routeList.get(which + 1);
				        Chromecast.this.createSession(selectedRoute, callbackContext);
				    }
                });
                builder.show();
            }
        });
        
        return true;
    }

	/**
	 * Helper for the creating of a session! The user-selected RouteInfo needs to be passed to a new ChromecastSession 
	 * @param routeInfo
	 * @param callbackContext
	 */
    private void createSession(RouteInfo routeInfo, final CallbackContext callbackContext) {
    	this.currentSession = new ChromecastSession(routeInfo, this.cordova, this, this);
        
        // Launch the app.
        this.currentSession.launch(this.appId, new ChromecastSessionCallback() {

			@Override
			void onSuccess(Object object) {
				if (object == null) {
					onError("unknown");
				} else {
					callbackContext.success((JSONObject) object);
				}
			}

			@Override
			void onError(String reason) {
				if (reason != null) {
					callbackContext.error(reason);
				} else {
					callbackContext.error("unknown");
				}
			}
        	
        });
    }
    
    /**
     * Set the volume level on the receiver - this is a Chromecast volume, not a Media volume
     * @param  newLevel
     */
    public boolean setReceiverVolumeLevel (double newLevel, CallbackContext callbackContext) {
        callbackContext.error("not_implemented");
        return true;
    }

    /**
     * Sets the muted boolean on the receiver - this is a Chromecast mute, not a Media mute
     * @param  muted           
     * @param  callbackContext 
     */
    public boolean setReceiverMuted (boolean muted, CallbackContext callbackContext) {
        callbackContext.error("not_implemented");
        return true;
    }

    /**
     * Stop the session! Disconnect! All of that jazz!
     * @param  callbackContext [description]
     */
    public boolean stopSession(CallbackContext callbackContext) {
        callbackContext.error("not_implemented");
        return true;
    }

    /**
     * Send a custom message to the receiver - we don't need this just yet... it was just simple to implement on the js side
     * @param  namespace       
     * @param  message         
     * @param  callbackContext
     */
    public boolean sendMessage (String namespace, String message, CallbackContext callbackContext) {
        callbackContext.error("not_implemented");
        return true;
    }

    /**
     * Paramaters galore! Ignore most of these - we really just need the contentId (the URL of the media) for now
     * @param  contentId               The URL of the media item
     * @param  contentType             MIME type of the content
     * @param  duration                Duration of the content
     * @param  streamType              buffered | live | other
     * @param  loadRequest.autoPlay    Whether or not to automatically start playing the media
     * @param  loadReuqest.currentTime Where to begin playing from
     * @param  callbackContext 
     */
    public boolean loadMedia (String contentId, String contentType, Integer duration, String streamType, Boolean autoPlay, Integer currentTime, final CallbackContext callbackContext) {
        
    	if (this.currentSession != null) {
    		return this.currentSession.loadMedia(contentId, contentType, duration, streamType, autoPlay, currentTime, 
    				new ChromecastSessionCallback() {

						@Override
						void onSuccess(Object object) {
							if (object == null) {
								onError("unknown");
							} else {
								callbackContext.success((JSONObject) object);
							}
						}

						@Override
						void onError(String reason) {
							callbackContext.error(reason);
						}
    			
    		});
    	} else {
    		callbackContext.error("session_error");
    		return false;
    	}
    }
    
    /**
     * Play on the current media in the current session
     * @param callbackContext
     * @return
     */
    public boolean mediaPlay(CallbackContext callbackContext) {
    	currentSession.mediaPlay(genericCallback(callbackContext));
    	return true;
    }
    
    /**
     * Pause on the current media in the current session
     * @param callbackContext
     * @return
     */
    public boolean mediaPause(CallbackContext callbackContext) {
    	currentSession.mediaPause(genericCallback(callbackContext));
    	return true;
    }
    
    
    /**
     * Seeks the current media in the current session
     * @param seekTime
     * @param resumeState
     * @param callbackContext
     * @return
     */
    public boolean mediaSeek(Integer seekTime, String resumeState, CallbackContext callbackContext) {
    	currentSession.mediaSeek(seekTime.longValue(), resumeState, genericCallback(callbackContext));
    	return true;
    }
    
    
    /**
     * Set the volume on the media
     * @param level
     * @param callbackContext
     * @return
     */
    public boolean setMediaVolume(Double level, CallbackContext callbackContext) {
    	currentSession.mediaSetVolume(level, genericCallback(callbackContext));
    	
    	return true;
    }
    
    /**
     * Set the muted on the media
     * @param muted
     * @param callbackContext
     * @return
     */
    public boolean setMediaMuted(Boolean muted, CallbackContext callbackContext) {
    	currentSession.mediaSetMuted(muted, genericCallback(callbackContext));
    	
    	return true;
    }
    
    /**
     * Stops the current media!
     * @param callbackContext
     * @return
     */
    public boolean mediaStop(CallbackContext callbackContext) {
    	currentSession.mediaStop(genericCallback(callbackContext));
    	
    	return true;
    }
    
    /**
     * Stops the session
     * @param callbackContext
     * @return
     */
    public boolean sessionStop (CallbackContext callbackContext) {
    	if (this.currentSession != null) {
    		this.currentSession.kill(genericCallback(callbackContext));
    		this.currentSession = null;
    	} else {
    		callbackContext.success();
    	}
    	
    	return true;
    }

    
    private void checkReceiverAvailable() {
    	final Activity activity = cordova.getActivity();
        activity.runOnUiThread(new Runnable() {
            public void run() {
                mMediaRouter = MediaRouter.getInstance(activity.getApplicationContext());
                List<RouteInfo> routeList = mMediaRouter.getRoutes();
                
                if (routeList.size() > 0) {
                	Chromecast.this.webView.sendJavascript("chrome.cast._.receiverAvailable()");
                } else {
                	Chromecast.this.webView.sendJavascript("chrome.cast._.receiverUnavailable()");
                }
            }
        });
    }
    
    private ChromecastSessionCallback genericCallback (final CallbackContext callbackContext) {
    	return new ChromecastSessionCallback() {

			@Override
			public void onSuccess(Object object) {
				callbackContext.success();
			}

			@Override
			public void onError(String reason) {
				callbackContext.error(reason);
			}
    		
    	};
    };

    protected void onRouteAdded(MediaRouter router, final RouteInfo route) {
       this.checkReceiverAvailable();
    }

	protected void onRouteRemoved(MediaRouter router, RouteInfo route) {
		this.checkReceiverAvailable();
	}

	protected void onRouteSelected(MediaRouter router, RouteInfo route) {
//		this.webView.sendJavascript("chromecast.emit('routeSelected', '"+route.getId()+"', '" + route.getName() + "')");
	}

	protected void onRouteUnselected(MediaRouter router, RouteInfo route) {
//		this.webView.sendJavascript("chromecast.emit('routeUnselected', '"+route.getId()+"', '" + route.getName() + "')");
	}

	@Override
	public void onMediaUpdated() {
		this.webView.sendJavascript("chrome.cast._.mediaUpdated();");
	}

	@Override
	public void onSessionUpdated() {
		this.webView.sendJavascript("chrome.cast._.sessionUpdated();");
	}
}

