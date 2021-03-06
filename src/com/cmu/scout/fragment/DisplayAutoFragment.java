package com.cmu.scout.fragment;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.cmu.scout.R;
import com.cmu.scout.provider.ScoutContract.Matches;
import com.cmu.scout.provider.ScoutContract.Teams;
import com.cmu.scout.ui.DisplayPagerActivity;
import com.cmu.scout.ui.OnTeamSelectedListener;

public class DisplayAutoFragment extends SherlockListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	
//	private static final String TAG = "DisplayAutoFragment";
//	private static final boolean DEBUG = true;
	
	private static final int DISPLAY_AUTO_LOADER = 0x01;
	
	private static final String SORT_ASC = " COLLATE LOCALIZED ASC";
	private static final String SORT_DESC = " COLLATE LOCALIZED DESC";
		
	private String mSortColumn = Teams.TEAM_NUM;
	private String mSortOrder = SORT_ASC;
	private String mSort = mSortColumn + mSortOrder;

	private AutoAdapter mAdapter;
	
	private OnTeamSelectedListener teamSelectedListener;
	
	public static DisplayAutoFragment newInstance() {
//		if (DEBUG) Log.v(TAG, "newInstance()");
		return new DisplayAutoFragment();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		if (DEBUG) Log.v(TAG, "++ ON CREATE VIEW ++");		

		View root = inflater.inflate(R.layout.display_teams_auto_layout, container, false);
	
		((TextView) root.findViewById(R.id.display_header_auto_team)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sortContent(v.getId());
			}
		});
		
		((TextView) root.findViewById(R.id.display_header_auto_shots_made_attempt)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sortContent(v.getId());
			}
		});
		
		((TextView) root.findViewById(R.id.display_header_auto_shots_percent)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sortContent(v.getId());
			}
		});
		
		((TextView) root.findViewById(R.id.display_header_auto_total_score)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sortContent(v.getId());
			}
		});
		
		return root;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
//		if (DEBUG) Log.v(TAG, "+ ON ACTIVITY CREATED +");
				
		getLoaderManager().initLoader(DISPLAY_AUTO_LOADER, null, this);	
		mAdapter = new AutoAdapter(getActivity(), null, 0);
		setListAdapter(mAdapter);
		
		registerForContextMenu(getListView());
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
//		if (DEBUG) Log.v(TAG, "+ ON ATTACH +");
		try {
			teamSelectedListener = (OnTeamSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnTeamSelectedListener");
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getActivity().getMenuInflater().inflate(R.menu.display_team_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Uri uri;
		
		switch (item.getItemId()) {
		case R.id.menu_team_edit_data:
			uri = Teams.buildTeamIdUri("" + info.id);
			((DisplayPagerActivity) getActivity()).onTeamEdit(uri);
			return true;
		case R.id.menu_display_teams_matches:
			uri = Matches.buildMatchTeamIdUri("" + info.id);
			teamSelectedListener.onTeamSelected(uri);
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	private static final String[] PROJECTION = { 
		Teams._ID, 
		Teams.TEAM_NUM,
		Teams.SUMMARY_AUTO_NUM_SCORED,
		Teams.SUMMARY_AUTO_NUM_ATTEMPT,
		Teams.SUMMARY_AUTO_NUM_POINTS
	};
	
	// called from the Activity
	public void sortContent(int viewId) {
//		if (DEBUG) Log.v(TAG, "onClickHandler");
		
		String col = "";
		
		switch(viewId) {
		case R.id.display_header_auto_team: 
			col = Teams.TEAM_NUM;
			break;
		case R.id.display_header_auto_shots_made_attempt: 
			col = "CAST(" + Teams.SUMMARY_AUTO_NUM_SCORED + " AS DOUBLE)" + "/" + Teams.SUMMARY_AUTO_NUM_ATTEMPT;
			break;
		case R.id.display_header_auto_shots_percent:
			col = "CAST(" + Teams.SUMMARY_AUTO_NUM_SCORED + " AS DOUBLE)" + "/" + Teams.SUMMARY_AUTO_NUM_ATTEMPT;
			break;
		case R.id.display_header_auto_total_score: 
			col = Teams.SUMMARY_AUTO_NUM_POINTS;
			break;
		default: return;
		}
		
		if (col.equals(mSortColumn)) {
			mSortColumn = col;
			mSortOrder = (mSortOrder.equals(SORT_ASC)) ? SORT_DESC : SORT_ASC;
		} else {
			mSortColumn = col;
			mSortOrder = SORT_ASC;
		}
				
		mSort = mSortColumn + mSortOrder;
		getLoaderManager().restartLoader(DISPLAY_AUTO_LOADER, null, this);
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), 
				Teams.CONTENT_URI, PROJECTION, null, null, mSort);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);	
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
	
	private static class AutoAdapter extends CursorAdapter {
		
//		private static final String TAG = "AutoAdapter";
//		private static final boolean DEBUG = false;

		private static final int[] ROW_COLOR_IDS = new int[] { 
			R.color.listview_gray, 
			R.color.listview_white 
		};
		
		private LayoutInflater mLayoutInflater;
		
		private DecimalFormat df;
		
		public AutoAdapter(Context ctx, Cursor cur, int flags) {
			super(ctx, cur, flags);
			
	        mLayoutInflater = LayoutInflater.from(ctx); 
	        df = new DecimalFormat("##.#");
		}
		
	    @Override
	    public View newView(Context context, Cursor cursor, ViewGroup parent) {
	        return mLayoutInflater.inflate(R.layout.display_teams_auto_row, parent, false);
	    }
		
		@Override
		public void bindView(View view, Context ctx, Cursor cur) {
//			if (DEBUG) Log.v(TAG, "bindView()");			
			
			// alternate row colors
			final int colorIdPos = cur.getPosition() % ROW_COLOR_IDS.length;
			int color = ctx.getResources().getColor(ROW_COLOR_IDS[colorIdPos]);
			view.setBackgroundColor(color);
			
			// use ViewHolder pattern to reduce number of times we search the
			// View hierarchy with "findViewById"
			ViewHolder holder = (ViewHolder) view.getTag();
	        if (holder == null) {
	            holder = new ViewHolder();
	            
	            // initialize TextViews
	            holder.team = (TextView) view.findViewById(R.id.display_row_auto_team);
	            holder.shotsMadeAttempt = (TextView) view.findViewById(R.id.display_row_auto_shots_made_attempt);
	            //holder.shotsAttempted = (TextView) view.findViewById(R.id.display_row_auto_shots_attempted);
	            holder.shotPercent = (TextView) view.findViewById(R.id.display_row_auto_shots_percent);
	            holder.totalScore = (TextView) view.findViewById(R.id.display_row_auto_total_score);
	            
	            // initialize column indices
	            holder.teamCol = cur.getColumnIndexOrThrow(Teams.TEAM_NUM);
	            holder.shotsMadeCol = cur.getColumnIndexOrThrow(Teams.SUMMARY_AUTO_NUM_SCORED);
	            holder.shotsAttemptedCol = cur.getColumnIndexOrThrow(Teams.SUMMARY_AUTO_NUM_ATTEMPT);
	            holder.totalScoreCol = cur.getColumnIndexOrThrow(Teams.SUMMARY_AUTO_NUM_POINTS);
	            
	            view.setTag(holder);
	        }
	        
	        holder.team.setText("" + cur.getInt(holder.teamCol));
	        
	        int made = cur.getInt(holder.shotsMadeCol);
	        int atmp = cur.getInt(holder.shotsAttemptedCol);
	       	        
	        // holder.shotsMade.setText("" + made);
	        // holder.shotsAttempted.setText("" + atmp);
	        String madeAttempt = (atmp > 0) ? ("" + made + " / " + atmp) : "N/A";
	        holder.shotsMadeAttempt.setText(madeAttempt);
	        
	        String percent = (atmp > 0) ? df.format((((double) made) / atmp) * 100)+"%" : "N/A";
	        holder.shotPercent.setText(percent);	        
	        
	        holder.totalScore.setText("" + cur.getInt(holder.totalScoreCol));
		}
				
		static class ViewHolder {
	        TextView team, shotsMadeAttempt, /*shotsAttempted,*/ shotPercent, totalScore;
	        int teamCol, shotsMadeCol, shotsAttemptedCol, /*shotPercentCol,*/ totalScoreCol;
	    }
	}
}
