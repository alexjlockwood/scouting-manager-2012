package com.cmu.scout.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.cmu.scout.R;

public class TeamListActivity extends FragmentActivity implements
		TeamListFragment.OnTeamSelectedListener {
	
	private static final String TAG = "TeamListActivity";
	private static boolean DEBUG = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.team_list_fragment);
	
		if (DEBUG) {
			FragmentManager.enableDebugLogging(true);
			Log.v(TAG, "---ON CREATE---");
		}
	}
	
	@Override
	public void onTeamSelected(Uri uri) {
		if (DEBUG) Log.v(TAG, "-ON TEAM SELECTED-");

		TeamDetailsFragment details = 
				(TeamDetailsFragment) getSupportFragmentManager()
				.findFragmentById(R.id.team_details_fragment);
		
		if (details == null || !details.isInLayout()) {
			Intent showContent = new Intent(getApplicationContext(),
					TeamDetailsActivity.class);
			showContent.setData(uri);
			startActivity(showContent);
		} else {
			details.updateContent(uri);
		}
	}
}
