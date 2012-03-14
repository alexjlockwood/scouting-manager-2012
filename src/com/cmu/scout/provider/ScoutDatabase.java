package com.cmu.scout.provider;

// TODO: IMPLEMENT THE onUpgrade() METHOD BEFORE MARKET RELEASE!!
// TODO: UNDERSTAND THE onUpgrade() METHOD!!
// TODO: MAKE SURE TO HANDLE CONFLICTS WITH UNIQUE TABLE COLUMNS!!
// TODO: TEST ON RANDOMLY CHOSEN AUTO-INCREMENT IDS TO REDUCE ANY BIAS IN TESTING STAGES!!

import android.content.Context;
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
    private static final int DATABASE_VERSION = 1;
	
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
                + Teams.HAS_KINECT + " INTEGER DEFAULT -1,"
                + Teams.CAN_CROSS + " INTEGER DEFAULT -1,"
                + Teams.CAN_PUSH_DOWN_BRIDGE + " INTEGER DEFAULT -1,"
                + Teams.STRATEGY + " INTEGER DEFAULT -1,"
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
				
				+ TeamMatches.NUM_BALANCED + " INTEGER,"
				+ TeamMatches.HOW_CROSS + " INTEGER,"
				+ TeamMatches.PICK_UP_BALLS + " INTEGER,"
				+ TeamMatches.SPEED + " INTEGER,"
				+ TeamMatches.AGILITY + " INTEGER,"
				+ TeamMatches.STRATEGY + " INTEGER,"
				+ TeamMatches.PENALTY_RISK + " TEXT,"
				+ TeamMatches.WHICH_ALLIANCE + " INTEGER,"
				+ TeamMatches.WIN_MATCH + " INTEGER,"
				+ TeamMatches.FINAL_SCORE + " INTEGER"
				// + TeamMatches.COMMENTS + " TEXT" 
				+ ");");
        
        // fillTestData(db);
	}

    /**
    * The database currently upgrades the database by destroying the existing data.
    * The real application MUST upgrade the database in place.
    */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion
				+ " to " + newVersion + ", which will destroy all old data");
		
		// TODO: IMPLEMENT THE onUpgrade() METHOD BEFORE MARKET RELEASE!!
		// TODO: UNDERSTAND THE onUpgrade() METHOD!!
		
		db.execSQL("DROP TABLE IF EXISTS " + Tables.TEAMS);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.MATCHES);
		onCreate(db);
	}
	
	/**
	 * Inputs test-data into the database when it is first created. This method
	 * is for testing purposes only.
	 */
	@SuppressWarnings("unused")
	private void fillTestData(SQLiteDatabase db) {
		db.execSQL("insert into teams (team_id, summary_num_wins, summary_num_losses, summary_total_score, summary_num_scored, summary_num_attempt, summary_num_points, summary_auto_num_scored, summary_auto_num_attempt, summary_auto_num_points) values (1, 10, 5, 100, 5, 8, 50, 4, 7, 60);");
		db.execSQL("insert into teams (team_id, summary_num_wins, summary_num_losses, summary_total_score, summary_num_scored, summary_num_attempt, summary_num_points, summary_auto_num_scored, summary_auto_num_attempt, summary_auto_num_points) values (2, 6, 5, 123, 4, 7, 44, 4, 7, 60);");
		db.execSQL("insert into teams (team_id, summary_num_wins, summary_num_losses, summary_total_score, summary_num_scored, summary_num_attempt, summary_num_points, summary_auto_num_scored, summary_auto_num_attempt, summary_auto_num_points) values (3, 3, 7, 165, 2, 4, 47, 4, 7, 60);");
		db.execSQL("insert into teams (team_id, summary_num_wins, summary_num_losses, summary_total_score, summary_num_scored, summary_num_attempt, summary_num_points, summary_auto_num_scored, summary_auto_num_attempt, summary_auto_num_points) values (4, 13, 2, 130, 6, 8, 43, 4, 7, 60);");
		db.execSQL("insert into teams (team_id, summary_num_wins, summary_num_losses, summary_total_score, summary_num_scored, summary_num_attempt, summary_num_points, summary_auto_num_scored, summary_auto_num_attempt, summary_auto_num_points) values (5, 10, 5, 99, 5, 7, 58, 4, 7, 60);");
		db.execSQL("insert into teams (team_id, summary_num_wins, summary_num_losses, summary_total_score, summary_num_scored, summary_num_attempt, summary_num_points, summary_auto_num_scored, summary_auto_num_attempt, summary_auto_num_points) values (6, 9, 2, 17, 4, 8, 55, 4, 7, 60);");
		db.execSQL("insert into teams (team_id, summary_num_wins, summary_num_losses, summary_total_score, summary_num_scored, summary_num_attempt, summary_num_points, summary_auto_num_scored, summary_auto_num_attempt, summary_auto_num_points) values (7, 8, 5, 45, 1, 6, 56, 4, 7, 60);");
		db.execSQL("insert into teams (team_id, summary_num_wins, summary_num_losses, summary_total_score, summary_num_scored, summary_num_attempt, summary_num_points, summary_auto_num_scored, summary_auto_num_attempt, summary_auto_num_points) values (8, 13, 7, 75, 2, 7, 30, 4, 7, 60);");
		db.execSQL("insert into teams (team_id, summary_num_wins, summary_num_losses, summary_total_score, summary_num_scored, summary_num_attempt, summary_num_points, summary_auto_num_scored, summary_auto_num_attempt, summary_auto_num_points) values (9, 10, 3, 87, 5, 5, 40, 4, 7, 60);");
		
		// db.execSQL("insert into teams (team_id, total_num_scored, total_auto_num_scored, score_percent, favorite_hoop, friday_rank, comments, speed, agility, strategy, pick_up_balls) values (101, 30, 50, '86%', 'med', 2, 'yay', 'slow', 'poor', 'defense', 'floor');");
		// db.execSQL("insert into teams (team_id, total_num_scored, total_auto_num_scored, score_percent, favorite_hoop, friday_rank, comments, speed, agility, strategy, pick_up_balls) values (102, 40, 54, '12%', 'high', 3, 'yay', 'medium', 'fair', 'offense', 'feeder');");
		// db.execSQL("insert into teams (team_id, total_num_scored, total_auto_num_scored, score_percent, favorite_hoop, friday_rank, comments, speed, agility, strategy, pick_up_balls) values (105, 23, 34, '87%', 'med', 4, 'yay', 'medium', 'poor', 'defense', 'floor');");
		// db.execSQL("insert into teams (team_id, total_num_scored, total_auto_num_scored, score_percent, favorite_hoop, friday_rank, comments, speed, agility, strategy, pick_up_balls) values (106, 75, 99, '100%', 'med', 5, 'yay', 'slow', 'good', 'offense', 'feeder');");
		// db.execSQL("insert into teams (team_id, total_num_scored, total_auto_num_scored, score_percent, favorite_hoop, friday_rank, comments, speed, agility, strategy, pick_up_balls) values (104, 45, 45, '0%', 'low', 6, 'yay', 'fast', 'excellent', 'both', 'feeder');");
		// db.execSQL("insert into teams (team_id) values (1)");
		//db.execSQL("insert into teams (team_id) values (2)");
		//db.execSQL("insert into teams (team_id) values (3)");
		//db.execSQL("insert into teams (team_id) values (4)");
		//db.execSQL("insert into teams (team_id) values (5)");
		//db.execSQL("insert into teams (team_id) values (6)");
		//db.execSQL("insert into teams (team_id) values (7)");
		//db.execSQL("insert into teams (team_id) values (8)");
		//db.execSQL("insert into teams (team_id) values (9)");
		db.execSQL("insert into teams (team_id) values (10)");
		db.execSQL("insert into teams (team_id) values (11)");
		db.execSQL("insert into teams (team_id) values (12)");
		db.execSQL("insert into teams (team_id) values (13)");
		db.execSQL("insert into teams (team_id) values (14)");
		db.execSQL("insert into teams (team_id) values (15)");
		db.execSQL("insert into teams (team_id) values (16)");
		db.execSQL("insert into teams (team_id) values (17)");
		db.execSQL("insert into teams (team_id) values (18)");
		db.execSQL("insert into teams (team_id) values (19)");
		db.execSQL("insert into teams (team_id) values (20)");
		db.execSQL("insert into teams (team_id) values (21)");
		db.execSQL("insert into teams (team_id) values (22)");
		db.execSQL("insert into teams (team_id) values (23)");
		db.execSQL("insert into teams (team_id) values (24)");
		db.execSQL("insert into teams (team_id) values (25)");
		db.execSQL("insert into teams (team_id) values (26)");
		db.execSQL("insert into teams (team_id) values (27)");
		db.execSQL("insert into teams (team_id) values (28)");
		db.execSQL("insert into teams (team_id) values (29)");
		db.execSQL("insert into teams (team_id) values (30)");
		db.execSQL("insert into teams (team_id) values (31)");
		db.execSQL("insert into teams (team_id) values (32)");
		db.execSQL("insert into teams (team_id) values (33)");
		db.execSQL("insert into teams (team_id) values (34)");
		db.execSQL("insert into teams (team_id) values (35)");
		db.execSQL("insert into teams (team_id) values (36)");
		db.execSQL("insert into teams (team_id) values (37)");
		db.execSQL("insert into teams (team_id) values (38)");
		db.execSQL("insert into teams (team_id) values (39)");
		db.execSQL("insert into teams (team_id) values (40)");
		db.execSQL("insert into matches (match_id) values (1);");
		db.execSQL("insert into matches (match_id) values (2);");
		db.execSQL("insert into matches (match_id) values (3);");
		db.execSQL("insert into matches (match_id) values (4);");
		db.execSQL("insert into matches (match_id) values (5);");
		db.execSQL("insert into matches (match_id) values (6);");
		/*
		db.execSQL("insert into team_matches (match_id, team_id) values (1, 101);");
		db.execSQL("insert into team_matches (match_id, team_id) values (1, 102);");
		db.execSQL("insert into team_matches (match_id, team_id) values (1, 103);");
		db.execSQL("insert into team_matches (match_id, team_id) values (1, 104);");
		db.execSQL("insert into team_matches (match_id, team_id) values (1, 105);");
		db.execSQL("insert into team_matches (match_id, team_id) values (1, 106);");
		db.execSQL("insert into team_matches (match_id, team_id) values (2, 101);");
		db.execSQL("insert into team_matches (match_id, team_id) values (2, 102);");
		db.execSQL("insert into team_matches (match_id, team_id) values (2, 103);");
		db.execSQL("insert into team_matches (match_id, team_id) values (2, 104);");
		db.execSQL("insert into team_matches (match_id, team_id) values (2, 105);");
		db.execSQL("insert into team_matches (match_id, team_id) values (2, 106);");
		db.execSQL("insert into team_matches (match_id, team_id) values (3, 101);");
		db.execSQL("insert into team_matches (match_id, team_id) values (3, 102);");
		db.execSQL("insert into team_matches (match_id, team_id) values (3, 103);");
		db.execSQL("insert into team_matches (match_id, team_id) values (4, 101);");
		db.execSQL("insert into team_matches (match_id, team_id) values (4, 102);");
		db.execSQL("insert into team_matches (match_id, team_id) values (4, 103);");
		 */
	}
}
