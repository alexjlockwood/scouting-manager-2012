package com.cmu.scout.ui;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;

import com.cmu.scout.R;
import com.cmu.scout.fragment.DisplayMatchesAutoFragment;
import com.cmu.scout.fragment.DisplayMatchesGeneralFragment;
import com.cmu.scout.fragment.DisplayMatchesTeleOpFragment;
import com.cmu.scout.provider.ScoutContract.Matches;
import com.cmu.scout.provider.ScoutContract.TeamMatches;
import com.cmu.scout.provider.ScoutContract.Teams;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;

public class DisplayPagerMatchesActivity extends FragmentActivity {
	
	private static final String TAG = "DisplayPagerMatchesActivity";
	private static final boolean DEBUG = true;
	
	private static final String TEAM_ID_STORAGE_KEY = "CurrentTeamId";
	private static final String TEAM_NUM_STORAGE_KEY = "CurrentTeamNum";
		
	private DisplayFragmentAdapter mAdapter;
	private ViewPager mPager;
	private PageIndicator mIndicator;
	
	private int mTeamId = -1;
	private int mTeamNum = -1;
	
	private static Uri mMatchesUri;
	
	private static final String[] PROJECTION = {
		TeamMatches.AUTO_NUM_ATTEMPT_HIGH,
		TeamMatches.AUTO_NUM_ATTEMPT_MED,
		TeamMatches.AUTO_NUM_ATTEMPT_LOW,
		TeamMatches.AUTO_NUM_SCORED_HIGH,
		TeamMatches.AUTO_NUM_SCORED_MED,
		TeamMatches.AUTO_NUM_SCORED_LOW,
		
		TeamMatches.NUM_ATTEMPT_HIGH,
		TeamMatches.NUM_ATTEMPT_MED,
		TeamMatches.NUM_ATTEMPT_LOW,
		TeamMatches.NUM_SCORED_HIGH,
		TeamMatches.NUM_SCORED_MED,
		TeamMatches.NUM_SCORED_LOW,
		
		TeamMatches.WIN_MATCH,
		TeamMatches.FINAL_SCORE
	};
	
	private static final String[] PROJECTION_SUMMARY = {
		Teams.SUMMARY_AUTO_NUM_ATTEMPT,
		Teams.SUMMARY_AUTO_NUM_POINTS,
		Teams.SUMMARY_AUTO_NUM_SCORED,
		
		Teams.SUMMARY_NUM_ATTEMPT,
		Teams.SUMMARY_NUM_POINTS,
		Teams.SUMMARY_NUM_SCORED,
		
		Teams.SUMMARY_NUM_WINS,
		Teams.SUMMARY_NUM_LOSSES,
		Teams.SUMMARY_TOTAL_SCORE
	};
	
	public void onTeamDeleted(long teamMatchesId) {
		final Uri teamUri = Teams.buildTeamIdUri(""+mTeamId);
		final Uri teamMatchesUri = Matches.buildMatchTeamIdUri(""+mTeamId);
		
		final Cursor teamCur = getContentResolver().query(teamUri, PROJECTION_SUMMARY, null, null, null);
		final Cursor teamMatchesCur = getContentResolver().query(teamMatchesUri, PROJECTION, TeamMatches._ID + " = " + teamMatchesId, null, null);
		
		if (teamCur != null && teamCur.moveToFirst()) {
			if (teamMatchesCur != null && teamMatchesCur.moveToFirst()) {
				// get match info
				int autoNumAttemptHigh = teamMatchesCur.getInt(teamMatchesCur.getColumnIndex(TeamMatches.AUTO_NUM_ATTEMPT_HIGH));
				int autoNumAttemptMed = teamMatchesCur.getInt(teamMatchesCur.getColumnIndex(TeamMatches.AUTO_NUM_ATTEMPT_MED));
				int autoNumAttemptLow = teamMatchesCur.getInt(teamMatchesCur.getColumnIndex(TeamMatches.AUTO_NUM_ATTEMPT_LOW));
				int autoNumScoredHigh = teamMatchesCur.getInt(teamMatchesCur.getColumnIndex(TeamMatches.AUTO_NUM_SCORED_HIGH));
				int autoNumScoredMed = teamMatchesCur.getInt(teamMatchesCur.getColumnIndex(TeamMatches.AUTO_NUM_SCORED_MED));
				int autoNumScoredLow = teamMatchesCur.getInt(teamMatchesCur.getColumnIndex(TeamMatches.AUTO_NUM_SCORED_LOW));
				
				int numAttemptHigh = teamMatchesCur.getInt(teamMatchesCur.getColumnIndex(TeamMatches.NUM_ATTEMPT_HIGH));
				int numAttemptMed = teamMatchesCur.getInt(teamMatchesCur.getColumnIndex(TeamMatches.NUM_ATTEMPT_MED));
				int numAttemptLow = teamMatchesCur.getInt(teamMatchesCur.getColumnIndex(TeamMatches.NUM_ATTEMPT_LOW));
				int numScoredHigh = teamMatchesCur.getInt(teamMatchesCur.getColumnIndex(TeamMatches.NUM_SCORED_HIGH));
				int numScoredMed = teamMatchesCur.getInt(teamMatchesCur.getColumnIndex(TeamMatches.NUM_SCORED_MED));
				int numScoredLow = teamMatchesCur.getInt(teamMatchesCur.getColumnIndex(TeamMatches.NUM_SCORED_LOW));
				
				int winMatch = teamMatchesCur.getInt(teamMatchesCur.getColumnIndex(TeamMatches.WIN_MATCH));
				int finalScore = teamMatchesCur.getInt(teamMatchesCur.getColumnIndex(TeamMatches.FINAL_SCORE));
				
				// get summary info
				int summaryAutoNumAttempt = teamCur.getInt(teamCur.getColumnIndex(Teams.SUMMARY_AUTO_NUM_ATTEMPT));
				int summaryAutoNumScored = teamCur.getInt(teamCur.getColumnIndex(Teams.SUMMARY_AUTO_NUM_SCORED));
				int summaryAutoNumPoints = teamCur.getInt(teamCur.getColumnIndex(Teams.SUMMARY_AUTO_NUM_POINTS));

				int summaryNumAttempt = teamCur.getInt(teamCur.getColumnIndex(Teams.SUMMARY_NUM_ATTEMPT));
				int summaryNumScored = teamCur.getInt(teamCur.getColumnIndex(Teams.SUMMARY_NUM_SCORED));
				int summaryNumPoints = teamCur.getInt(teamCur.getColumnIndex(Teams.SUMMARY_NUM_POINTS));
				
				int summaryNumWins = teamCur.getInt(teamCur.getColumnIndex(Teams.SUMMARY_NUM_WINS));
				int summaryNumLosses = teamCur.getInt(teamCur.getColumnIndex(Teams.SUMMARY_NUM_LOSSES));
				int summaryTotalScore = teamCur.getInt(teamCur.getColumnIndex(Teams.SUMMARY_TOTAL_SCORE));
				
				// compute updated summary info and add  to ContentValues
				ContentValues values = new ContentValues();
				
				values.put(Teams.SUMMARY_AUTO_NUM_ATTEMPT, summaryAutoNumAttempt - (autoNumAttemptHigh + autoNumAttemptMed + autoNumAttemptLow));
				values.put(Teams.SUMMARY_AUTO_NUM_SCORED, summaryAutoNumScored - (autoNumScoredHigh + autoNumScoredMed + autoNumScoredLow));
				values.put(Teams.SUMMARY_AUTO_NUM_POINTS, summaryAutoNumPoints - (3*autoNumScoredHigh + 2*autoNumScoredMed + 1*autoNumScoredLow));
				
				values.put(Teams.SUMMARY_NUM_ATTEMPT, summaryNumAttempt - (numAttemptHigh + numAttemptMed + numAttemptLow));
				values.put(Teams.SUMMARY_NUM_SCORED, summaryNumScored - (numScoredHigh + numScoredMed + numScoredLow));
				values.put(Teams.SUMMARY_NUM_POINTS, summaryNumPoints - (3*numScoredHigh + 2*numScoredMed + 1*numScoredLow));
				
				values.put(Teams.SUMMARY_NUM_WINS, summaryNumWins - ((winMatch == 1) ? 1 : 0));
				values.put(Teams.SUMMARY_NUM_LOSSES, summaryNumLosses - ((winMatch == 0) ? 1 : 0));
				values.put(Teams.SUMMARY_TOTAL_SCORE, summaryTotalScore - finalScore);
				
				// update the database
				getContentResolver().delete(teamMatchesUri, TeamMatches._ID + " = " + teamMatchesId, null);
				getContentResolver().update(teamUri, values, null, null);				
			}
		}
	}	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(TEAM_ID_STORAGE_KEY, mTeamId);
		outState.putInt(TEAM_NUM_STORAGE_KEY, mTeamNum);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mTeamId = savedInstanceState.getInt(TEAM_ID_STORAGE_KEY);
		mTeamNum = savedInstanceState.getInt(TEAM_NUM_STORAGE_KEY);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.v(TAG, "+++ ON CREATE +++");
		setContentView(R.layout.display_teams_pager);
		
		if (DEBUG) {
			FragmentManager.enableDebugLogging(true);
		}
		
		mMatchesUri = getIntent().getData();
		
		mTeamId = Integer.valueOf(mMatchesUri.getLastPathSegment());
		
		final Uri teamUri = Teams.buildTeamIdUri(""+mTeamId);
		final Cursor teamNumCur = getContentResolver().query(teamUri, new String[] { Teams.TEAM_NUM }, null, null, null);
		if (teamNumCur != null && teamNumCur.moveToFirst()) {
			mTeamNum = teamNumCur.getInt(teamNumCur.getColumnIndex(Teams.TEAM_NUM));
		}
		
		final ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setTitle(R.string.display_team_matches_title);
			actionBar.setSubtitle("Team " + mTeamNum);
		}
		
		mAdapter = new DisplayFragmentAdapter(getSupportFragmentManager());

		mPager = (ViewPager)findViewById(R.id.display_pager);
		mPager.setAdapter(mAdapter);

		mIndicator = (TabPageIndicator)findViewById(R.id.display_indicator);
		mIndicator.setViewPager(mPager);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			// go to home screen when app icon in action bar is clicked
			Intent intent = new Intent(this, DashboardActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        	startActivity(intent);
        	return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public static class DisplayFragmentAdapter extends FragmentPagerAdapter 
			implements TitleProvider {
		
		private static final String TAG = "DisplayFragmentAdapter";
		private static final boolean DEBUG = false;
		
		public static final int POSITION_AUTO = 0;
		public static final int POSITION_TELEOP = 1;
		public static final int POSITION_GENERAL = 2;
		
		private static final String[] TITLES = new String[] { 
			"Autonomous",
			"Teleop",
			"General"
		};

		private int mCount = TITLES.length;

		public DisplayFragmentAdapter(FragmentManager fm) {		
			super(fm);
			if (DEBUG) Log.v(TAG, "DisplayFragmentAdapter()");
		}

		@Override
		public Fragment getItem(int position) {
			if (DEBUG) Log.v(TAG, "getItem()");
			switch (position) {
			case POSITION_AUTO: return DisplayMatchesAutoFragment.newInstance(mMatchesUri);
			case POSITION_TELEOP: return DisplayMatchesTeleOpFragment.newInstance(mMatchesUri);
			case POSITION_GENERAL: return DisplayMatchesGeneralFragment.newInstance(mMatchesUri);
			}
			return null;
		}

		@Override
		public int getCount() {
			if (DEBUG) Log.v(TAG, "getCount()");
			return mCount;
		}
		
		@Override
		public String getTitle(int position) {
			if (DEBUG) Log.v(TAG, "getTitle()");
			return TITLES[position % TITLES.length].toUpperCase();
		}
	}	
}