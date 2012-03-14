package com.cmu.scout.fragment;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
	
	private static final String TAG = "MatchFragmentTeleOp";
	private static final boolean DEBUG = true;
	
	public static final int EDIT_TEXT_NONE = 0;
	public static final int EDIT_TEXT_HIGH = 1;
	public static final int EDIT_TEXT_MED = 2;
	public static final int EDIT_TEXT_LOW = 3;
	
	public int faultyEditTextBox = EDIT_TEXT_NONE;
	
	private EditText mHighCounter;
	private EditText mHighAtmpCounter; 
	private EditText mMedCounter;
	private EditText mMedAtmpCounter;
	private EditText mLowCounter;
	private EditText mLowAtmpCounter;
	
	private int mTeamId;
	private int mMatchId;
	private int mMatchNum;
	
	private static final String HIGH_MADE_STORAGE_KEY = "high_made";
	private static final String MED_MADE_STORAGE_KEY = "med_made";
	private static final String LOW_MADE_STORAGE_KEY = "low_made";
	private static final String HIGH_ATTEMPT_STORAGE_KEY = "high_attempt";
	private static final String MED_ATTEMPT_STORAGE_KEY = "med_attempt";
	private static final String LOW_ATTEMPT_STORAGE_KEY = "low_attempt";
	
	private static final String SUMMARY_MADE_STORAGE_KEY = "summary_made";
	private static final String SUMMARY_ATTEMPT_STORAGE_KEY = "summary_attempt";
	private static final String SUMMARY_POINTS_STORAGE_KEY = "summary_points";
	
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
		if (DEBUG) Log.v(TAG, "newInstance()");
		return new MatchInputTeleOpFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mTeamId = savedInstanceState.getInt("mTeamId");
			mMatchId = savedInstanceState.getInt("mMatchId");
			mMatchNum = savedInstanceState.getInt("mMatchNum");
			
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
		
		if (DEBUG) Log.v(TAG, "++ ON CREATE VIEW ++");
		final View parent = inflater.inflate(R.layout.match_scout_teleop_page, container, false);
		
		mHighCounter = (EditText) parent.findViewById(R.id.ET_Shots_Hit_High);
		mMedCounter = (EditText) parent.findViewById(R.id.ET_Shots_Hit_Med);
		mLowCounter = (EditText) parent.findViewById(R.id.ET_Shots_Hit_Low);
		
		mHighAtmpCounter = (EditText) parent.findViewById(R.id.ET_Shots_Atmp_High);
		mMedAtmpCounter = (EditText) parent.findViewById(R.id.ET_Shots_Atmp_Med);
		mLowAtmpCounter = (EditText) parent.findViewById(R.id.ET_Shots_Atmp_Low);
		
		return parent;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DEBUG) Log.v(TAG, "+ ON ACTIVITY CREATED +");
		
		loadData();
		
		// store initial data so we don't add to cumulative data when we shouldn't be
		String highShotsMade = mHighCounter.getText().toString();
		String medShotsMade  = mMedCounter.getText().toString();
		String lowShotsMade  = mLowCounter.getText().toString();
		String highShotsAtmp = mHighAtmpCounter.getText().toString();
		String medShotsAtmp  = mMedAtmpCounter.getText().toString();
		String lowShotsAtmp  = mLowAtmpCounter.getText().toString();
		
		mHighMadeInit = (!TextUtils.isEmpty(highShotsMade)) ? Integer.valueOf(highShotsMade) : 0;
		mMedMadeInit = (!TextUtils.isEmpty(medShotsMade)) ? Integer.valueOf(medShotsMade) : 0;
		mLowMadeInit = (!TextUtils.isEmpty(lowShotsMade)) ? Integer.valueOf(lowShotsMade) : 0;
		mHighAttemptInit = (!TextUtils.isEmpty(highShotsAtmp)) ? Integer.valueOf(highShotsAtmp) : 0;
		mMedAttemptInit = (!TextUtils.isEmpty(medShotsAtmp)) ? Integer.valueOf(medShotsAtmp) : 0;
		mLowAttemptInit = (!TextUtils.isEmpty(lowShotsAtmp)) ? Integer.valueOf(lowShotsAtmp) : 0;
		
		mSummaryNumScoredInit = (mHighMadeInit + mMedMadeInit + mLowMadeInit);
		mSummaryNumAttemptInit = (mHighAttemptInit + mMedAttemptInit + mLowAttemptInit);
		mSummaryNumPointsInit = (3*mHighMadeInit + 2*mMedMadeInit + 1*mLowMadeInit);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (DEBUG) Log.v(TAG, "ON SAVE INSTANCE STATE");
		
		outState.putInt("mTeamId", mTeamId);
		outState.putInt("mMatchId", mMatchId);
		outState.putInt("mMatchNum", mMatchNum);
		
		outState.putInt(HIGH_MADE_STORAGE_KEY, mHighMadeInit);
		outState.putInt(MED_MADE_STORAGE_KEY, mHighMadeInit);
		outState.putInt(LOW_MADE_STORAGE_KEY, mLowMadeInit);
		outState.putInt(HIGH_ATTEMPT_STORAGE_KEY, mHighAttemptInit);
		outState.putInt(MED_ATTEMPT_STORAGE_KEY, mMedAttemptInit);
		outState.putInt(LOW_ATTEMPT_STORAGE_KEY, mLowAttemptInit);
		
		outState.putInt(SUMMARY_MADE_STORAGE_KEY, mSummaryNumScoredInit);
		outState.putInt(SUMMARY_ATTEMPT_STORAGE_KEY, mSummaryNumAttemptInit);
		outState.putInt(SUMMARY_POINTS_STORAGE_KEY, mSummaryNumPointsInit);
	}
	
	@Override
	public void updateDisplay(int viewId) {
		if (DEBUG) Log.v(TAG, "updateDisplay()");

		switch(viewId) {
		case R.id.BT_Shots_Hit_High:
			incCount(mHighCounter);
			incCount(mHighAtmpCounter);
			break;
		case R.id.BT_Shots_Hit_Med:
			incCount(mMedCounter);
			incCount(mMedAtmpCounter);
			break;
		case R.id.BT_Shots_Hit_Low:
			incCount(mLowCounter);
			incCount(mLowAtmpCounter);
			break;
		case R.id.BT_Shots_Miss_High:
			incCount(mHighAtmpCounter);
			break;
		case R.id.BT_Shots_Miss_Med:
			incCount(mMedAtmpCounter);
			break;
		case R.id.BT_Shots_Miss_Low:
			incCount(mLowAtmpCounter);
			break;
		}
	}

	public void setFaultyEditTextBox(int box) {
		faultyEditTextBox = box;
	}
	
	public int getFaultyEditTextBox() {
		return faultyEditTextBox;
	}
	
	public void resetFaultyEditTextBox() {
		faultyEditTextBox = 0;
	}
	
	public void selectFaultyEditTextBox(int box) {
		switch (box) {
		case EDIT_TEXT_NONE: break;
		case EDIT_TEXT_HIGH: 
			mHighCounter.requestFocus();
			break;
		case EDIT_TEXT_MED: 
			mMedCounter.requestFocus();
			break;
		case EDIT_TEXT_LOW: 
			mLowCounter.requestFocus();
			break;
		}
		resetFaultyEditTextBox();
	}
	
	@Override
	public ContentValues getData() {
		if (DEBUG) Log.v(TAG, "getData()");

		String highShotsMade = mHighCounter.getText().toString();
		String medShotsMade  = mMedCounter.getText().toString();
		String lowShotsMade  = mLowCounter.getText().toString();
		String highShotsAtmp = mHighAtmpCounter.getText().toString();
		String medShotsAtmp  = mMedAtmpCounter.getText().toString();
		String lowShotsAtmp  = mLowAtmpCounter.getText().toString();
		
		int numHighShotsMade = (highShotsMade.isEmpty()) ? 0 : Integer.valueOf(highShotsMade);
		int numMedShotsMade  = (medShotsMade.isEmpty())  ? 0 : Integer.valueOf(medShotsMade);
		int numLowShotsMade  = (lowShotsMade.isEmpty())  ? 0 : Integer.valueOf(lowShotsMade);
		int numHighShotsAtmp = (highShotsAtmp.isEmpty()) ? 0 : Integer.valueOf(highShotsAtmp);
		int numMedShotsAtmp  = (medShotsAtmp.isEmpty())  ? 0 : Integer.valueOf(medShotsAtmp);
		int numLowShotsAtmp  = (lowShotsAtmp.isEmpty())  ? 0 : Integer.valueOf(lowShotsAtmp);
		
		int summaryNumScored = (numHighShotsMade + numMedShotsMade + numLowShotsMade) - mSummaryNumScoredInit;
		int summaryNumAttempt = (numHighShotsAtmp + numMedShotsAtmp + numLowShotsAtmp) - mSummaryNumAttemptInit;
		int summaryNumPoints = (3*numHighShotsMade + 2*numMedShotsMade + 1*numLowShotsMade) - mSummaryNumPointsInit;
		
		// Protect against corrupt user input
		if (numHighShotsMade > numHighShotsAtmp) {
			setFaultyEditTextBox(1);
			return null;
		}
		
		if (numMedShotsMade > numMedShotsAtmp) {
			setFaultyEditTextBox(2);
			return null;
		}
		
		if (numLowShotsMade > numLowShotsAtmp) {
			setFaultyEditTextBox(3);
			return null;
		}
		
		ContentValues values = new ContentValues();
		
		values.put(TeamMatches.NUM_SCORED_HIGH, numHighShotsMade);
		values.put(TeamMatches.NUM_SCORED_MED, numMedShotsMade);
		values.put(TeamMatches.NUM_SCORED_LOW, numLowShotsMade);
		values.put(TeamMatches.NUM_ATTEMPT_HIGH, numHighShotsAtmp);
		values.put(TeamMatches.NUM_ATTEMPT_MED, numMedShotsAtmp);
		values.put(TeamMatches.NUM_ATTEMPT_LOW, numLowShotsAtmp);
		
		values.put(Teams.SUMMARY_NUM_SCORED, summaryNumScored);
		values.put(Teams.SUMMARY_NUM_ATTEMPT, summaryNumAttempt);
		values.put(Teams.SUMMARY_NUM_POINTS, summaryNumPoints);
		
		return values;
	}
	
	@Override
	public void loadData() {
		if (DEBUG) Log.v(TAG, "loadData()");
		
		mTeamId = ((MatchPagerActivity) getActivity()).getCurrentTeamId();
		mMatchNum = ((MatchPagerActivity) getActivity()).getCurrentMatchNum();
		
		final Cursor matchCur = getActivity().getContentResolver().query(Matches.CONTENT_URI, null, Matches.MATCH_NUM + " = ?", new String[] { "" + mMatchNum }, null);
		
		if (matchCur != null && matchCur.moveToFirst()) {
			
			mMatchId = matchCur.getInt(matchCur.getColumnIndex(Matches._ID));
			
			Uri teamUri = Matches.buildMatchIdTeamIdUri(""+mMatchId, ""+mTeamId);
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
				mHighAtmpCounter.setText("" + numHighShotsAtmp);
				mMedAtmpCounter.setText("" + numMedShotsAtmp);
				mLowAtmpCounter.setText("" + numLowShotsAtmp);
			}
		}
	}

	@Override
	public void clearScreen() {
		if (DEBUG) Log.v(TAG, "clearScreen()");

		mHighCounter.setText(R.string.zero);
		mHighAtmpCounter.setText(R.string.zero);
		mMedCounter.setText(R.string.zero);
		mMedAtmpCounter.setText(R.string.zero);
		mLowCounter.setText(R.string.zero);
		mLowAtmpCounter.setText(R.string.zero);
	}
	
    public void incCount(EditText et){
    	String scoreStr = et.getText().toString();
		int score = (!scoreStr.isEmpty()) ? new Integer(scoreStr) : 0;
		score = Math.min(score+1, MatchPagerActivity.MAX_SCORE);
		et.setText("" + score);
    }
}
