package com.bangoexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class BangoAnalyticsExampleActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        AnalyticsWrapper.onCreate(this, savedInstanceState);
        
        setUpButton();
    }

    @Override
    protected void onPause() {
    	super.onPause();
    	
    	AnalyticsWrapper.onPause();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	AnalyticsWrapper.onStop(this);
    }
    
    private void setUpButton() {
		Button button = (Button) findViewById(R.id.open_activity_button);
        button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(BangoAnalyticsExampleActivity.this, SomeOtherActivity.class);
				startActivity(intent);
			}
		});
	}
}