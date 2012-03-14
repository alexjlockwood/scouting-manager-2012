package com.cmu.scout.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.cmu.scout.R;

public class DashboardActivity extends Activity {
	/*
	private Button mTeamScout;
	private Button mMatchScout;
	private Button mDisplay;
	private Button mSendToDevice;
	private Button mManageData;
	*/
	
	// intent passed to team grid
	public static final String INTENT_CALL_FROM_TEAM = "call_from_team";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard_layout);
		/*
		final DashboardLayout root = (DashboardLayout) findViewById(R.id.DashboardContainer);
		
		mTeamScout = (Button) root.findViewById(R.id.dashboard_teams);
		mMatchScout = (Button) root.findViewById(R.id.dashboard_match);
		mDisplay = (Button) root.findViewById(R.id.dashboard_display);
		mSendToDevice = (Button) root.findViewById(R.id.dashboard_transfer);
		mManageData = (Button) root.findViewById(R.id.dashboard_manage);
		
		root.post(new Runnable() {
			@Override
			public void run() {
				TouchDelegate[] delegates = {
					getTouchDelegate(mTeamScout),
					getTouchDelegate(mMatchScout),
					getTouchDelegate(mDisplay),
					getTouchDelegate(mSendToDevice),
					getTouchDelegate(mManageData)
				};
				root.setMultipleTouchDelegates(delegates);
			}
		});
		*/
	}
	
	public void onClickHandler(View v) {
		switch (v.getId()) {
		case R.id.dashboard_teams:
			//modified by Roger
			Intent teamData = new Intent(getApplicationContext(), TeamGridActivity.class);
			teamData.putExtra(INTENT_CALL_FROM_TEAM, true);
			startActivity(teamData);
			break;
		case R.id.dashboard_match:
			//modified by Roger
			Intent matchData = new Intent(getApplicationContext(), TeamGridActivity.class);
			matchData.putExtra(INTENT_CALL_FROM_TEAM, false);
			startActivity(matchData);
			break;
		case R.id.dashboard_display:
			startActivity(new Intent(getApplicationContext(), DisplayPagerActivity.class));
			break;
		case R.id.dashboard_transfer:
			Toast.makeText(DashboardActivity.this, "Coming soon!", Toast.LENGTH_SHORT).show();
			break;
		case R.id.dashboard_manage:
			Toast.makeText(DashboardActivity.this, "Coming soon!", Toast.LENGTH_SHORT).show();
			break;
		}
	}
	/*
	private TouchDelegate getTouchDelegate(View v) {
		Rect bounds = new Rect();
		v.getHitRect(bounds);
		bounds.left -= 50;
		bounds.right += 50;
		
		return new TouchDelegate(bounds,v);
	}*/
}
