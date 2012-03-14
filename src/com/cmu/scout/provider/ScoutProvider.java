package com.cmu.scout.provider;

import java.util.Arrays;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.cmu.scout.provider.ScoutContract.Matches;
import com.cmu.scout.provider.ScoutContract.TeamMatches;
import com.cmu.scout.provider.ScoutContract.Teams;
import com.cmu.scout.provider.ScoutDatabase.Tables;

/**
 * The ContentProvider for the Scouting application. This class serves as an
 * abstraction layer over the SQLiteDatabase.
 * 
 * This class is not responsible for deep-insertions, deletions, queries, etc. 
 * The client must perform such actions on their own.
 */
public class ScoutProvider extends ContentProvider {
	private static final String TAG = "ScoutProvider";
	private static final boolean DEBUG = true;
	
	private ScoutDatabase mOpenHelper;
	
	private static final UriMatcher sUriMatcher;
	
	// Teams table.
	private static final int TEAMS = 100;
	private static final int TEAMS_ID = 101;
	
	// Matches table.
	private static final int MATCHES = 200;
	private static final int MATCHES_ID = 201;
	
	// TeamMatch table.
	private static final int MATCHES_TEAMS_ID = 300;
	private static final int MATCHES_ID_TEAMS_ID = 301;
	
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		
		sUriMatcher.addURI(ScoutContract.AUTHORITY, "teams", TEAMS);
		sUriMatcher.addURI(ScoutContract.AUTHORITY, "teams/#", TEAMS_ID);
		
		sUriMatcher.addURI(ScoutContract.AUTHORITY, "matches", MATCHES);
		sUriMatcher.addURI(ScoutContract.AUTHORITY, "matches/#", MATCHES_ID);
		
		sUriMatcher.addURI(ScoutContract.AUTHORITY, "matches/teams/#", MATCHES_TEAMS_ID);
		sUriMatcher.addURI(ScoutContract.AUTHORITY, "matches/#/teams/#", MATCHES_ID_TEAMS_ID);
	}
	
	@Override
	public boolean onCreate() {
		final Context context = getContext();
		mOpenHelper = new ScoutDatabase(context);
		return true;
	}
	
	@Override
	public String getType(Uri uri) {
		final int match = sUriMatcher.match(uri);
	    switch (match) {
	    	case TEAMS: 
	    		return Teams.CONTENT_TYPE;
	    	case TEAMS_ID:
	    		return Teams.CONTENT_ITEM_TYPE;
	    	case MATCHES:
	    		return Matches.CONTENT_TYPE;
	    	case MATCHES_ID:
	    		return Matches.CONTENT_TYPE;
	    	case MATCHES_TEAMS_ID:
	    		return Matches.CONTENT_TYPE;
	    	case MATCHES_ID_TEAMS_ID:
	    		return Matches.CONTENT_ITEM_TYPE;
	    	default:
	    		throw new UnsupportedOperationException("Unknown URI: " + uri);
	    }
	}
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
        if (DEBUG) Log.v(TAG, "delete(uri=" + uri + ")");
		
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int rowsAffected = 0;
		String finalWhere;

		final int match = sUriMatcher.match(uri);
		switch (match) {
		// Delete a selection of teams the teams table.
		case TEAMS:
			rowsAffected = db.delete(Tables.TEAMS, where, whereArgs);
			break;
		// Delete a single team from the teams table.
		case TEAMS_ID:
			finalWhere = Teams._ID + " = " + uri.getLastPathSegment();			
			if (!TextUtils.isEmpty(where)) {
				finalWhere = finalWhere + " AND " + where;
			}
			rowsAffected = db.delete(Tables.TEAMS, finalWhere, whereArgs);
			break;
		// Delete a selection of matches the matches table.
		case MATCHES:
			rowsAffected = db.delete(Tables.MATCHES, where, whereArgs);
			break;
		// Delete a single match from the matches table.
		case MATCHES_ID:
			finalWhere = Matches._ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(where)) {
				finalWhere = finalWhere + " AND " + where;
			}
			rowsAffected = db.delete(Tables.MATCHES, finalWhere, whereArgs);
			break;
		// Delete a team's match data from the team_matches table.
		case MATCHES_TEAMS_ID:
			finalWhere = TeamMatches.TEAM_ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(where)) {
				finalWhere = finalWhere + " AND " + where;
			}
			rowsAffected = db.delete(Tables.TEAM_MATCHES, finalWhere, whereArgs);
			break;
		// Delete a team's specific match from the team_matches table.
		case MATCHES_ID_TEAMS_ID:
			finalWhere = TeamMatches.MATCH_ID + " = " + uri.getPathSegments().get(1) + " AND "
					   + TeamMatches.TEAM_ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(where)) {
				finalWhere = finalWhere + " AND " + where;
			}
			Log.v(TAG, finalWhere);
			rowsAffected = db.delete(Tables.TEAM_MATCHES, finalWhere, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return rowsAffected;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
        if (DEBUG) Log.v(TAG, "insert(uri=" + uri + ", values=" + values.toString() + ")");
		
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long newId; Uri newUri;

        final int match = sUriMatcher.match(uri);
		switch (match) {
		// Insert a new team into the teams table.
		case TEAMS:
			newId = db.insert(Tables.TEAMS, null, values);
			if (newId > 0) {
				newUri = Teams.buildTeamIdUri("" + newId);
				getContext().getContentResolver().notifyChange(newUri, null);
				return newUri;
			} else {
				throw new SQLException("Failed to insert row into " + uri);
			}
		// Insert a new match into the matches table.
		case MATCHES:
			newId = db.insert(Tables.MATCHES, null, values);
			if (newId > 0) {
				newUri = Matches.buildMatchIdUri("" + newId);
				getContext().getContentResolver().notifyChange(newUri, null);
				return newUri;
			} else {
				throw new SQLException("Failed to insert row into " + uri);
			}
		// Insert a new match-item into the team_matches table
		case MATCHES_TEAMS_ID:
			newId = db.insert(Tables.TEAM_MATCHES, null, values);
			if (newId > 0) {
				newUri = Matches.buildMatchTeamIdUri("" + newId);
				getContext().getContentResolver().notifyChange(newUri, null);
				return newUri;
			} else {
				throw new SQLException("Failed to insert row into " + uri);
			}
		default:
			throw new UnsupportedOperationException("Unknown or Invalid URI " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String where, String[] whereArgs, String sortOrder) {
        if (DEBUG) {
        	Log.v(TAG, "query(uri=" + uri + ", proj=" + Arrays.toString(projection) + 
        		", where=" + where + ", whereArgs=" + Arrays.toString(whereArgs) + 
        		", sortOrder=" + sortOrder + ")");
        }

        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        
        final int match = sUriMatcher.match(uri);
        switch(match) {
        // Get a cursor over a selection of teams in the teams table.
        case TEAMS:
        	qb.setTables(Tables.TEAMS);
        	break;
        // Get a cursor over a single team in the teams table.
        case TEAMS_ID:
        	qb.setTables(Tables.TEAMS);
        	qb.appendWhere(Teams._ID + " = " + uri.getLastPathSegment());
        	break;
        // Get a cursor over a selection of matches in the matches table.
        case MATCHES:
        	qb.setTables(Tables.MATCHES);
        	break;
        // Get a cursor over a single match in the matches table.
        case MATCHES_ID:
        	qb.setTables(Tables.MATCHES);
        	qb.appendWhere(Matches._ID + " = " + uri.getLastPathSegment());
        	break;
        // Get a cursor over a team's matches in the team_matches table.
        case MATCHES_TEAMS_ID:
        	qb.setTables(Tables.TEAM_MATCHES);
        	qb.appendWhere(TeamMatches.TEAM_ID + " = " + uri.getLastPathSegment());
        	break;
        // Get a cursor over a team's specific match in the team_matches table.
        case MATCHES_ID_TEAMS_ID:
        	qb.setTables(Tables.TEAM_MATCHES);
        	qb.appendWhere(TeamMatches.MATCH_ID + " = " + uri.getPathSegments().get(1) + " AND " + 
        				   TeamMatches.TEAM_ID +  " = " + uri.getLastPathSegment());
        	break;
        default:
        	throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        
        final Cursor cur = qb.query(mOpenHelper.getReadableDatabase(), projection, 
        		where, whereArgs, null, null, sortOrder);
        cur.setNotificationUri(getContext().getContentResolver(), uri);
        return cur;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        if (DEBUG) Log.v(TAG, "update(uri=" + uri + ", values=" + values.toString() + ")");
		
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();		
		String finalWhere;
		int rowsAffected;

		switch (sUriMatcher.match(uri)) {
		// Update a selection of teams in the teams table.
		case TEAMS:
			rowsAffected = db.update(Tables.TEAMS, values, where, whereArgs);
			break;
		// Update a single team in the teams table.
		case TEAMS_ID:
			finalWhere = Teams._ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(where)) {
				finalWhere = finalWhere + " AND " + where;
			}
			rowsAffected = db.update(Tables.TEAMS, values, finalWhere, whereArgs);
			break;
		// Update a selection of matches in the matches table.
		case MATCHES:
			rowsAffected = db.update(Tables.MATCHES, values, where, whereArgs);
			break;
		// Update a single match in the matches table.
		case MATCHES_ID:
			finalWhere = Matches._ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(where)) {
				finalWhere = finalWhere + " AND " + where;
			}
			rowsAffected = db.update(Tables.MATCHES, values, finalWhere, whereArgs);
			break;
		// Update a team's match data from the team_matches table.
		case MATCHES_TEAMS_ID:
			finalWhere = TeamMatches.TEAM_ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(where)) {
				finalWhere = finalWhere + " AND " + where;
			}
			rowsAffected = db.update(Tables.TEAM_MATCHES, values, finalWhere, whereArgs);
			break;
		// Update a team's specific match from the team_matches table.
		case MATCHES_ID_TEAMS_ID:
			finalWhere = TeamMatches.MATCH_ID + " = " + uri.getPathSegments().get(1) + " AND " 
						   + TeamMatches.TEAM_ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(where)) {
				finalWhere = finalWhere + " AND " + where;
			}
			rowsAffected = db.update(Tables.TEAM_MATCHES, values, finalWhere, whereArgs);
			break;
		default:
			throw new UnsupportedOperationException("Unknown or Invalid URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return rowsAffected;
	}
}
