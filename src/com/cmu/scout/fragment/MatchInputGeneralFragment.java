package com.cmu.scout.fragment;

import java.util.HashMap;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

import com.cmu.scout.R;
import com.cmu.scout.provider.ScoutContract.Matches;
import com.cmu.scout.provider.ScoutContract.TeamMatches;
import com.cmu.scout.provider.ScoutContract.Teams;
import com.cmu.scout.ui.MatchPagerActivity;

public class MatchInputGeneralFragment extends MatchFragment {
	
	private static final String TAG = "MatchInputGeneralFragment";
	private static final boolean DEBUG = true;
		
	private ToggleButton mToggleBalance;
	private ToggleButton mDidNothing;
	
	private RadioButton mRadioButtonBalance1;
	private RadioButton mRadioButtonBalance2;
	private RadioButton mRadioButtonBalance3;

	private RadioGroup mRadioBalance;
	private RadioGroup mRadioCross;
	private RadioGroup mRadioPickBalls;
	private RadioGroup mRadioSpeed;
	private RadioGroup mRadioAgility;
	private RadioGroup mRadioStrategy;
	private RadioGroup mRadioPenalty;
	
	private RadioGroup mRadioAlliance;
	private RadioGroup mRadioWinMatch;
	
	private EditText mFinalScore;
	private EditText mComment;
	
	// provides a mapping of View IDs to integer values 
	private static HashMap<Integer, Integer> mViewIdMap;
	
	// these are the values to be stored in database
	static {
		mViewIdMap = new HashMap<Integer,Integer>();

		mViewIdMap.put(R.id.RB_Balance_1, 1);
		mViewIdMap.put(R.id.RB_Balance_2, 2);
		mViewIdMap.put(R.id.RB_Balance_3, 3);
		mViewIdMap.put(R.id.RB_Cross_Bridge, 1);
		mViewIdMap.put(R.id.RB_Cross_Barrier, 2);
		mViewIdMap.put(R.id.RB_Cross_Both, 3);
		mViewIdMap.put(R.id.RB_Pick_Ball_Feeder, 0);
		mViewIdMap.put(R.id.RB_Pick_Ball_Floor, 1);
		mViewIdMap.put(R.id.RB_Pick_Ball_Both, 2);
		mViewIdMap.put(R.id.RB_Speed_Pr, 0);
		mViewIdMap.put(R.id.RB_Speed_Fr, 1);
		mViewIdMap.put(R.id.RB_Speed_Gd, 2);
		mViewIdMap.put(R.id.RB_Speed_Ex, 3);
		mViewIdMap.put(R.id.RB_Agility_Pr, 0);
		mViewIdMap.put(R.id.RB_Agility_Fr, 1);
		mViewIdMap.put(R.id.RB_Agility_Gd, 2);
		mViewIdMap.put(R.id.RB_Agility_Ex, 3);
		mViewIdMap.put(R.id.RB_Strategy_Offensive, 0);
		mViewIdMap.put(R.id.RB_Strategy_Defensive, 1);
		mViewIdMap.put(R.id.RB_Strategy_Neutral, 2);
		mViewIdMap.put(R.id.RB_Penalty_Risk_Low, 0);
		mViewIdMap.put(R.id.RB_Penalty_Risk_Med, 1);
		mViewIdMap.put(R.id.RB_Penalty_Risk_High, 2);
		mViewIdMap.put(R.id.RB_alliance_blue, 0);
		mViewIdMap.put(R.id.RB_alliance_red, 1);
		mViewIdMap.put(R.id.RB_match_loss, 0);
		mViewIdMap.put(R.id.RB_match_win, 1);
		//mViewIdMap.put(R.id.RB_match_draw, 2);
		mViewIdMap.put(-1,-1);
	}
	
	private static final String[] PROJECTION = { 
		TeamMatches.TEAM_ID, 
		TeamMatches.NUM_BALANCED,
		TeamMatches.HOW_CROSS,
		TeamMatches.PICK_UP_BALLS,
		TeamMatches.SPEED,
		TeamMatches.AGILITY,
		TeamMatches.STRATEGY,
		TeamMatches.PENALTY_RISK,
		TeamMatches.WHICH_ALLIANCE,
		TeamMatches.WIN_MATCH,
		TeamMatches.FINAL_SCORE,
		TeamMatches.COMMENTS,
		TeamMatches.DID_NOTHING
	};
	
	private static final String GENERAL_WIN_STORAGE_KEY = "general_win";
	private static final String GENERAL_LOSS_STORAGE_KEY = "general_loss";
	private static final String GENERAL_SCORE_STORAGE_KEY = "general_score";
	
	private boolean mGeneralWinInit;
	private boolean mGeneralLossInit;
	private int mGeneralScoreInit;
	
	public static MatchInputGeneralFragment newInstance() {
		if (DEBUG) Log.v(TAG, "newInstance()");
		return new MatchInputGeneralFragment(); 
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "++ ON CREATE VIEW ++");
		
		if (savedInstanceState != null) {		
			mGeneralWinInit = savedInstanceState.getBoolean(GENERAL_WIN_STORAGE_KEY);
			mGeneralLossInit = savedInstanceState.getBoolean(GENERAL_LOSS_STORAGE_KEY);
			mGeneralScoreInit = savedInstanceState.getInt(GENERAL_SCORE_STORAGE_KEY);
		}
	
		final View parent = inflater.inflate(R.layout.match_scout_general_page, container, false);
		
		mToggleBalance = (ToggleButton) parent.findViewById(R.id.TBT_Balance);
		mRadioButtonBalance1 = (RadioButton) parent.findViewById(R.id.RB_Balance_1);
		mRadioButtonBalance2 = (RadioButton) parent.findViewById(R.id.RB_Balance_2);
		mRadioButtonBalance3 = (RadioButton) parent.findViewById(R.id.RB_Balance_3);
		
		mRadioBalance = (RadioGroup) parent.findViewById(R.id.RBG_Balance);
		mRadioCross = (RadioGroup) parent.findViewById(R.id.RBG_Cross);
		mRadioPickBalls = (RadioGroup) parent.findViewById(R.id.RBG_Pick_Ball);
		mRadioSpeed = (RadioGroup) parent.findViewById(R.id.RBG_Speed);
		mRadioAgility = (RadioGroup) parent.findViewById(R.id.RBG_Agility);
		mRadioStrategy = (RadioGroup) parent.findViewById(R.id.RBG_Strategy);
		mRadioPenalty = (RadioGroup) parent.findViewById(R.id.RBG_Penalty_Risk);
		
		mRadioAlliance = (RadioGroup) parent.findViewById(R.id.RBG_which_alliance);
		mRadioWinMatch = (RadioGroup) parent.findViewById(R.id.RBG_win_match);
		
		mFinalScore = (EditText) parent.findViewById(R.id.ET_final_score);
		
		mComment = (EditText) parent.findViewById(R.id.ET_Match_Comment);
		mDidNothing = (ToggleButton) parent.findViewById(R.id.TBT_did_nothing);
		
		return parent;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (DEBUG) Log.v(TAG, "ON SAVE INSTANCE STATE");
		
		outState.putBoolean(GENERAL_WIN_STORAGE_KEY, mGeneralWinInit);
		outState.putBoolean(GENERAL_LOSS_STORAGE_KEY, mGeneralLossInit);
		outState.putInt(GENERAL_SCORE_STORAGE_KEY, mGeneralScoreInit);
	}
	
	@Override
	public void updateDisplay(int viewId) {
		if (DEBUG) Log.v(TAG, "updateDisplay()");

		switch(viewId) {
		case R.id.TBT_Balance:
			boolean isToggleChecked = mToggleBalance.isChecked();
			mRadioButtonBalance1.setEnabled(isToggleChecked);
			mRadioButtonBalance2.setEnabled(isToggleChecked);
			mRadioButtonBalance3.setEnabled(isToggleChecked);
			break;
		case R.id.TBT_did_nothing:
			// Do nothing (no pun intended)
			break;
		}
	}
	
	private static final String[] SUMMARY_PROJ = {
		Teams.SUMMARY_NUM_WINS,
		Teams.SUMMARY_NUM_LOSSES,
		Teams.SUMMARY_TOTAL_SCORE
	};
	
	@Override
	public void saveData() {
		if (DEBUG) Log.v(TAG, "saveData()");
		
		// get ids
		int teamId = ((MatchPagerActivity)getActivity()).getTeamId();
		int matchId = ((MatchPagerActivity)getActivity()).getMatchId();
		
		Integer numBalanced = (mToggleBalance.isChecked()) ? mRadioBalance.getCheckedRadioButtonId() : -1;
		Integer didNothing  = (mDidNothing.isChecked()) ? 1 : 0;
		Integer howCross    = mRadioCross.getCheckedRadioButtonId();
		Integer pickUpBalls = mRadioPickBalls.getCheckedRadioButtonId();
		Integer speed       = mRadioSpeed.getCheckedRadioButtonId();
		Integer agility     = mRadioAgility.getCheckedRadioButtonId();
		Integer strategy    = mRadioStrategy.getCheckedRadioButtonId();
		Integer penaltyRisk = mRadioPenalty.getCheckedRadioButtonId();		
		Integer alliance    = mRadioAlliance.getCheckedRadioButtonId();
		Integer winMatch    = mRadioWinMatch.getCheckedRadioButtonId();		
		String scoreText    = mFinalScore.getText().toString();
		Integer finalScore  = (scoreText == null || scoreText.length() == 0) ? 0 : Integer.valueOf(mFinalScore.getText().toString());
		String comments      = mComment.getText().toString();
		
		
		int summaryTotalScore = finalScore - mGeneralScoreInit;
		
		int summaryNumWins = 0, summaryNumLosses = 0;
		if (mViewIdMap.get(winMatch) == 1) {
			// then win is checked
			if (!mGeneralWinInit && !mGeneralLossInit) {
				// then neither were checked
				summaryNumWins = 1;
			} else if (!mGeneralWinInit && mGeneralLossInit) {
				// then only loss was checked
				summaryNumWins = 1;
				summaryNumLosses = -1;
			}			
		} else if (mViewIdMap.get(winMatch) == 0) {
			// then loss is checked
			if (!mGeneralWinInit && !mGeneralLossInit) {
				// then neither were checked
				summaryNumLosses = 1;
			} else if (mGeneralWinInit && !mGeneralLossInit) {
				// then only win was checked
				summaryNumWins = -1;
				summaryNumLosses = 1;
			}
		}
		
		// get already existing cumulative data
		final Uri summaryUri = Teams.buildTeamIdUri(""+teamId);
		final Cursor summaryCur = getActivity().getContentResolver().query(summaryUri, SUMMARY_PROJ, null, null, null);
		
		if (summaryCur != null && summaryCur.moveToFirst()) {
			summaryNumWins += summaryCur.getInt(summaryCur.getColumnIndex(Teams.SUMMARY_NUM_WINS));
			summaryNumLosses += summaryCur.getInt(summaryCur.getColumnIndex(Teams.SUMMARY_NUM_LOSSES));
			summaryTotalScore += summaryCur.getInt(summaryCur.getColumnIndex(Teams.SUMMARY_TOTAL_SCORE));
			summaryCur.close();
		}
			
		ContentValues teamMatchValues = new ContentValues();
		ContentValues summaryValues = new ContentValues();
		
		// add team-match data
		//teamMatchValues.put(TeamMatches.TEAM_ID,        teamId);
		//teamMatchValues.put(TeamMatches.MATCH_ID,       matchId);
		teamMatchValues.put(TeamMatches.NUM_BALANCED,   mViewIdMap.get(numBalanced));	
		teamMatchValues.put(TeamMatches.HOW_CROSS,      mViewIdMap.get(howCross));
		teamMatchValues.put(TeamMatches.PICK_UP_BALLS,  mViewIdMap.get(pickUpBalls));
		teamMatchValues.put(TeamMatches.SPEED,          mViewIdMap.get(speed));
		teamMatchValues.put(TeamMatches.AGILITY,        mViewIdMap.get(agility));
		teamMatchValues.put(TeamMatches.STRATEGY,       mViewIdMap.get(strategy));
		teamMatchValues.put(TeamMatches.PENALTY_RISK,   mViewIdMap.get(penaltyRisk));
		teamMatchValues.put(TeamMatches.WHICH_ALLIANCE, mViewIdMap.get(alliance));
		teamMatchValues.put(TeamMatches.WIN_MATCH,      mViewIdMap.get(winMatch));		
		teamMatchValues.put(TeamMatches.FINAL_SCORE,    finalScore);
		teamMatchValues.put(TeamMatches.COMMENTS,       comments);
		teamMatchValues.put(TeamMatches.DID_NOTHING,    didNothing);
		
		// add summary data
		summaryValues.put(Teams.SUMMARY_NUM_WINS, summaryNumWins);
		summaryValues.put(Teams.SUMMARY_NUM_LOSSES, summaryNumLosses);
		summaryValues.put(Teams.SUMMARY_TOTAL_SCORE, summaryTotalScore);

		getActivity().getContentResolver().update(Matches.buildMatchIdTeamIdUri(""+matchId, ""+teamId), teamMatchValues, null, null);
		getActivity().getContentResolver().update(summaryUri, summaryValues, null, null);
	}
	
	@Override
	public void loadData() {
		if (DEBUG) Log.v(TAG, "loadData()");
		
		int teamId = ((MatchPagerActivity)getActivity()).getTeamId();
		int matchId = ((MatchPagerActivity)getActivity()).getMatchId();
		
		mGeneralWinInit = false;
		mGeneralLossInit = false;
		mGeneralScoreInit = 0;

		final Uri teamUri = Matches.buildMatchIdTeamIdUri(""+matchId, ""+teamId);
		final Cursor cur = getActivity().getContentResolver().query(teamUri, PROJECTION, null, null, null);

		if (cur != null && cur.moveToFirst()) {
			int numBalanced = cur.getInt(cur.getColumnIndex(TeamMatches.NUM_BALANCED));
			int howCross  = cur.getInt(cur.getColumnIndex(TeamMatches.HOW_CROSS));
			int pickUpBalls  = cur.getInt(cur.getColumnIndex(TeamMatches.PICK_UP_BALLS));
			int speed = cur.getInt(cur.getColumnIndex(TeamMatches.SPEED));
			int agility  = cur.getInt(cur.getColumnIndex(TeamMatches.AGILITY));
			int strategy  = cur.getInt(cur.getColumnIndex(TeamMatches.STRATEGY));
			int penaltyRisk  = cur.getInt(cur.getColumnIndex(TeamMatches.STRATEGY));
			int whichAlliance  = cur.getInt(cur.getColumnIndex(TeamMatches.WHICH_ALLIANCE));
			int winMatch  = cur.getInt(cur.getColumnIndex(TeamMatches.WIN_MATCH));
			int finalScore  = cur.getInt(cur.getColumnIndex(TeamMatches.FINAL_SCORE));
			int didNothing = cur.getInt(cur.getColumnIndex(TeamMatches.DID_NOTHING));
			String comments = cur.getString(cur.getColumnIndex(TeamMatches.COMMENTS));

			cur.close();

			if (numBalanced == 1 || numBalanced == 2 || numBalanced == 3) {
				mToggleBalance.setChecked(true);
				mRadioButtonBalance1.setEnabled(true);
				mRadioButtonBalance2.setEnabled(true);
				mRadioButtonBalance3.setEnabled(true);

				switch(numBalanced) {
				case 1: mRadioBalance.check(R.id.RB_Balance_1); break;
				case 2: mRadioBalance.check(R.id.RB_Balance_2); break;
				case 3: mRadioBalance.check(R.id.RB_Balance_3); break;
				default: mRadioBalance.check(-1); break;
				}
			} else {
				mToggleBalance.setChecked(false);
				mRadioButtonBalance1.setEnabled(false);
				mRadioButtonBalance2.setEnabled(false);
				mRadioButtonBalance3.setEnabled(false);
				mRadioBalance.check(-1);
			}

			switch(howCross) {
			case 1: mRadioCross.check(R.id.RB_Cross_Bridge); break;
			case 2: mRadioCross.check(R.id.RB_Cross_Barrier); break;
			case 3: mRadioCross.check(R.id.RB_Cross_Both); break;
			default: mRadioCross.check(-1); break;
			}

			switch(pickUpBalls) {
			case 0: mRadioPickBalls.check(R.id.RB_Pick_Ball_Feeder); break;
			case 1: mRadioPickBalls.check(R.id.RB_Pick_Ball_Floor); break;
			case 2: mRadioPickBalls.check(R.id.RB_Pick_Ball_Both); break;
			default: mRadioPickBalls.check(-1); break;
			}

			switch(speed) {
			case 0: mRadioSpeed.check(R.id.RB_Speed_Pr); break;
			case 1: mRadioSpeed.check(R.id.RB_Speed_Fr); break;
			case 2: mRadioSpeed.check(R.id.RB_Speed_Gd); break;
			case 3: mRadioSpeed.check(R.id.RB_Speed_Ex); break;
			default: mRadioSpeed.check(-1); break;
			}

			switch(agility) {
			case 0: mRadioAgility.check(R.id.RB_Agility_Pr); break;
			case 1: mRadioAgility.check(R.id.RB_Agility_Fr); break;
			case 2: mRadioAgility.check(R.id.RB_Agility_Gd); break;
			case 3: mRadioAgility.check(R.id.RB_Agility_Ex); break;
			default: mRadioAgility.check(-1); break;
			}

			switch(strategy) {
			case 0: mRadioStrategy.check(R.id.RB_Strategy_Offensive); break;
			case 1: mRadioStrategy.check(R.id.RB_Strategy_Defensive); break;
			case 2: mRadioStrategy.check(R.id.RB_Strategy_Neutral); break;
			default: mRadioStrategy.check(-1); break;
			}

			switch(penaltyRisk) {
			case 0: mRadioPenalty.check(R.id.RB_Penalty_Risk_Low); break;
			case 1: mRadioPenalty.check(R.id.RB_Penalty_Risk_Med); break;
			case 2: mRadioPenalty.check(R.id.RB_Penalty_Risk_High); break;
			default: mRadioPenalty.check(-1); break;
			}

			switch(whichAlliance) {
			case 0: mRadioAlliance.check(R.id.RB_alliance_blue); break;
			case 1: mRadioAlliance.check(R.id.RB_alliance_red); break;
			default: mRadioAlliance.check(-1); break;
			}

			switch(winMatch) {
			case 0: 
				mRadioWinMatch.check(R.id.RB_match_loss); 
				mGeneralLossInit = true;
				break;
			case 1: 
				mRadioWinMatch.check(R.id.RB_match_win); 
				mGeneralWinInit = true;
				break;
			default: mRadioWinMatch.check(-1); break;
			}

			mFinalScore.setText(""+finalScore);
			mGeneralScoreInit = finalScore;
			
			mComment.setText(comments);
			
			mDidNothing.setChecked((didNothing == 1) ? true : false);
		}
	}

	@Override
	public void clearScreen() {
		if (DEBUG) Log.v(TAG, "clearScreen()");

    	mToggleBalance.setChecked(false);
    	mRadioButtonBalance1.setEnabled(false);
    	mRadioButtonBalance2.setEnabled(false);
    	mRadioButtonBalance3.setEnabled(false);
    	
    	mRadioBalance.clearCheck();
    	mRadioCross.clearCheck();
    	mRadioPickBalls.clearCheck();
    	mRadioSpeed.clearCheck();
    	mRadioAgility.clearCheck();
    	mRadioStrategy.clearCheck();
    	mRadioPenalty.clearCheck();
    	
    	mRadioAlliance.clearCheck();
    	mRadioWinMatch.clearCheck();
    	mFinalScore.setText("");
    	
    	mComment.setText("");
    	mDidNothing.setChecked(false);
	}
	
	@Override 
	public void onResume() {
		super.onResume();
		loadData();
	}
	
	@Override
	public void onPause() {
		super.onPause();		
		saveData();
	}
}
