package com.cmu.scout.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;

import com.cmu.scout.R;
import com.cmu.scout.provider.ScoutContract.Matches;
import com.cmu.scout.provider.ScoutContract.Teams;
import com.cmu.scout.ui.OnTeamSelectedListener;

public class DisplayScoutTwoFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final String TAG = "DisplayScoutTwoFragment";
	private static final boolean DEBUG = true;
	
	private static final int DISPLAY_SCOUT_TWO_LOADER = 0x01;
	
	private static final String SORT_ASC = " COLLATE LOCALIZED ASC";
	private static final String SORT_DESC = " COLLATE LOCALIZED DESC";
		
	private String mSortColumn = Teams.TEAM_NUM;
	private String mSortOrder = SORT_ASC;
	private String mSort = mSortColumn + mSortOrder;

	private MainAdapter mAdapter;
	
	private OnTeamSelectedListener teamSelectedListener;
	
	public static DisplayScoutTwoFragment newInstance() {
		if (DEBUG) Log.v(TAG, "newInstance()");
		return new DisplayScoutTwoFragment();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "++ ON CREATE VIEW ++");		

		View root = inflater.inflate(R.layout.display_teams_scout_two_layout, container, false);
	
		((TextView) root.findViewById(R.id.display_header_scout_two_team)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sortContent(v.getId());
			}
		});
		
		((TextView) root.findViewById(R.id.display_header_scout_two_plays_auto)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sortContent(v.getId());
			}
		});
		
		((TextView) root.findViewById(R.id.display_header_scout_two_push_down_bridge)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sortContent(v.getId());
			}
		});
		
		((TextView) root.findViewById(R.id.display_header_scout_two_strategy)).setOnClickListener(new OnClickListener() {
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
		if (DEBUG) Log.v(TAG, "+ ON ACTIVITY CREATED +");
				
		getLoaderManager().initLoader(DISPLAY_SCOUT_TWO_LOADER, null, this);	
		mAdapter = new MainAdapter(getActivity(), null, 0);
		setListAdapter(mAdapter);
		
		registerForContextMenu(getListView());
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (DEBUG) Log.v(TAG, "+ ON ATTACH +");
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

		switch (item.getItemId()) {
		case R.id.menu_display_team_matches:
			Uri uri = Matches.buildMatchTeamIdUri("" + info.id);
			teamSelectedListener.onTeamSelected(uri);
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	private static final String[] PROJECTION = { 
		Teams._ID,
		Teams.TEAM_NUM,
		Teams.HAS_AUTONOMOUS,
		Teams.CAN_PUSH_DOWN_BRIDGE,
		Teams.STRATEGY
	};
	
	// called from the Activity
	public void sortContent(int viewId) {
		if (DEBUG) Log.v(TAG, "onClickHandler");
		String col = null;
		
		switch(viewId) {
		case R.id.display_header_scout_two_team: 
			col = Teams.TEAM_NUM;
			break;
		case R.id.display_header_scout_two_plays_auto: 
			col = Teams.HAS_AUTONOMOUS;
			break;
		case R.id.display_header_scout_two_push_down_bridge: 
			col = Teams.CAN_PUSH_DOWN_BRIDGE;
			break;
		case R.id.display_header_scout_two_strategy: 
			col = Teams.STRATEGY;
			break;
		}
		
		if (col.equals(mSortColumn)) {
			mSortColumn = col;
			mSortOrder = (mSortOrder.equals(SORT_ASC)) ? SORT_DESC : SORT_ASC;
		} else {
			mSortColumn = col;
			mSortOrder = SORT_ASC;
		}
		
		if (DEBUG) Log.v(TAG, "sorting: " + mSort + " --> " + (mSortColumn+mSortOrder));
		
		mSort = mSortColumn + mSortOrder;
		getLoaderManager().restartLoader(DISPLAY_SCOUT_TWO_LOADER, null, this);
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
		
		private static final String TAG = "MainAdapter";
		private static final boolean DEBUG = false;
		
		private static final int[] ROW_COLOR_IDS = new int[] { 
			R.color.listview_gray, 
			R.color.listview_white 
		};
		
		private LayoutInflater mLayoutInflater;
		
		private String[] autoStrings;
		private String[] bridgeStrings;
		private String[] strategyStrings;
		
		public MainAdapter(Context ctx, Cursor cur, int flags) {
			super(ctx, cur, flags);
			
	        mLayoutInflater = LayoutInflater.from(ctx); 
		
	        final Resources res = ctx.getResources();
	        strategyStrings = res.getStringArray(R.array.strategy_option);
	        autoStrings = new String[] { "N/A", "No", "Yes" };
	        bridgeStrings = new String[] { "N/A", "No", "Yes" };
	    }
		
	    @Override
	    public View newView(Context context, Cursor cursor, ViewGroup parent) {
	        return mLayoutInflater.inflate(R.layout.display_teams_scout_two_row, parent, false);
	    }
		
		@Override
		public void bindView(View view, Context ctx, Cursor cur) {
			if (DEBUG) Log.v(TAG, "bindView()");			

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
	            holder.team = (TextView) view.findViewById(R.id.display_row_scout_two_team);
	            holder.playsAuto = (TextView) view.findViewById(R.id.display_row_scout_two_plays_auto);
	            holder.pushDownBridge = (TextView) view.findViewById(R.id.display_row_scout_two_push_down_bridge);
	            holder.strategy = (TextView) view.findViewById(R.id.display_row_scout_two_strategy);
	            
	            // initialize column indices
	            holder.teamCol = cur.getColumnIndexOrThrow(Teams.TEAM_NUM);
	            holder.playsAutoCol = cur.getColumnIndexOrThrow(Teams.HAS_AUTONOMOUS);
	            holder.pushDownBridgeCol = cur.getColumnIndexOrThrow(Teams.CAN_PUSH_DOWN_BRIDGE);
	            holder.strategyCol = cur.getColumnIndexOrThrow(Teams.STRATEGY);
	            
	            view.setTag(holder);
	        }
	        
	        int playsAutoVal = cur.getInt(holder.playsAutoCol)+1;
	        int pushDownBridgeVal = cur.getInt(holder.pushDownBridgeCol)+1;
	        int strategyStringVal = cur.getInt(holder.strategyCol)+1;
	        
	        holder.team.setText("" + cur.getInt(holder.teamCol));
	        holder.playsAuto.setText("" + autoStrings[playsAutoVal]);
	        holder.pushDownBridge.setText("" + bridgeStrings[pushDownBridgeVal]);
	        holder.strategy.setText("" + strategyStrings[strategyStringVal]);
		}
				
		static class ViewHolder {
	        TextView team, playsAuto, pushDownBridge, strategy;
	        int teamCol, playsAutoCol, pushDownBridgeCol, strategyCol;
	    }
	}
}
