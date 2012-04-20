package com.cmu.scout.fragment;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
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

public class DisplayMainFragment extends SherlockListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	
//	private static final String TAG = "DisplayMainFragment";
//	private static final boolean DEBUG = true;
	
	private static final int DISPLAY_MAIN_LOADER = 0x01;
	
	private static final String SORT_ASC = " COLLATE LOCALIZED ASC";
	private static final String SORT_DESC = " COLLATE LOCALIZED DESC";
		
	private String mSortColumn = Teams.TEAM_NUM;
	private String mSortOrder = SORT_ASC;
	private String mSort = mSortColumn + mSortOrder;

	private MainAdapter mAdapter;
	
	private OnTeamSelectedListener teamSelectedListener;
	
	public static DisplayMainFragment newInstance() {
//		if (DEBUG) Log.v(TAG, "newInstance()");
		return new DisplayMainFragment();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		if (DEBUG) Log.v(TAG, "++ ON CREATE VIEW ++");		

		View root = inflater.inflate(R.layout.display_teams_main_layout, container, false);
	
		((TextView) root.findViewById(R.id.display_header_main_team)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sortContent(v.getId());
			}
		});
		
		((TextView) root.findViewById(R.id.display_header_main_nickname)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sortContent(v.getId());
			}
		});
		
		((TextView) root.findViewById(R.id.display_header_main_wins)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sortContent(v.getId());
			}
		});
		
		((TextView) root.findViewById(R.id.display_header_main_losses)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sortContent(v.getId());
			}
		});
		
		((TextView) root.findViewById(R.id.display_header_main_total_score)).setOnClickListener(new OnClickListener() {
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
				
		getLoaderManager().initLoader(DISPLAY_MAIN_LOADER, null, this);	
		mAdapter = new MainAdapter(getActivity(), null, 0);
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
		Teams.TEAM_NAME,
		Teams.SUMMARY_NUM_WINS,
		Teams.SUMMARY_NUM_LOSSES,
		Teams.SUMMARY_TOTAL_SCORE
	};
	
	// called from the Activity
	public void sortContent(int viewId) {
//		if (DEBUG) Log.v(TAG, "onClickHandler");
		String col = null;
		
		switch(viewId) {
		case R.id.display_header_main_team: 
			col = Teams.TEAM_NUM;
			break;
		case R.id.display_header_main_nickname: 
			col = Teams.TEAM_NAME;
			break;
		case R.id.display_header_main_wins: 
			col = Teams.SUMMARY_NUM_WINS;
			break;
		case R.id.display_header_main_losses: 
			col = Teams.SUMMARY_NUM_LOSSES;
			break;
		case R.id.display_header_main_total_score: 
			col = Teams.SUMMARY_TOTAL_SCORE;
			break;
		}
		
		if (col.equals(mSortColumn)) {
			mSortColumn = col;
			mSortOrder = (mSortOrder.equals(SORT_ASC)) ? SORT_DESC : SORT_ASC;
		} else {
			mSortColumn = col;
			mSortOrder = SORT_ASC;
		}
				
		mSort = mSortColumn + mSortOrder;
		getLoaderManager().restartLoader(DISPLAY_MAIN_LOADER, null, this);
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
	
	private static class MainAdapter extends CursorAdapter {
		
//		private static final String TAG = "MainAdapter";
//		private static final boolean DEBUG = false;
		
		private static final int[] ROW_COLOR_IDS = new int[] { 
			R.color.listview_gray, 
			R.color.listview_white 
		};
		
		private LayoutInflater mLayoutInflater;
		
		public MainAdapter(Context ctx, Cursor cur, int flags) {
			super(ctx, cur, flags);
	        mLayoutInflater = LayoutInflater.from(ctx); 
		}
		
	    @Override
	    public View newView(Context context, Cursor cursor, ViewGroup parent) {
	        return mLayoutInflater.inflate(R.layout.display_teams_main_row, parent, false);
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
	            holder.team = (TextView) view.findViewById(R.id.display_row_main_team);
	            holder.nickName = (TextView) view.findViewById(R.id.display_row_main_nickname);
	            holder.wins = (TextView) view.findViewById(R.id.display_row_main_wins);
	            holder.losses = (TextView) view.findViewById(R.id.display_row_main_losses);
	            holder.totalScore = (TextView) view.findViewById(R.id.display_row_main_total_score);
	            
	            // initialize column indices
	            holder.teamCol = cur.getColumnIndexOrThrow(Teams.TEAM_NUM);
	            holder.nickNameCol = cur.getColumnIndexOrThrow(Teams.TEAM_NAME);
	            holder.winsCol = cur.getColumnIndexOrThrow(Teams.SUMMARY_NUM_WINS);
	            holder.lossesCol = cur.getColumnIndexOrThrow(Teams.SUMMARY_NUM_LOSSES);
	            holder.totalScoreCol = cur.getColumnIndexOrThrow(Teams.SUMMARY_TOTAL_SCORE);
	            
	            view.setTag(holder);
	        }
	        
	        String teamNickName = cur.getString(holder.nickNameCol);
	        
	        holder.team.setText("" + cur.getInt(holder.teamCol));
	        holder.nickName.setText((TextUtils.isEmpty(teamNickName)) ? "N/A" : teamNickName);
	        holder.wins.setText("" + cur.getInt(holder.winsCol));
	        holder.losses.setText("" + cur.getInt(holder.lossesCol));
	        holder.totalScore.setText("" + cur.getInt(holder.totalScoreCol));
		}
				
		static class ViewHolder {
	        TextView team, nickName, wins, losses, totalScore;
	        int teamCol, nickNameCol, winsCol, lossesCol, totalScoreCol;
	    }
	}
}
