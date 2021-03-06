package com.cmu.scout.fragment;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.cmu.scout.R;
import com.cmu.scout.provider.ScoutContract.Matches;
import com.cmu.scout.provider.ScoutContract.TeamMatches;
import com.cmu.scout.provider.ScoutContract.Teams;
import com.cmu.scout.ui.MatchPagerActivity;

public class MatchInputTeleOpFragment extends MatchFragment {
	
//	private static final String TAG = "MatchInputTeleOpFragment";
//	private static final boolean DEBUG = true;
	
	private EditText mHighCounter;
	private EditText mHighMissCounter; 
	private EditText mMedCounter;
	private EditText mMedMissCounter;
	private EditText mLowCounter;
	private EditText mLowMissCounter;
	/*
	private static final String HIGH_MADE_STORAGE_KEY = "high_made";
	private static final String MED_MADE_STORAGE_KEY = "med_made";
	private static final String LOW_MADE_STORAGE_KEY = "low_made";
	private static final String HIGH_ATTEMPT_STORAGE_KEY = "high_attempt";
	private static final String MED_ATTEMPT_STORAGE_KEY = "med_attempt";
	private static final String LOW_ATTEMPT_STORAGE_KEY = "low_attempt";
	
	private static final String SUMMARY_MADE_STORAGE_KEY = "summary_made";
	private static final String SUMMARY_ATTEMPT_STORAGE_KEY = "summary_attempt";
	private static final String SUMMARY_POINTS_STORAGE_KEY = "summary_points";
	*/
	private int mHighMadeInit;
	private int mMedMadeInit;
	private int mLowMadeInit;
	private int mHighAttemptInit;
	private int mMedAttemptInit;
	private int mLowAttemptInit;
	
	private int mSummaryNumScoredInit;
	private int mSummaryNumAttemptInit;
	private int mSummaryNumPointsInit;
	
	private static final String[] PROJECTION = {
		TeamMatches.TEAM_ID,
		TeamMatches.NUM_SCORED_HIGH, 
		TeamMatches.NUM_SCORED_MED,
		TeamMatches.NUM_SCORED_LOW,
		TeamMatches.NUM_ATTEMPT_HIGH,
		TeamMatches.NUM_ATTEMPT_MED,
		TeamMatches.NUM_ATTEMPT_LOW
	};
	
	public static MatchInputTeleOpFragment newInstance() {
//		if (DEBUG) Log.v(TAG, "newInstance()");
	    return new MatchInputTeleOpFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		if (DEBUG) Log.v(TAG, "++ ON CREATE VIEW ++");
		/*
		if (savedInstanceState != null) {		
			mHighMadeInit = savedInstanceState.getInt(HIGH_MADE_STORAGE_KEY);
			mMedMadeInit = savedInstanceState.getInt(MED_MADE_STORAGE_KEY);
			mLowMadeInit = savedInstanceState.getInt(LOW_MADE_STORAGE_KEY);
			mHighAttemptInit = savedInstanceState.getInt(HIGH_ATTEMPT_STORAGE_KEY);
			mMedAttemptInit = savedInstanceState.getInt(MED_ATTEMPT_STORAGE_KEY);
			mLowAttemptInit = savedInstanceState.getInt(LOW_ATTEMPT_STORAGE_KEY);
			
			mSummaryNumScoredInit = savedInstanceState.getInt(SUMMARY_MADE_STORAGE_KEY);
			mSummaryNumAttemptInit = savedInstanceState.getInt(SUMMARY_ATTEMPT_STORAGE_KEY);
			mSummaryNumPointsInit = savedInstanceState.getInt(SUMMARY_POINTS_STORAGE_KEY);
		}
		*/
		final View parent = inflater.inflate(R.layout.match_scout_teleop_page, container, false);
		
		mHighCounter = (EditText) parent.findViewById(R.id.ET_Shots_Hit_High);
		mMedCounter = (EditText) parent.findViewById(R.id.ET_Shots_Hit_Med);
		mLowCounter = (EditText) parent.findViewById(R.id.ET_Shots_Hit_Low);
		
		mHighMissCounter = (EditText) parent.findViewById(R.id.ET_Shots_Miss_High);
		mMedMissCounter = (EditText) parent.findViewById(R.id.ET_Shots_Miss_Med);
		mLowMissCounter = (EditText) parent.findViewById(R.id.ET_Shots_Miss_Low);
		
		return parent;
	}
	/*
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (DEBUG) Log.v(TAG, "ON SAVE INSTANCE STATE");

		outState.putInt(HIGH_MADE_STORAGE_KEY, mHighMadeInit);
		outState.putInt(MED_MADE_STORAGE_KEY, mMedMadeInit);
		outState.putInt(LOW_MADE_STORAGE_KEY, mLowMadeInit);
		outState.putInt(HIGH_ATTEMPT_STORAGE_KEY, mHighAttemptInit);
		outState.putInt(MED_ATTEMPT_STORAGE_KEY, mMedAttemptInit);
		outState.putInt(LOW_ATTEMPT_STORAGE_KEY, mLowAttemptInit);
		
		outState.putInt(SUMMARY_MADE_STORAGE_KEY, mSummaryNumScoredInit);
		outState.putInt(SUMMARY_ATTEMPT_STORAGE_KEY, mSummaryNumAttemptInit);
		outState.putInt(SUMMARY_POINTS_STORAGE_KEY, mSummaryNumPointsInit);
	}*/
	
	@Override
	public void updateDisplay(int viewId) {
//		if (DEBUG) Log.v(TAG, "updateDisplay()");

		switch(viewId) {
		case R.id.BT_Shots_Hit_High:
			incCount(mHighCounter);
			break;
		case R.id.BT_Shots_Hit_Med:
			incCount(mMedCounter);
			break;
		case R.id.BT_Shots_Hit_Low:
			incCount(mLowCounter);
			break;
		case R.id.BT_Shots_Miss_High:
			incCount(mHighMissCounter);
			break;
		case R.id.BT_Shots_Miss_Med:
			incCount(mMedMissCounter);
			break;
		case R.id.BT_Shots_Miss_Low:
			incCount(mLowMissCounter);
			break;
		}
	}

	private static final String[] SUMMARY_PROJ = {
		Teams.SUMMARY_NUM_ATTEMPT,
		Teams.SUMMARY_NUM_SCORED,
		Teams.SUMMARY_NUM_POINTS
	};
	
	@Override
	public void saveData() {
//		if (DEBUG) Log.v(TAG, "saveData()");
		
		int teamId = ((MatchPagerActivity)getActivity()).getTeamId();
		int matchId = ((MatchPagerActivity)getActivity()).getMatchId();
		
		// retrieve data from screen
		String highShotsMade = mHighCounter.getText().toString();
		String medShotsMade  = mMedCounter.getText().toString();
		String lowShotsMade  = mLowCounter.getText().toString();
		String highShotsMiss = mHighMissCounter.getText().toString();
		String medShotsMiss  = mMedMissCounter.getText().toString();
		String lowShotsMiss  = mLowMissCounter.getText().toString();
		
		int numHighShotsMade = (highShotsMade == null || highShotsMade.length() == 0) ? 0 : Integer.valueOf(highShotsMade);
		int numMedShotsMade  = (medShotsMade == null || medShotsMade.length() == 0)  ? 0 : Integer.valueOf(medShotsMade);
		int numLowShotsMade  = (lowShotsMade == null || lowShotsMade.length() == 0)  ? 0 : Integer.valueOf(lowShotsMade);
		int numHighShotsMiss = (highShotsMiss == null || highShotsMiss.length() == 0) ? 0 : Integer.valueOf(highShotsMiss);
		int numMedShotsMiss  = (medShotsMiss == null || medShotsMiss.length() == 0)  ? 0 : Integer.valueOf(medShotsMiss);
		int numLowShotsMiss  = (lowShotsMiss == null || lowShotsMiss.length() == 0)  ? 0 : Integer.valueOf(lowShotsMiss);
		
		// compute summary data offset
		int summaryNumScored = (numHighShotsMade + numMedShotsMade + numLowShotsMade) - mSummaryNumScoredInit;
		int summaryNumAttempt = (numHighShotsMiss + numMedShotsMiss + numLowShotsMiss) 
			+(numHighShotsMade + numMedShotsMade + numLowShotsMade)- mSummaryNumAttemptInit;
		int summaryNumPoints = (3*numHighShotsMade + 2*numMedShotsMade + 1*numLowShotsMade) - mSummaryNumPointsInit;
		
		// get already existing cumulative data
		final Uri summaryUri = Teams.buildTeamIdUri(""+teamId);
		final Cursor summaryCur = getActivity().getContentResolver().query(summaryUri, SUMMARY_PROJ, null, null, null);
		
		if (summaryCur != null && summaryCur.moveToFirst()) {
			summaryNumScored += summaryCur.getInt(summaryCur.getColumnIndex(Teams.SUMMARY_NUM_SCORED));
			summaryNumAttempt += summaryCur.getInt(summaryCur.getColumnIndex(Teams.SUMMARY_NUM_ATTEMPT));
			summaryNumPoints += summaryCur.getInt(summaryCur.getColumnIndex(Teams.SUMMARY_NUM_POINTS));			
			summaryCur.close();
		}
		
		ContentValues teamMatchValues = new ContentValues();
		ContentValues summaryValues = new ContentValues();
		
		// add team-match data
		//teamMatchValues.put(TeamMatches.TEAM_ID, teamId);
		//teamMatchValues.put(TeamMatches.MATCH_ID, matchId);
		teamMatchValues.put(TeamMatches.NUM_SCORED_HIGH, numHighShotsMade);
		teamMatchValues.put(TeamMatches.NUM_SCORED_MED, numMedShotsMade);
		teamMatchValues.put(TeamMatches.NUM_SCORED_LOW, numLowShotsMade);
		teamMatchValues.put(TeamMatches.NUM_ATTEMPT_HIGH, numHighShotsMiss+numHighShotsMade);
		teamMatchValues.put(TeamMatches.NUM_ATTEMPT_MED, numMedShotsMiss+numMedShotsMade);
		teamMatchValues.put(TeamMatches.NUM_ATTEMPT_LOW, numLowShotsMiss+numLowShotsMade);
		
		// add summary data
		summaryValues.put(Teams.SUMMARY_NUM_SCORED, summaryNumScored);
		summaryValues.put(Teams.SUMMARY_NUM_ATTEMPT, summaryNumAttempt);
		summaryValues.put(Teams.SUMMARY_NUM_POINTS, summaryNumPoints);
		
		getActivity().getContentResolver().update(Matches.buildMatchIdTeamIdUri(""+matchId, ""+teamId), teamMatchValues, null, null);
		getActivity().getContentResolver().update(summaryUri, summaryValues, null, null);
	}
	
	@Override
	public void loadData() {
//		if (DEBUG) Log.v(TAG, "loadData()");
		
		int teamId = ((MatchPagerActivity)getActivity()).getTeamId();
		int matchId = ((MatchPagerActivity)getActivity()).getMatchId();

		Uri teamUri = Matches.buildMatchIdTeamIdUri(""+matchId, ""+teamId);
		final Cursor cur = getActivity().getContentResolver().query(teamUri, PROJECTION, null, null, null);
		
		if (cur != null && cur.moveToFirst()) {
			int numHighShotsMade = cur.getInt(cur.getColumnIndex(TeamMatches.NUM_SCORED_HIGH));
			int numMedShotsMade  = cur.getInt(cur.getColumnIndex(TeamMatches.NUM_SCORED_MED));
			int numLowShotsMade  = cur.getInt(cur.getColumnIndex(TeamMatches.NUM_SCORED_LOW));
			int numHighShotsAtmp = cur.getInt(cur.getColumnIndex(TeamMatches.NUM_ATTEMPT_HIGH));
			int numMedShotsAtmp  = cur.getInt(cur.getColumnIndex(TeamMatches.NUM_ATTEMPT_MED));
			int numLowShotsAtmp  = cur.getInt(cur.getColumnIndex(TeamMatches.NUM_ATTEMPT_LOW));
		
			mHighCounter.setText("" + numHighShotsMade);
			mMedCounter.setText("" + numMedShotsMade);
			mLowCounter.setText("" + numLowShotsMade);
			mHighMissCounter.setText("" + (numHighShotsAtmp-numHighShotsMade));
			mMedMissCounter.setText("" + (numMedShotsAtmp-numMedShotsMade));
			mLowMissCounter.setText("" + (numLowShotsAtmp-numLowShotsMade));
		}
		cur.close();
	}

	@Override
	public void clearScreen() {
//		if (DEBUG) Log.v(TAG, "clearScreen()");

		mHighCounter.setText(R.string.zero);
		mHighMissCounter.setText(R.string.zero);
		mMedCounter.setText(R.string.zero);
		mMedMissCounter.setText(R.string.zero);
		mLowCounter.setText(R.string.zero);
		mLowMissCounter.setText(R.string.zero);
	}
	
    public void incCount(EditText et){
    	String scoreStr = et.getText().toString();
		int score = (scoreStr == null || scoreStr.length() == 0) ? 0 : Integer.valueOf(scoreStr);
		score = Math.min(score+1, MatchPagerActivity.MAX_SCORE);
		et.setText("" + score);
    }
    
	@Override 
	public void onResume() {
		super.onResume();
		loadData();
		
		// store initial data so we don't add to cumulative data when we shouldn't be
		String highShotsMade = mHighCounter.getText().toString();
		String medShotsMade  = mMedCounter.getText().toString();
		String lowShotsMade  = mLowCounter.getText().toString();
		String highShotsMiss = mHighMissCounter.getText().toString();
		String medShotsMiss  = mMedMissCounter.getText().toString();
		String lowShotsMiss  = mLowMissCounter.getText().toString();
		
		mHighMadeInit = (!TextUtils.isEmpty(highShotsMade)) ? Integer.valueOf(highShotsMade) : 0;
		mMedMadeInit = (!TextUtils.isEmpty(medShotsMade)) ? Integer.valueOf(medShotsMade) : 0;
		mLowMadeInit = (!TextUtils.isEmpty(lowShotsMade)) ? Integer.valueOf(lowShotsMade) : 0;
		mHighAttemptInit = ((!TextUtils.isEmpty(highShotsMiss)) ? Integer.valueOf(highShotsMiss) : 0) + mHighMadeInit;
		mMedAttemptInit = ((!TextUtils.isEmpty(medShotsMiss)) ? Integer.valueOf(medShotsMiss) : 0) + mMedMadeInit;
		mLowAttemptInit = ((!TextUtils.isEmpty(lowShotsMiss)) ? Integer.valueOf(lowShotsMiss) : 0) + mLowMadeInit;
		
		mSummaryNumScoredInit = (mHighMadeInit + mMedMadeInit + mLowMadeInit);
		mSummaryNumAttemptInit = (mHighAttemptInit + mMedAttemptInit + mLowAttemptInit);
		mSummaryNumPointsInit = (3*mHighMadeInit + 2*mMedMadeInit + 1*mLowMadeInit);
	}
	
	@Override
	public void onPause() {
		super.onPause();		
		saveData();
	}
}
