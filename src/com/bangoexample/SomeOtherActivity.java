package com.bangoexample;

import java.util.Hashtable;

import android.app.Activity;
import android.os.Bundle;

public class SomeOtherActivity extends Activity {
	protected void onCreate(Bundle savedInstanceState) {
		Hashtable<String, String> eventParams = new Hashtable<String, String>();
		eventParams.put("bgo_custom3", "some text");
		AnalyticsWrapper.addEvent("some name", "some detail", eventParams);
	}
}
