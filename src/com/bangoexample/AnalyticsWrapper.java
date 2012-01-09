package com.bangoexample;

import java.util.Hashtable;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.bango.android.BangoAgent;

public class AnalyticsWrapper {
	private static String applicationID = "" /* Analytics ID goes here */;

	/**
	 * This method needs to be called in the onCreate method of the first activity the app boots up into.
	 * It will automatically decide whether the app needs to call onStartSession based on whether or not
	 * onStartSession has been called before.
	 * 
	 * If savedInstanceState == null, then we need to call onStartSession.
	 * 
	 * @param activity Was used in an earlier version. Is no longer used.
	 * @param savedInstanceState Used to determine if the activity was simply rotated, or if we are coming up for the first time.
	 */
	public static void onCreate(Activity activity, Bundle savedInstanceState) {
		final Activity localActivity = activity;

		if (savedInstanceState == null) {
			// A try/catch statement is used in an attempt to prevent a Bango error from crashing the app entirely.
			try {
				BangoAgent.onStartSession(localActivity.getApplicationContext(), applicationID);
			} catch (Exception e) {
			}
		}
	}

	/**
	 * This method is called in the onDestroy method of the activity the app boots up into.
	 * 
	 * @param activity The activity that the application boots up into. Used for the BangoAgent.onEndSession call.
	 */
	public static void onStop(Activity activity) {
		final Activity localActivity = activity;
		
		// Verify that the activity is in fact being destroyed (and therefore not simply rotating) when onEndSession is called.
		if (activity.isFinishing()) {
			try {
				// We use the application context because a Bango representative told us to do so in an earlier phone conversation.
				BangoAgent.onEndSession(localActivity.getApplicationContext());
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Fires off a Bango event into a new thread.
	 * 
	 * @param eventName
	 * @param eventDetail
	 * @param eventParameters This is the hashtable containing the details of the Bango event. If it is null, the async task will automatically create a new hash table.
	 */
	public static synchronized void addEvent(String eventName, String eventDetail, Hashtable<String, String> eventParameters) {
		try {
			BangoEventAsyncTask bangoEvent = new BangoEventAsyncTask(eventName, eventDetail, eventParameters);
			bangoEvent.execute();
		} catch (Exception e) {
		}
	}

	/**
	 * Unused.
	 */
	public static void onResume() {
		try {
			BangoAgent.onResume();
		} catch (Exception e) {
		}
	}

	/**
	 * Call Bango's onIdle method, and then post events to the server.
	 * 
	 * This is called in the onPause method of various activities.
	 * 
	 * We need to call postEvents because there is no guarantee that we will ever post events otherwise.
	 * It is possible for a used to continuously use this application without ever triggering an onStartSession
	 * or onEndSession event.
	 * 
	 * This call performs its tasks in another thread to prevent the UI thread from locking up.
	 */
	public static void onPause() {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... arg0) {
				try {
					BangoAgent.onIdle();
					BangoAgent.postEvents();
				} catch (Exception e) {
				}
				return null;
			}
		}.execute();
	}

	/**
	 * Unused.
	 * @param context
	 */
	public static void onStart(Context context) {
	}

	/**
	 * Controls the BangoAgent.onEvent calls that we are making.
	 * 
	 * Adds additional, universal analytics information to the event calls.
	 * 
	 * Kicks off the onEvent call in a new thread (and synchronized) in order to prevent several problems that we have encountered.
	 */
	private static class BangoEventAsyncTask extends AsyncTask<Void, Void, Void> {
		private String eventName;
		private String eventDetail;
		private Hashtable<String, String> eventParameters;

		public BangoEventAsyncTask(String eventName, String eventDetail, Hashtable<String, String> eventParameters) {
			this.eventName = eventName;
			this.eventDetail = eventDetail;
			if (eventParameters == null)
				this.eventParameters = new Hashtable<String, String>();
			else
				this.eventParameters = eventParameters;
		}

		@Override
		protected Void doInBackground(Void... params) {
			loadRegionToEventParameters(eventParameters, new RegionLoadingCallback() {
				@Override
				public void onRegionLoadComplete(Hashtable<String, String> eventParameters) {
					eventParameters.put("bgo_custom1", "some text");
					eventParameters.put("bgo_custom8", "some text");
					eventParameters.put("bgo_custom9", "some text");
					eventParameters.put("bgo_custom10", "some text");

					sendBangoEvent();
				}
			});
			return null;
		}

		private void sendBangoEvent() {
			try {
				BangoAgent.onEvent(eventName, "", eventDetail, eventParameters);
			} catch (Exception e) {
				// Note: Not sure if we ever get here. Bango exceptions appear to not be catchable.
			}
		}

		private void loadRegionToEventParameters(Hashtable<String, String> eventParameters, final RegionLoadingCallback callback) {
			if (eventParameters == null)
				eventParameters = new Hashtable<String, String>();

			final Hashtable<String, String> eventHashTable = eventParameters;
			String region;

			// In our code, we have the logic for the region here. I have removed this logic as it is not relevant for debugging Bango.
			region = "some text";
			
			eventHashTable.put("bgo_region", region);
			callback.onRegionLoadComplete(eventHashTable);
		}

		public interface RegionLoadingCallback {
			public void onRegionLoadComplete(Hashtable<String, String> eventParameters);
		}
	}
}