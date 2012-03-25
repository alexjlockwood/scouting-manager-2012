package com.cmu.scout.fragment;

import android.content.ContentValues;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.cmu.scout.R;
import com.cmu.scout.provider.ScoutContract.Matches;
import com.cmu.scout.provider.ScoutContract.Teams;

public class MatchInputStartFragment extends MatchFragment {
	
	private static final String TAG = "MatchInputFragmentStart";
	private static final boolean DEBUG = true;
	
	public static final int EDIT_TEXT_NONE = 0;
	public static final int EDIT_TEXT_MATCH = 1;
	public static final int EDIT_TEXT_TEAM = 2;
	
	public int faultyEditTextBox = EDIT_TEXT_NONE;
	
	private EditText mStartMatchNumber;
	private EditText mStartTeamNumber; 
	
	private int mTeamNum;
	private int mMatchNum;

	public static MatchInputStartFragment newInstance() {
		if (DEBUG) Log.v(TAG, "newInstance()");
		return new MatchInputStartFragment();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mTeamNum = savedInstanceState.getInt("mTeamNum");
			mMatchNum = savedInstanceState.getInt("mMatchNum");
		}
				
		if (DEBUG) Log.v(TAG, "++ ON CREATE VIEW ++");
		final View parent = inflater.inflate(R.layout.match_scout_start_page, container, false);
			
		mStartTeamNumber = (EditText) parent.findViewById(R.id.ET_team_number);
		mStartMatchNumber = (EditText) parent.findViewById(R.id.ET_match_number);
		
		return parent;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DEBUG) Log.v(TAG, "+ ON ACTIVITY CREATED +");
		
		mStartMatchNumber.requestFocus();
		
		//loadData();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (DEBUG) Log.v(TAG, "ON SAVE INSTANCE STATE");
		
		outState.putInt("mTeamNum", mTeamNum);
		outState.putInt("mMatchNum", mMatchNum);
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
		case EDIT_TEXT_NONE: 
			break;
		case EDIT_TEXT_MATCH: 
			mStartMatchNumber.requestFocus();
			break;
		case EDIT_TEXT_TEAM: 
			mStartTeamNumber.requestFocus();
			break;
		}
		resetFaultyEditTextBox();
	}
	
	@Override
	public ContentValues getData() {
		if (DEBUG) Log.v(TAG, "getData()");
		
		String matchNumStr = mStartMatchNumber.getText().toString();
		String teamNumStr  = mStartTeamNumber.getText().toString();
		
		// Protect against corrupt user input
		if (TextUtils.isEmpty(matchNumStr) || Integer.parseInt(matchNumStr) <= 0) {
			setFaultyEditTextBox(EDIT_TEXT_MATCH);
			return null;
		}
		
		if (TextUtils.isEmpty(teamNumStr) || Integer.parseInt(teamNumStr) <= 0) {
			setFaultyEditTextBox(EDIT_TEXT_TEAM);
			return null;
		}
		
		int matchNum = Integer.parseInt(matchNumStr);
		int teamNum = Integer.parseInt(teamNumStr);

		ContentValues values = new ContentValues();
		values.put(Matches.MATCH_NUM, matchNum);
		values.put(Teams.TEAM_NUM, teamNum);
		
		return values;
	}
	
	@Override
	public void loadData() {
		/* No implementation required */
	}

	@Override
	public void clearScreen() {
		if (DEBUG) Log.v(TAG, "clearScreen()");
		
		mStartMatchNumber.setText("");
		mStartTeamNumber.setText("");
	}
	
	@Override
	public void updateDisplay(int viewId) {
		/* No implementation required */
	}
}
