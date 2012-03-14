package com.cmu.scout.test;

import android.app.Activity;
import android.net.Uri;

import com.cmu.scout.ui.OnTeamSelectedListener;

public class DisplayTeamsActivity extends Activity implements OnTeamSelectedListener {
    
	// private static final String TAG = "DisplayTeamActivity";
	// private static final boolean DEBUG = true;
	
	public void onTeamSelected(Uri uri) {
		// TODO: implement this
	}
	
	// TODO: figure out how to handle View clicks in Fragments
	/*
	public void onHeaderClick(View v) {
		String sortOrder;
		
		switch (v.getId()) {
		case R.id.teamId:
			sort = DisplayGeneralFragment.SORT_TEAM_ID;
			break;
		case R.id.fridayRank:
			sort = DisplayGeneralFragment.SORT_FRIDAY_RANK;
			break;
		case R.id.speed:
			sort = DisplayGeneralFragment.SORT_SPEED;
			break;
		case R.id.agility:
			sort = DisplayGeneralFragment.SORT_AGILITY;
			break;
		case R.id.strategy:
			sort = DisplayGeneralFragment.SORT_STRATEGY;
			break;
		case R.id.pickUpBalls:
			sort = DisplayGeneralFragment.SORT_PICK_UP_BALLS;
			break;
		}
		
	}*/
	/*
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			Intent intent = new Intent(this, DashboardActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
   	     	return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.v(TAG, "+++ ON CREATE +++");

        final ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

        bar.addTab(bar.newTab()
                .setText("General")
                .setTabListener(new TabListener<DisplayTeamsGeneralFragment>(
                        this, "general", DisplayTeamsGeneralFragment.class)));
        
        bar.addTab(bar.newTab()
                .setText("Offense")
                .setTabListener(new TabListener<DisplayTeamsOffenseFragment>(
                        this, "offense", DisplayTeamsOffenseFragment.class)));      

        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (DEBUG) Log.v(TAG, "onSaveInstanceState()");

        outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
    }*/
}