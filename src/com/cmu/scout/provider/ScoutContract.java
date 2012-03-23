package com.cmu.scout.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines a contract between the Scout content provider and its clients. A contract defines the
 * information that a client needs to access the provider as one or more data tables. A contract
 * is a public, non-extendable (final) class that contains constants defining column names and
 * URIs. A well-written client depends only on the constants in the contract.
 */
public final class ScoutContract {

	// This class cannot be instantiated.
	private ScoutContract() { }
	
	/** Column names for teams table */
	interface TeamsColumns {
		
		/* General */
		
		/**
		 * Team's unique id
		 * 
		 * <P>Type: TEXT NOT NULL (UNIQUE ON CONFLICT REPLACE)</P>
		 */
		String TEAM_NUM = "team_num";
		
		/* Pre-scouting */
		
		/**
		 * Team's nickname
		 * 
		 * <P>Type: TEXT</P>
		 */
		String TEAM_NAME = "team_name";
		
		/**
		 * Uri address for the team's photo
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		String TEAM_PHOTO = "team_photo";
		
		/**
		 * Team's drive system
		 * 
		 * <P>
		 * Type: INTEGER<br>
		 * Default: -1<br>
		 * </P>
		 * 
		 * <P>
		 * Possible values:<br>
		 * 0: 6-wheel drop<br>
		 * 1: Mecanum<br>
		 * 2: Omni<br>
		 * 3: Swerve<br>
		 * 4: 4-wheel drive<br>
		 * 5: 8-wheel<br>
		 * 6: Slide<br>
		 * 7: Other<br>
		 * -1: No value<br>
		 * </P>
		 */
		String DRIVE_SYSTEM = "drive_system";
		
		/**
		 * Team's wheels
		 * 
		 * <P>
		 * Type: INTEGER
		 * Default: -1
		 * </P>
		 * 
		 * <P>
		 * Possible values:<br>
		 * 0: Plaction<br>
		 * 1: Metal with traction<br>
		 * 2: Omni-wheels<br>
		 * 3: Mecanum<br>
		 * 4: Kit of parts (KoP)<br>
		 * 5: Pneumatic<br>
		 * 6: Tank tread<br>
		 * 7: Other<br>
		 * -1: No value<br>
		 * </P>
		 */
		String WHEELS = "wheels";
		
		/**
		 * Does team play autonomous?
		 * 
		 * <P>
		 * Type: INTEGER<br>
		 * Default: -1<br>
		 * </P>
		 * 
		 * <P>
		 * Possible values:<br>
		 * 0: No<br>
		 * 1: Yes<br>
		 * -1: No value<br>
		 * </P>
		 */
		String HAS_AUTONOMOUS = "has_autonomous";
		
		/**
		 * Preferred starting position in Autonomous?
		 * 
		 * <P>
		 * Type: INTEGER (multi-select)<br>
		 * Default: -1
		 * </P>
		 * 
		 * <P>
		 * Possible values:<br>
		 * 0: None<br>
		 * 1: Left<br>
		 * 2: Middle<br>
		 * 3: Right<br>
		 * 12: Left/Middle<br>
		 * 23: Middle/Right<br>
		 * 123: Left/Middle/Right<br>
		 * </P>
		 */
		String PREFERRED_START = "preferred_start_position";
		
		/**
		 * Does team have Kinect?
		 * 
		 * Type: INTEGER<br>
		 * Default: -1<br>
		 * 
		 * <P>
		 * Possible values:<br>
		 * 0: No<br>
		 * 1: Yes<br>
		 * -1: No value<br>
		 * </P>
		 */
		String HAS_KINECT = "has_kinect";
		
		/**
		 * Can team cross barrier?
		 * 
		 * <P>
		 * Type: INTEGER<br>
		 * Default: -1<br>
		 * </P>
		 * 
		 * <P>
		 * Possible values:<br>
		 * 0: No<br>
		 * 1: Yes<br>
		 * -1: No value<br>
		 * </P>
		 */
		String CAN_CROSS = "can_cross";
		
		/**
		 * Can team push down bridge?
		 * 
		 * <P>
		 * Type: INTEGER<br>
		 * Default: -1<br>
		 * </P>
		 * 
		 * <P>
		 * Possible values:<br>
		 * 0: No<br>
		 * 1: Yes<br>
		 * -1: No value<br>
		 * </P>
		 */
		String CAN_PUSH_DOWN_BRIDGE = "can_push_down_bridge";
		
		/**
		 * What is the team's strategy?
		 * 
		 * <P>
		 * Type: INTEGER<br>
		 * Default: -1<br>
		 * </P>
		 * 
		 * <P>
		 * Possible values:<br>
		 * 0: None<br>
		 * 1: Offense<br>
		 * 2: Defense<br>
		 * -1: No value<br>
		 * </P>
		 */
		String STRATEGY = "strategy";
		
		/**
		 * Additional comments
		 * 
		 * <P>Type: TEXT</P>
		 */
		String COMMENTS = "comments";
		
		/* Pre-compiled summary data */
		
		/** Autonomous cumulative scoring data **/
		String SUMMARY_AUTO_NUM_SCORED = "summary_auto_num_scored";
		String SUMMARY_AUTO_NUM_ATTEMPT = "summary_auto_num_attempt";
		String SUMMARY_AUTO_NUM_POINTS = "summary_auto_num_points";
		
		// String SUMMARY_AUTO_NUM_SCORED_LOW = "summary_auto_num_scored_low";
		// String SUMMARY_AUTO_NUM_SCORED_MED = "summary_auto_num_scored_med";
		// String SUMMARY_AUTO_NUM_SCORED_HIGH = "summary_auto_num_scored_high";
		// String SUMMARY_AUTO_NUM_ATTEMPT_LOW = "summary_auto_num_attempt_low";
		// String SUMMARY_AUTO_NUM_ATTEMPT_MED = "summary_auto_num_attempt_med";
		// String SUMMARY_AUTO_NUM_ATTEMPT_HIGH = "summary_auto_num_attempt_high";		
		
		/** Tele-op cumulative scoring data **/
		String SUMMARY_NUM_SCORED = "summary_num_scored";
		String SUMMARY_NUM_ATTEMPT = "summary_num_attempt";
		String SUMMARY_NUM_POINTS = "summary_num_points";
		
		// String SUMMARY_NUM_SCORED_LOW = "summary_num_scored_low";
		// String SUMMARY_NUM_SCORED_MED = "summary_num_scored_med";
		// String SUMMARY_NUM_SCORED_HIGH = "summary_num_scored_high";
		// String SUMMARY_NUM_ATTEMPT_LOW = "summary_num_attempt_low";
		// String SUMMARY_NUM_ATTEMPT_MED = "summary_num_attempt_med";
		// String SUMMARY_NUM_ATTEMPT_HIGH = "summary_num_attempt_high";	
		
		/** Other pre-compiled data **/
		
		/** 
		 * Team's average speed
		 * <P>
		 * Type: INTEGER<br>
		 * Default: -1<br>
		 * </P>
		 * <P>
		 * Possible values:<br>
		 * 0: Slow<br>
		 * 1: Normal<br>
		 * 2: Fast<br>
		 * -1: No value<br>
		 * </P>
		 */
		// String SUMMARY_SPEED = "speed";
		
		/** 
		 * Can the team balance robots?
		 * <P>
		 * Type: INTEGER<br>
		 * Default: -1<br>
		 * </P>
		 * <P>
		 * Possible values:<br>
		 * 0: No<br>
		 * 1: Yes<br>
		 * -1: No value<br>
		 **/
		// String SUMMARY_CAN_BALANCE = "summary_can_balance";
	
		/**
		 * Total number of wins for a team
		 * <P>
		 * Type: INTEGER
		 * Default: 0
		 * </P>
		 */
		String SUMMARY_NUM_WINS = "summary_num_wins";
		
		/**
		 * Total number of losses for a team
		 * <P>
		 * Type: INTEGER
		 * Default: 0
		 * </P
		 */
		String SUMMARY_NUM_LOSSES = "summary_num_losses";
		
		/**
		 * Total score for a team
		 * <P>
		 * Type: INTEGER
		 * Default: 0
		 * </P>
		 */
		String SUMMARY_TOTAL_SCORE = "summary_total_score";
	}
	
	/* Column names for matches table */
	interface MatchesColumns {
		String MATCH_NUM = "match_num";
		// String TIME = "time";
		// String DATE = "date";
		// String COMMENTS = "comments";
	}
	
	/* Column names for team_matches table */
	interface TeamMatchesColumns {		
		/* Foreign keys */		
		String TEAM_ID = "team_id";
		String MATCH_ID = "match_id";
		
		/* Autonomous data (first 15 seconds of match) */		
		String AUTO_NUM_SCORED_LOW = "auto_num_scored_low";
		String AUTO_NUM_SCORED_MED = "auto_num_scored_med";
		String AUTO_NUM_SCORED_HIGH = "auto_num_scored_high";
		String AUTO_NUM_ATTEMPT_LOW = "auto_num_attempt_low";
		String AUTO_NUM_ATTEMPT_MED = "auto_num_attempt_med";
		String AUTO_NUM_ATTEMPT_HIGH = "auto_num_attempt_high";
		
		/* Non-autonomous data (rest of the match) */		
		String NUM_SCORED_LOW = "num_scored_low";
		String NUM_SCORED_MED = "num_scored_med";
		String NUM_SCORED_HIGH = "num_scored_high";	
		String NUM_ATTEMPT_LOW = "num_attempt_low";
		String NUM_ATTEMPT_MED = "num_attempt_med";
		String NUM_ATTEMPT_HIGH = "num_attempt_high";
		
		/**
		 * Which alliance were they?
		 * <P>
		 * Type: INTEGER<br>
		 * Default: -1<br>
		 * </P>
		 * <P>
		 * Possible values:<br>
		 * 0: Blue
		 * 1: Red
		 * -1: No value
		 * </P>
		 **/
		String WHICH_ALLIANCE = "which_alliance";
		
		/**
		 * Did their alliance win the match?
		 * <P>
		 * Type: INTEGER<br>
		 * Default: -1<br>
		 * </P>
		 * <P>
		 * Possible values:<br>
		 * 0: No (loss)
		 * 1: Yes (win)
		 * -1: No value
		 * </P>
		 **/
		String WIN_MATCH = "win_match";
		
		/**
		 * What was the team's final score?
		 * <P>
		 * Type: INTEGER<br>
		 * Default: -1<br>
		 * </P>
		 */
		String FINAL_SCORE = "final_score";
		
		/* General information (robot's performance) */

		/** 
		 * Number of robots balanced 
		 * <P>
		 * Type: INTEGER<br>
		 * Default: -1<br>
		 * </P>
		 * <P>
		 * Possible values:<br>
		 * 0: Did not attempt (not currently implemented)<br>
		 * 2: Two<br>
		 * 3: Three<br>
		 * -1: No value<br>
		 * </P>
		 */
		String NUM_BALANCED = "num_balanced";

		/** 
		 * How did the team cross the barrier?
		 * <P>
		 * Type: INTEGER<br>
		 * Default: -1<br>
		 * </P>
		 * <P>
		 * Possible values:<br>
		 * 0: Did not cross (not currently implemented)<br>
		 * 1: Barrier<br>
		 * 2: Bridge<br>
		 * 3: Barrier/bridge<br>
		 * -1: No value<br>
		 * </P>
		 */
		String HOW_CROSS = "how_cross";		

		/** 
		 * Where did the team pick up balls?
		 * <P>
		 * Type: INTEGER<br>
		 * Default: -1<br>
		 * </P>
		 * <P>
		 * Possible values:<br>
		 * 0: From feeder<br>
		 * 1: From floor<br>
		 * -1: No value<br>
		 * </P>
		 */
		String PICK_UP_BALLS = "pick_up_balls";
		
		/** 
		 * Robot's speed
		 * <P>
		 * Type: INTEGER<br>
		 * Default: -1<br>
		 * </P>
		 * <P>
		 * Possible values:<br>
		 * 0: Poor<br>
		 * 1: Fair<br>
		 * 2: Good<br>
		 * 3: Great<br>
		 * -1: No value<br>
		 * </P>
		 */
		String SPEED = "speed";

		/** 
		 * Robot's agility
		 * <P>
		 * Type: INTEGER<br>
		 * Default: -1<br>
		 * </P>
		 * <P>
		 * Possible values:<br>
		 * 0: Poor<br>
		 * 1: Fair<br>
		 * 2: Good<br>
		 * 3: Great<br>
		 * -1: No value<br>
		 * </P>
		 */
		String AGILITY = "agility";

		/** 
		 * Team's strategy
		 * <P>
		 * Type: INTEGER<br>
		 * Default: -1<br>
		 * </P>
		 * <P>
		 * Possible values:<br>
		 * 0: Offense<br>
		 * 1: Defense<br>
		 * 2: Neutral<br>
		 * -1: No value<br>
		 * </P>
		 */
		String STRATEGY = "strategy";

		/** 
		 * Robot's penalty risk
		 * <P>
		 * Type: INTEGER<br>
		 * Default: -1<br>
		 * </P>
		 * <P>
		 * Possible values:<br>
		 * 0: Low<br>
		 * 1: Medium<br>
		 * 2: High<br>
		 * -1: No value<br>
		 * </P>
		 */
		String PENALTY_RISK = "penalty_risk";
		
		/**
		 * Additional comments
		 *
		 * <P>Type: TEXT</P>
		 */
		// String COMMENTS = "comments";
	}

    public static final String AUTHORITY = "com.cmu.scout.provider.Scout";
	private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);	
	private static final String PATH_TEAMS = "teams";
	private static final String PATH_MATCHES = "matches";
	private static final String PATH_TEAM_MATCHES = "team_matches";
	
	/**
	 * Teams table contract. This table stores a list of teams and information
	 * about those teams.
	 */
	public static final class Teams implements BaseColumns, TeamsColumns {
		
		// This class cannot be instantiated.
		private Teams() { }
		
        /* MIME type definitions */
        
        /** 
         * The MIME type of {@link #CONTENT_URI} providing a directory of teams. 
         */
        public static final String CONTENT_TYPE = 
        		"vnd.android.cursor.dir/vnd.scout.team";

        /** 
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single team. 
         */
        public static final String CONTENT_ITEM_TYPE = 
        		"vnd.android.cursor.item/vnd.scout.team";
        
        /* URI definitions */
        
        /** 
         * The base URI for this table. 
         */
        public static final Uri CONTENT_URI = 
        		BASE_CONTENT_URI.buildUpon().appendPath(PATH_TEAMS).build();
      
        /* URI builders */
        
        /** 
         * Builds the content URI match pattern for a single team, specified 
         * by the team's ID. 
         */
        public static Uri buildTeamIdUri(String teamId) {
            return CONTENT_URI.buildUpon().appendPath(teamId).build();
        }
	}
	
	/**
	 * Matches table contract. This table store matches and information 
	 * about the matches. 
	 */
	public static final class Matches implements BaseColumns, MatchesColumns {
		
		// This class cannot be instantiated.
		private Matches() { }

        /* MIME type definitions */
        
        /** 
         * The MIME type of {@link #CONTENT_URI} providing a directory of match-items. 
         */
        public static final String CONTENT_TYPE = 
        		"vnd.android.cursor.dir/vnd.scout.match";

        /** 
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single match-item.
         */
        public static final String CONTENT_ITEM_TYPE = 
        		"vnd.android.cursor.item/vnd.scout.match";
        
        /* URI definitions */
   
        /** 
         * The base URI for this table. 
         */
        public static final Uri CONTENT_URI = 
        		BASE_CONTENT_URI.buildUpon().appendPath(PATH_MATCHES).build();

        /* URI Builders */
        
        /** 
         * Build the content URI for a single match, specified by the match's ID. 
         */
        public static Uri buildMatchIdUri(String matchId) {
            return CONTENT_URI.buildUpon().appendPath(matchId).build();
        }
        
        /*
         * NOTE: The following static methods provide access to the TeamsMatches table,
         * but are provided in the Matches class to avoid confusion.
         * 
         * They will NEVER allow access to data in the matches table.
         */
        
        /**
         * Build the content URI for a single team's matches, specified by the team's id.
         */        
        public static Uri buildMatchTeamIdUri(String teamId) {
            return CONTENT_URI.buildUpon().appendPath(PATH_TEAMS).appendPath(teamId).build();
        }
        
        /**
         * Build the content URI for a specific match for a single team, specified first 
         * by the team's id, and specified second by the match's id.
         */        
        public static Uri buildMatchIdTeamIdUri(String matchId, String teamId) {
            return CONTENT_URI.buildUpon().appendPath(matchId)
            		.appendPath(PATH_TEAMS).appendPath(teamId).build();
        }
	}

	/**
	 * The "team_match" table provides information on how a specific team 
	 * performed while competing during a given match. This class contains no
	 * methods, but implements BaseColumns and TeamMatchesColumns for public access
	 * to the table's column names.
	 */
	public static final class TeamMatches implements BaseColumns, TeamMatchesColumns {
		
		// This class cannot be instantiated.
		private TeamMatches() { }
        
		/* MIME type definitions */
        
        /** 
         * The MIME type of {@link #CONTENT_URI} providing a directory of team-match items. 
         */
        public static final String CONTENT_TYPE = 
        		"vnd.android.cursor.dir/vnd.scout.team_match";

        /** 
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single team-match item.
         */
        public static final String CONTENT_ITEM_TYPE = 
        		"vnd.android.cursor.item/vnd.scout.team_match";
        
        /* URI definitions */
   
        /** 
         * The base URI for this table. 
         */
        public static final Uri CONTENT_URI = 
        		BASE_CONTENT_URI.buildUpon().appendPath(PATH_TEAM_MATCHES).build();
		
	}

}