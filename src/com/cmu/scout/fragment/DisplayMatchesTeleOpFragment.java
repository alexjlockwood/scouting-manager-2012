package com.cmu.scout.fragment;

import android.content.Context;
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
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;

import com.cmu.scout.R;
import com.cmu.scout.provider.ScoutContract.TeamMatches;
import com.cmu.scout.ui.DisplayPagerMatchesActivity;

public class DisplayMatchesTeleOpFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final String TAG = "DisplayMatchesTeleopFragment";
	private static final boolean DEBUG = true;
	
	private static final String TEAM_MATCHES_URI_STORAGE_KEY = "CurrentTeamMatchesUri";
	
	private static final int DISPLAY_MATCHES_TELEOP_LOADER = 0x01;
	
	private static final String DEFAULT_SORT = TeamMatches.MATCH_ID + " COLLATE LOCALIZED ASC";

	private AutoAdapter mAdapter;
	
	//private int mTeamId;
	
	private Bundle mArgs;
	
	public static DisplayMatchesTeleOpFragment newInstance(Uri teamMatchesUri) {
		if (DEBUG) Log.v(TAG, "newInstance()");
        DisplayMatchesTeleOpFragment f = new  DisplayMatchesTeleOpFragment();

        Bundle args = new Bundle();
        args.putString(TEAM_MATCHES_URI_STORAGE_KEY, teamMatchesUri.toString());
        f.setArguments(args);
        
        return f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "++ ON CREATE VIEW ++");		
		return inflater.inflate(R.layout.display_team_matches_teleop_layout, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DEBUG) Log.v(TAG, "+ ON ACTIVITY CREATED +");
		
		mArgs = getArguments();
		//mTeamId = Integer.valueOf(Uri.parse(args.getString(TEAM_MATCHES_URI_STORAGE_KEY)).getLastPathSegment());
		
		registerForContextMenu(getListView());
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		// temporary work around... need to restart loader because something is messed up in the database
		// that is preventing changes from being loaded on its own
		getLoaderManager().restartLoader(DISPLAY_MATCHES_TELEOP_LOADER, mArgs, this);	
		mAdapter = new AutoAdapter(getActivity(), null, 0);
		setListAdapter(mAdapter);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getActivity().getMenuInflater().inflate(R.menu.display_team_matches_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		switch (item.getItemId()) {
		case R.id.menu_match_edit_data:
			((DisplayPagerMatchesActivity) getActivity()).onTeamMatchEdit(info.id);
			return true;
		case R.id.menu_delete_team_match:
			((DisplayPagerMatchesActivity) getActivity()).onTeamMatchDelete(info.id);
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	private static final String[] PROJECTION = { 
		TeamMatches._ID,
		TeamMatches.MATCH_ID, 
		TeamMatches.NUM_SCORED_HIGH,
		TeamMatches.NUM_SCORED_MED,
		TeamMatches.NUM_SCORED_LOW,
		TeamMatches.NUM_ATTEMPT_HIGH,
		TeamMatches.NUM_ATTEMPT_MED,
		TeamMatches.NUM_ATTEMPT_LOW
	};
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {		
		final Uri teamMatchesUri = Uri.parse(args.getString(TEAM_MATCHES_URI_STORAGE_KEY));
		return new CursorLoader(getActivity(), 
				teamMatchesUri, PROJECTION, null, null, DEFAULT_SORT);
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
		
		private static final String TAG = "AutoAdapter";
		private static final boolean DEBUG = false;

		private static final int[] ROW_COLOR_IDS = new int[] { 
			R.color.listview_gray, 
			R.color.listview_white 
		};
		
		private LayoutInflater mLayoutInflater;
		
		public AutoAdapter(Context ctx, Cursor cur, int flags) {
			super(ctx, cur, flags);
			
	        mLayoutInflater = LayoutInflater.from(ctx); 
		}
		
	    @Override
	    public View newView(Context context, Cursor cursor, ViewGroup parent) {
	        return mLayoutInflater.inflate(R.layout.display_team_matches_teleop_row, parent, false);
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
	            holder.match = (TextView) view.findViewById(R.id.display_row_teleop_match);
	            holder.lowShots = (TextView) view.findViewById(R.id.display_row_teleop_low_shots_made_attempt);
	            holder.medShots = (TextView) view.findViewById(R.id.display_row_teleop_med_shots_made_attempt);
	            holder.highShots = (TextView) view.findViewById(R.id.display_row_teleop_high_shots_made_attempt);
	            
	            // initialize column indices
	            // holder.matchCol = cur.getColumnIndexOrThrow(TeamMatches.MATCH_ID);
	            holder.lowShotsMadeCol = cur.getColumnIndexOrThrow(TeamMatches.NUM_SCORED_LOW);
	            holder.lowShotsAttemptCol = cur.getColumnIndexOrThrow(TeamMatches.NUM_ATTEMPT_LOW);
	            holder.medShotsMadeCol = cur.getColumnIndexOrThrow(TeamMatches.NUM_SCORED_MED);
	            holder.medShotsAttemptCol = cur.getColumnIndexOrThrow(TeamMatches.NUM_ATTEMPT_MED);
	            holder.highShotsMadeCol = cur.getColumnIndexOrThrow(TeamMatches.NUM_SCORED_HIGH);
	            holder.highShotsAttemptCol = cur.getColumnIndexOrThrow(TeamMatches.NUM_ATTEMPT_HIGH);
	            
	            view.setTag(holder);
	        }
	        
	        holder.match.setText("" + (cur.getPosition()+1));
	        
	        int lowDenom = cur.getInt(holder.lowShotsAttemptCol);
	        int medDenom = cur.getInt(holder.medShotsAttemptCol);
	        int highDenom = cur.getInt(holder.highShotsAttemptCol);
	        
	        String lowRow = (lowDenom != 0) ? "" + cur.getInt(holder.lowShotsMadeCol) + " / " + lowDenom : "N/A";
	        String medRow = (medDenom != 0) ? "" + cur.getInt(holder.medShotsMadeCol) + " / " + medDenom : "N/A";
	        String highRow = (highDenom != 0) ? "" + cur.getInt(holder.highShotsMadeCol) + " / " + highDenom : "N/A";
	        
	        holder.lowShots.setText(lowRow);
	        holder.medShots.setText(medRow);
	        holder.highShots.setText(highRow);
		}
				
		static class ViewHolder {
	        TextView match, lowShots, medShots, highShots;
	        int /*matchCol,*/ lowShotsMadeCol, lowShotsAttemptCol, medShotsMadeCol, medShotsAttemptCol, highShotsMadeCol, highShotsAttemptCol;
	    }
	}
}
