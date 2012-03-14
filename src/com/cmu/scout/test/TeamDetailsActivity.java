package com.cmu.scout.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.cmu.scout.R;

public class TeamDetailsActivity extends FragmentActivity {

	private static final String TAG = "TeamDetailsActivity";
	private static final boolean DEBUG = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.team_details_fragment);
		
		if (DEBUG) {
			FragmentManager.enableDebugLogging(true);
			Log.v(TAG, "---ON CREATE---");
		}
		
		final Intent launchingIntent = getIntent();
		Uri uri = launchingIntent.getData();
		
		// add the fragment to the layout
		TeamDetailsFragment details =
				(TeamDetailsFragment) getSupportFragmentManager()
				.findFragmentById(R.id.team_details_fragment);
		
		// update the layout with the new team's data
		details.updateContent(uri);		
	}
	
}
