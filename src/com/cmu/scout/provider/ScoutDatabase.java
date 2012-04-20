package com.cmu.scout.provider;

// TODO: IMPLEMENT THE onUpgrade() METHOD BEFORE MARKET RELEASE!!
// TODO: UNDERSTAND THE onUpgrade() METHOD!!
// TODO: MAKE SURE TO HANDLE CONFLICTS WITH UNIQUE TABLE COLUMNS!!
// TODO: TEST ON RANDOMLY CHOSEN AUTO-INCREMENT IDS TO REDUCE ANY BIAS IN TESTING STAGES!!

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cmu.scout.provider.ScoutContract.Matches;
import com.cmu.scout.provider.ScoutContract.TeamMatches;
import com.cmu.scout.provider.ScoutContract.Teams;

/**
 * This class is responsible for the creation of the database. The ScoutProvider
 * class allows access to the data stored in the database.
 */
public class ScoutDatabase extends SQLiteOpenHelper {
	
    private static final String TAG = "ScoutDatabase";

    private static final String DATABASE_NAME = "scout.db";
    private static final int DATABASE_VERSION = 2;
	
    // TODO: figure out whether I should be using "default -1" or just "null"
    
    /** SQLite table names */
	interface Tables {
		String TEAMS = "teams";
		String TEAM_MATCHES = "team_matches";
		String MATCHES = "matches";
	}
	
    /** {@code REFERENCES} clauses. */
    private interface References {
        String TEAM_ID =  "REFERENCES " + Tables.TEAMS   + "(" + Teams._ID +   ")";
        String MATCH_ID = "REFERENCES " + Tables.MATCHES + "(" + Matches._ID + ")";
    }
    
    public ScoutDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		// TODO: HANDLE THE "UNIQUE" CONFLICT!!
		// TODO: Update these creation statements to reflect new column entries!
		// TODO: Update the column types (change TEXT to INTEGER for radio buttons,
		// as well as all of the "ID"s... I have no idea why I wanted to do this with "TEXT".
		
		db.execSQL("CREATE TABLE " + Tables.TEAMS + " ("
                + Teams._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                
                /* General */
                + Teams.TEAM_NUM + " INTEGER NOT NULL,"
                
                /* Pre-scouting */
                + Teams.TEAM_NAME + " TEXT,"
                + Teams.TEAM_PHOTO + " TEXT,"
                + Teams.DRIVE_SYSTEM + " INTEGER DEFAULT -1,"
                + Teams.WHEELS + " INTEGER DEFAULT -1,"
                + Teams.HAS_AUTONOMOUS + " INTEGER DEFAULT -1,"
                + Teams.PREFERRED_START + " INTEGER DEFAULT -1,"
                + Teams.SHOOT_FROM_WHERE + " INTEGER DEFAULT -1,"
                + Teams.HAS_KINECT + " INTEGER DEFAULT -1,"
                + Teams.CAN_CROSS + " INTEGER DEFAULT -1,"
                + Teams.CAN_PUSH_DOWN_BRIDGE + " INTEGER DEFAULT -1,"
                + Teams.STRATEGY + " INTEGER DEFAULT -1,"
                + Teams.FRIDAY_RANK + " INTEGER DEFAULT -1, "
                + Teams.COMMENTS + " TEXT,"
                
                /* Pre-calculated, match-dependent data */
                + Teams.SUMMARY_NUM_SCORED + " INTEGER DEFAULT 0,"
                + Teams.SUMMARY_NUM_ATTEMPT + " INTEGER DEFAULT 0,"
                + Teams.SUMMARY_NUM_POINTS + " INTEGER DEFAULT 0,"
                
                // + Teams.SUMMARY_NUM_SCORED_LOW + " INTEGER DEFAULT 0,"
                // + Teams.SUMMARY_NUM_SCORED_MED + " INTEGER DEFAULT 0,"
                // + Teams.SUMMARY_NUM_SCORED_HIGH + " INTEGER DEFAULT 0,"
                // + Teams.SUMMARY_NUM_ATTEMPT_LOW + " INTEGER DEFAULT 0,"
                // + Teams.SUMMARY_NUM_ATTEMPT_MED + " INTEGER DEFAULT 0,"
                // + Teams.SUMMARY_NUM_ATTEMPT_HIGH + " INTEGER DEFAULT 0,"
                
                + Teams.SUMMARY_AUTO_NUM_SCORED + " INTEGER DEFAULT 0,"
                + Teams.SUMMARY_AUTO_NUM_ATTEMPT + " INTEGER DEFAULT 0,"
                + Teams.SUMMARY_AUTO_NUM_POINTS + " INTEGER DEFAULT 0,"
                
                // + Teams.SUMMARY_AUTO_NUM_SCORED_LOW + " INTEGER DEFAULT 0,"
                // + Teams.SUMMARY_AUTO_NUM_SCORED_MED + " INTEGER DEFAULT 0,"
                // + Teams.SUMMARY_AUTO_NUM_SCORED_HIGH + " INTEGER DEFAULT 0,"
                // + Teams.SUMMARY_AUTO_NUM_ATTEMPT_LOW + " INTEGER DEFAULT 0,"
                // + Teams.SUMMARY_AUTO_NUM_ATTEMPT_MED + " INTEGER DEFAULT 0,"
                // + Teams.SUMMARY_AUTO_NUM_ATTEMPT_HIGH + " INTEGER DEFAULT 0,"
                
                // + Teams.SUMMARY_SPEED + " INTEGER DEFAULT -1,"
                // + Teams.SUMMARY_CAN_BALANCE + " INTEGER DEFAULT -1,"
                
                + Teams.SUMMARY_NUM_WINS + " INTEGER DEFAULT 0,"
                + Teams.SUMMARY_NUM_LOSSES + " INTEGER DEFAULT 0,"
                + Teams.SUMMARY_TOTAL_SCORE + " INTEGER DEFAULT 0,"    
                
                + "UNIQUE (" + Teams.TEAM_NUM + ") ON CONFLICT REPLACE);");
		
        db.execSQL("CREATE TABLE " + Tables.MATCHES + " ("
                + Matches._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"              
                + Matches.MATCH_NUM + " INTEGER NOT NULL,"
                // + Matches.TIME + " TEXT,"
                // + Matches.DATE + " TEXT,"
                // + Matches.COMMENTS + " TEXT,"
                + "UNIQUE (" + Matches.MATCH_NUM + ") ON CONFLICT REPLACE);");
        
        
        // TODO: check all of these!!! make sure that there is no
        // "text" vs "integer" confusion!!
        // TODO: should I be referencing the auto-increment id or the team/match id?
		db.execSQL("CREATE TABLE " + Tables.TEAM_MATCHES + " (" 
				+ TeamMatches._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				
				+ TeamMatches.MATCH_ID + " INTEGER NOT NULL " + References.MATCH_ID + ","
				+ TeamMatches.TEAM_ID + " INTEGER NOT NULL " + References.TEAM_ID + ","
				
				+ TeamMatches.AUTO_NUM_SCORED_LOW + " INTEGER,"
				+ TeamMatches.AUTO_NUM_SCORED_MED + " INTEGER,"
				+ TeamMatches.AUTO_NUM_SCORED_HIGH + " INTEGER,"
				+ TeamMatches.AUTO_NUM_ATTEMPT_LOW + " INTEGER,"
				+ TeamMatches.AUTO_NUM_ATTEMPT_MED + " INTEGER,"
				+ TeamMatches.AUTO_NUM_ATTEMPT_HIGH + " INTEGER,"
				
				+ TeamMatches.NUM_SCORED_LOW + " INTEGER,"
				+ TeamMatches.NUM_SCORED_MED + " INTEGER,"
				+ TeamMatches.NUM_SCORED_HIGH + " INTEGER,"		
				+ TeamMatches.NUM_ATTEMPT_LOW + " INTEGER,"
				+ TeamMatches.NUM_ATTEMPT_MED + " INTEGER,"
				+ TeamMatches.NUM_ATTEMPT_HIGH + " INTEGER,"
				
				+ TeamMatches.NUM_BALANCED + " INTEGER DEFAULT -1,"
				+ TeamMatches.HOW_CROSS + " INTEGER DEFAULT -1,"
				+ TeamMatches.PICK_UP_BALLS + " INTEGER DEFAULT -1,"
				+ TeamMatches.SPEED + " INTEGER DEFAULT -1,"
				+ TeamMatches.AGILITY + " INTEGER DEFAULT -1,"
				+ TeamMatches.STRATEGY + " INTEGER DEFAULT -1,"
				+ TeamMatches.PENALTY_RISK + " TEXT,"
				+ TeamMatches.WHICH_ALLIANCE + " INTEGER DEFAULT -1,"
				+ TeamMatches.WIN_MATCH + " INTEGER DEFAULT -1,"
				+ TeamMatches.FINAL_SCORE + " INTEGER,"
				+ TeamMatches.COMMENTS + " TEXT,"
				+ TeamMatches.DID_NOTHING + " INTEGER DEFAULT -1" + ");");
	}

    /**
    * The database currently upgrades the database by destroying the existing data.
    * The real application MUST upgrade the database in place.
    */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion
				+ " to " + newVersion + ".");
		
		if (oldVersion < newVersion) {
			// Upgrade the database.
			switch (oldVersion) {
			
			case 1:
				// Upgrade the database from version 1 to 2.

				// SQLite only allows adding one column at a time, so perform
				//   call "execSQL" three times (once for each new column).
				try {
					// Add comments column to TeamMatches table.
                    db.execSQL("ALTER TABLE " + Tables.TEAM_MATCHES + " ADD COLUMN " + TeamMatches.COMMENTS + " TEXT;");
                    
                    // TODO: Add "did nothing?" column to TeamMatches table.
                    db.execSQL("ALTER TABLE " + Tables.TEAM_MATCHES + " ADD COLUMN " + TeamMatches.DID_NOTHING + " INTEGER DEFAULT -1;");
                    
                    // TODO: Add "shoots from where" column to Teams table.
                    db.execSQL("ALTER TABLE " + Tables.TEAMS + " ADD COLUMN " + Teams.SHOOT_FROM_WHERE + " INTEGER DEFAULT -1;");  
				} catch (SQLException e) {
                    Log.e(TAG, "Error executing SQL statement: ", e);   
				}
				break;
				
				// Remove this "break" statement when adding new upgrades.
				// This will allow for the upgrades to "fall-through".
				
			default: 
				Log.w(TAG, "Unknown version: " + oldVersion + ". Creating new database.");
				db.execSQL("DROP TABLE IF EXISTS " + Tables.TEAMS);
				db.execSQL("DROP TABLE IF EXISTS " + Tables.MATCHES);
				db.execSQL("DROP TABLE IF EXISTS " + Tables.TEAM_MATCHES);
				onCreate(db);	
			}		
		} else {
			// Then downgrade version. We don't need to worry about this.
		}
	}
}
