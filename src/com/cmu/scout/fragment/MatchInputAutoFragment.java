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

public class MatchInputAutoFragment extends MatchFragment {
	
	private static final String TAG = "MatchInputFragmentAuto";
	private static final boolean DEBUG = true;
	
	public static final int EDIT_TEXT_NONE = 0;
	public static final int EDIT_TEXT_HIGH = 1;
	public static final int EDIT_TEXT_MED = 2;
	public static final int EDIT_TEXT_LOW = 3;
	
	public int faultyEditTextBox = EDIT_TEXT_NONE;
	
	private EditText mAutoHighCounter;
	private EditText mAutoHighAtmpCounter; 
	private EditText mAutoMedCounter;
	private EditText mAutoMedAtmpCounter;
	private EditText mAutoLowCounter;
	private EditText mAutoLowAtmpCounter;
	
	private int mTeamId;
	private int mMatchId;
	private int mMatchNum;
	
	private static final String AUTO_HIGH_MADE_STORAGE_KEY = "auto_high_made";
	private static final String AUTO_MED_MADE_STORAGE_KEY = "auto_med_made";
	private static final String AUTO_LOW_MADE_STORAGE_KEY = "auto_low_made";
	private static final String AUTO_HIGH_ATTEMPT_STORAGE_KEY = "auto_high_attempt";
	private static final String AUTO_MED_ATTEMPT_STORAGE_KEY = "auto_med_attempt";
	private static final String AUTO_LOW_ATTEMPT_STORAGE_KEY = "auto_low_attempt";
	
	private static final String SUMMARY_AUTO_MADE_STORAGE_KEY = "summary_auto_made";
	private static final String SUMMARY_AUTO_ATTEMPT_STORAGE_KEY = "summary_auto_attempt";
	private static final String SUMMARY_AUTO_POINTS_STORAGE_KEY = "summary_auto_points";
	
	private int mAutoHighMadeInit;
	private int mAutoMedMadeInit;
	private int mAutoLowMadeInit;
	private int mAutoHighAttemptInit;
	private int mAutoMedAttemptInit;
	private int mAutoLowAttemptInit;
	
	private int mSummaryAutoNumScoredInit;
	private int mSummaryAutoNumAttemptInit;
	private int mSummaryAutoNumPointsInit;
	
	private static final String[] PROJECTION = { 
		TeamMatches.AUTO_NUM_SCORED_HIGH, 
		TeamMatches.AUTO_NUM_SCORED_MED,
		TeamMatches.AUTO_NUM_SCORED_LOW,
		TeamMatches.AUTO_NUM_ATTEMPT_HIGH,
		TeamMatches.AUTO_NUM_ATTEMPT_MED,
		TeamMatches.AUTO_NUM_ATTEMPT_LOW
	};

	public static MatchInputAutoFragment newInstance() {
		if (DEBUG) Log.v(TAG, "newInstance()");
		return new MatchInputAutoFragment();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mTeamId = savedInstanceState.getInt("mTeamId");
			mMatchId = savedInstanceState.getInt("mMatchId");
			mMatchNum = savedInstanceState.getInt("mMatchNum");
			
			mAutoHighMadeInit = savedInstanceState.getInt(AUTO_HIGH_MADE_STORAGE_KEY);
			mAutoMedMadeInit = savedInstanceState.getInt(AUTO_MED_MADE_STORAGE_KEY);
			mAutoLowMadeInit = savedInstanceState.getInt(AUTO_LOW_MADE_STORAGE_KEY);
			mAutoHighAttemptInit = savedInstanceState.getInt(AUTO_HIGH_ATTEMPT_STORAGE_KEY);
			mAutoMedAttemptInit = savedInstanceState.getInt(AUTO_MED_ATTEMPT_STORAGE_KEY);
			mAutoLowAttemptInit = savedInstanceState.getInt(AUTO_LOW_ATTEMPT_STORAGE_KEY);
			
			mSummaryAutoNumScoredInit = savedInstanceState.getInt(SUMMARY_AUTO_MADE_STORAGE_KEY);
			mSummaryAutoNumAttemptInit = savedInstanceState.getInt(SUMMARY_AUTO_ATTEMPT_STORAGE_KEY);
			mSummaryAutoNumPointsInit = savedInstanceState.getInt(SUMMARY_AUTO_POINTS_STORAGE_KEY);
		}
				
		if (DEBUG) Log.v(TAG, "++ ON CREATE VIEW ++");
		final View parent = inflater.inflate(R.layout.match_scout_auto_page, container, false);
		
		mAutoHighCounter = (EditText) parent.findViewById(R.id.ET_Auto_Shots_Hit_High);
		mAutoMedCounter = (EditText) parent.findViewById(R.id.ET_Auto_Shots_Hit_Med);
		mAutoLowCounter = (EditText) parent.findViewById(R.id.ET_Auto_Shots_Hit_Low);
		
		mAutoHighAtmpCounter = (EditText) parent.findViewById(R.id.ET_Auto_Shots_Atmp_High);
		mAutoMedAtmpCounter = (EditText) parent.findViewById(R.id.ET_Auto_Shots_Atmp_Med);
		mAutoLowAtmpCounter = (EditText) parent.findViewById(R.id.ET_Auto_Shots_Atmp_Low);
		
		return parent;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DEBUG) Log.v(TAG, "+ ON ACTIVITY CREATED +");
				
		loadData();
		
		// store initial data so we don't add to cumulative data when we shouldn't be
		String highShotsMade = mAutoHighCounter.getText().toString();
		String medShotsMade  = mAutoMedCounter.getText().toString();
		String lowShotsMade  = mAutoLowCounter.getText().toString();
		String highShotsAtmp = mAutoHighAtmpCounter.getText().toString();
		String medShotsAtmp  = mAutoMedAtmpCounter.getText().toString();
		String lowShotsAtmp  = mAutoLowAtmpCounter.getText().toString();
		
		mAutoHighMadeInit = (!TextUtils.isEmpty(highShotsMade)) ? Integer.valueOf(highShotsMade) : 0;
		mAutoMedMadeInit = (!TextUtils.isEmpty(medShotsMade)) ? Integer.valueOf(medShotsMade) : 0;
		mAutoLowMadeInit = (!TextUtils.isEmpty(lowShotsMade)) ? Integer.valueOf(lowShotsMade) : 0;
		mAutoHighAttemptInit = (!TextUtils.isEmpty(highShotsAtmp)) ? Integer.valueOf(highShotsAtmp) : 0;
		mAutoMedAttemptInit = (!TextUtils.isEmpty(medShotsAtmp)) ? Integer.valueOf(medShotsAtmp) : 0;
		mAutoLowAttemptInit = (!TextUtils.isEmpty(lowShotsAtmp)) ? Integer.valueOf(lowShotsAtmp) : 0;
		
		mSummaryAutoNumScoredInit = (mAutoHighMadeInit + mAutoMedMadeInit + mAutoLowMadeInit);
		mSummaryAutoNumAttemptInit = (mAutoHighAttemptInit + mAutoMedAttemptInit + mAutoLowAttemptInit);
		mSummaryAutoNumPointsInit = (3*mAutoHighMadeInit + 2*mAutoMedMadeInit + 1*mAutoLowMadeInit);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (DEBUG) Log.v(TAG, "ON SAVE INSTANCE STATE");
		
		outState.putInt("mTeamId", mTeamId);
		outState.putInt("mMatchId", mMatchId);
		outState.putInt("mMatchNum", mMatchNum);
		
		outState.putInt(AUTO_HIGH_MADE_STORAGE_KEY, mAutoHighMadeInit);
		outState.putInt(AUTO_MED_MADE_STORAGE_KEY, mAutoHighMadeInit);
		outState.putInt(AUTO_LOW_MADE_STORAGE_KEY, mAutoLowMadeInit);
		outState.putInt(AUTO_HIGH_ATTEMPT_STORAGE_KEY, mAutoHighAttemptInit);
		outState.putInt(AUTO_MED_ATTEMPT_STORAGE_KEY, mAutoMedAttemptInit);
		outState.putInt(AUTO_LOW_ATTEMPT_STORAGE_KEY, mAutoLowAttemptInit);
		
		outState.putInt(SUMMARY_AUTO_MADE_STORAGE_KEY, mSummaryAutoNumScoredInit);
		outState.putInt(SUMMARY_AUTO_ATTEMPT_STORAGE_KEY, mSummaryAutoNumAttemptInit);
		outState.putInt(SUMMARY_AUTO_POINTS_STORAGE_KEY, mSummaryAutoNumPointsInit);
	}

	@Override
	public void updateDisplay(int viewId) {
		if (DEBUG) Log.v(TAG, "updateDisplay()");
		
		switch(viewId) {
		case R.id.BT_Auto_Shots_Hit_High:
			incCount(mAutoHighCounter);
			incCount(mAutoHighAtmpCounter);
			break;
		case R.id.BT_Auto_Shots_Hit_Med:
			incCount(mAutoMedCounter);
			incCount(mAutoMedAtmpCounter);
			break;
		case R.id.BT_Auto_Shots_Hit_Low:
			incCount(mAutoLowCounter);
			incCount(mAutoLowAtmpCounter);
			break;
		case R.id.BT_Auto_Shots_Miss_High:
			incCount(mAutoHighAtmpCounter);
			break;
		case R.id.BT_Auto_Shots_Miss_Med:
			incCount(mAutoMedAtmpCounter);
			break;
		case R.id.BT_Auto_Shots_Miss_Low:
			incCount(mAutoLowAtmpCounter);
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
		faultyEditTextBox = EDIT_TEXT_NONE;
	}
	
	public void selectFaultyEditTextBox(int box) {
		switch (box) {
		case EDIT_TEXT_NONE: break;
		case EDIT_TEXT_HIGH: 
			mAutoHighCounter.requestFocus();
			break;
		case EDIT_TEXT_MED: 
			mAutoMedCounter.requestFocus();
			break;
		case EDIT_TEXT_LOW:
			mAutoLowCounter.requestFocus();
			break;
		}
		resetFaultyEditTextBox();
	}

	@Override
	public ContentValues getData() {
		if (DEBUG) Log.v(TAG, "getData()");
		
		String highShotsMade = mAutoHighCounter.getText().toString();
		String medShotsMade  = mAutoMedCounter.getText().toString();
		String lowShotsMade  = mAutoLowCounter.getText().toString();
		String highShotsAtmp = mAutoHighAtmpCounter.getText().toString();
		String medShotsAtmp  = mAutoMedAtmpCounter.getText().toString();
		String lowShotsAtmp  = mAutoLowAtmpCounter.getText().toString();
		
		int numHighShotsMade = (highShotsMade.isEmpty()) ? 0 : Integer.valueOf(highShotsMade);
		int numMedShotsMade  = (medShotsMade.isEmpty())  ? 0 : Integer.valueOf(medShotsMade);
		int numLowShotsMade  = (lowShotsMade.isEmpty())  ? 0 : Integer.valueOf(lowShotsMade);
		int numHighShotsAtmp = (highShotsAtmp.isEmpty()) ? 0 : Integer.valueOf(highShotsAtmp);
		int numMedShotsAtmp  = (medShotsAtmp.isEmpty())  ? 0 : Integer.valueOf(medShotsAtmp);
		int numLowShotsAtmp  = (lowShotsAtmp.isEmpty())  ? 0 : Integer.valueOf(lowShotsAtmp);
		
		int summaryAutoNumScored = (numHighShotsMade + numMedShotsMade + numLowShotsMade) - mSummaryAutoNumScoredInit;
		int summaryAutoNumAttempt = (numHighShotsAtmp + numMedShotsAtmp + numLowShotsAtmp) - mSummaryAutoNumAttemptInit;
		int summaryAutoNumPoints = (3*numHighShotsMade + 2*numMedShotsMade + 1*numLowShotsMade) - mSummaryAutoNumPointsInit;
		
		// Protect against corrupt user input
		if (numHighShotsMade > numHighShotsAtmp) {
			setFaultyEditTextBox(EDIT_TEXT_HIGH);
			return null;
		}
		
		if (numMedShotsMade > numMedShotsAtmp) {
			setFaultyEditTextBox(EDIT_TEXT_MED);
			return null;
		}
		
		if (numLowShotsMade > numLowShotsAtmp) {
			setFaultyEditTextBox(EDIT_TEXT_LOW);
			return null;
		}
		
		ContentValues values = new ContentValues();
		
		values.put(TeamMatches.AUTO_NUM_SCORED_HIGH, numHighShotsMade);
		values.put(TeamMatches.AUTO_NUM_SCORED_MED, numMedShotsMade);
		values.put(TeamMatches.AUTO_NUM_SCORED_LOW, numLowShotsMade);
		values.put(TeamMatches.AUTO_NUM_ATTEMPT_HIGH, numHighShotsAtmp);
		values.put(TeamMatches.AUTO_NUM_ATTEMPT_MED, numMedShotsAtmp);
		values.put(TeamMatches.AUTO_NUM_ATTEMPT_LOW, numLowShotsAtmp);
		
		values.put(Teams.SUMMARY_AUTO_NUM_SCORED, summaryAutoNumScored);
		values.put(Teams.SUMMARY_AUTO_NUM_ATTEMPT, summaryAutoNumAttempt);
		values.put(Teams.SUMMARY_AUTO_NUM_POINTS, summaryAutoNumPoints);
		
		return values;
	}
	
	@Override
	public void loadData() {
		if (DEBUG) Log.v(TAG, "loadData()");
		
		// TODO: THIS IS BAD DESIGN. FRAGMENTS SHOULD BE DESIGNED FOR REUSE
		mTeamId = ((MatchPagerActivity) getActivity()).getCurrentTeamId();
		mMatchNum = ((MatchPagerActivity) getActivity()).getCurrentMatchNum();
		
		final Cursor matchCur = getActivity().getContentResolver().query(Matches.CONTENT_URI, null, Matches.MATCH_NUM + " = ?", new String[] { "" + mMatchNum }, null);
		
		if (matchCur != null && matchCur.moveToFirst()) {
			
			mMatchId = matchCur.getInt(matchCur.getColumnIndex(Matches._ID));
			
			Uri teamUri = Matches.buildMatchIdTeamIdUri(""+mMatchId, ""+mTeamId);
			final Cursor cur = getActivity().getContentResolver().query(teamUri, PROJECTION, null, null, null);
				
			if (cur != null && cur.moveToFirst()) {

				int numHighShotsMade = cur.getInt(cur.getColumnIndex(TeamMatches.AUTO_NUM_SCORED_HIGH));
				int numMedShotsMade  = cur.getInt(cur.getColumnIndex(TeamMatches.AUTO_NUM_SCORED_MED));
				int numLowShotsMade  = cur.getInt(cur.getColumnIndex(TeamMatches.AUTO_NUM_SCORED_LOW));
				int numHighShotsAtmp = cur.getInt(cur.getColumnIndex(TeamMatches.AUTO_NUM_ATTEMPT_HIGH));
				int numMedShotsAtmp  = cur.getInt(cur.getColumnIndex(TeamMatches.AUTO_NUM_ATTEMPT_MED));
				int numLowShotsAtmp  = cur.getInt(cur.getColumnIndex(TeamMatches.AUTO_NUM_ATTEMPT_LOW));
		
				mAutoHighCounter.setText("" + numHighShotsMade);
				mAutoMedCounter.setText("" + numMedShotsMade);
				mAutoLowCounter.setText("" + numLowShotsMade);
				mAutoHighAtmpCounter.setText("" + numHighShotsAtmp);
				mAutoMedAtmpCounter.setText("" + numMedShotsAtmp);
				mAutoLowAtmpCounter.setText("" + numLowShotsAtmp);
			}
		}
	}

	@Override
	public void clearScreen() {
		if (DEBUG) Log.v(TAG, "clearScreen()");
		
		mAutoHighCounter.setText(R.string.zero);
		mAutoHighAtmpCounter.setText(R.string.zero);
		mAutoMedCounter.setText(R.string.zero);
		mAutoMedAtmpCounter.setText(R.string.zero);
		mAutoLowCounter.setText(R.string.zero);
		mAutoLowAtmpCounter.setText(R.string.zero);
	}
	
    public void incCount(EditText et){
    	String scoreStr = et.getText().toString();
		int score = (!scoreStr.isEmpty()) ? new Integer(scoreStr) : 0;
		score = Math.min(score+1, MatchPagerActivity.MAX_SCORE);
		et.setText("" + score);
    }
}
