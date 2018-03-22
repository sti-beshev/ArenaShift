package com.beshev.arenashift.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseManager extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "ArenaShift.db";
	private static final int DATABASE_VERSION = 4;
	// The name of a table can't start with a number
	public static final String TABLE_YEAR = "a2016";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MONTH = "Month";
    public static final String COLUMN_DAY = "Day";
    public static final String COLUMN_PAN_MEHANIK = "PanMehanik";
    public static final String COLUMN_PAN_KASA_ONE = "PanKasaOne";
    public static final String COLUMN_PAN_KASA_TWO = "PanKasaTwo";
    public static final String COLUMN_PAN_KASA_THREE = "PanKasaThree";
    public static final String COLUMN_RAZPOREDITEL_ONE = "RazporeditelOne";
    public static final String COLUMN_RAZPOREDITEL_TWO = "RazporeditelTwo";
    public static final String COLUMN_CEN_MEHANIK = "CenMehanik";
    public static final String COLUMN_CEN_KASA = "CenKasa";

    public static final String TABLE_EVENTS = "events";
    public static final String COLUMN_DATE = "Date";
    public static final String COLUMN_EVENT = "Event";
	

	public DataBaseManager(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
	}


	@Override
	public void onCreate(SQLiteDatabase db) {

		String CREATE_TABLE_YEAR = "CREATE TABLE " + TABLE_YEAR + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
				+ COLUMN_MONTH + " INTEGER,"
                + COLUMN_DAY + " INTEGER,"
                + COLUMN_PAN_MEHANIK + " TEXT,"
				+ COLUMN_PAN_KASA_ONE + " TEXT,"
                + COLUMN_PAN_KASA_TWO + " TEXT,"
                + COLUMN_PAN_KASA_THREE + " TEXT,"
                + COLUMN_RAZPOREDITEL_ONE + " TEXT,"
                + COLUMN_RAZPOREDITEL_TWO + " TEXT,"
				+ COLUMN_CEN_MEHANIK + " TEXT,"
                + COLUMN_CEN_KASA + " TEXT);";
		
		db.execSQL(CREATE_TABLE_YEAR);

		String CREATE_TABLE_EVENTS = "CREATE TABLE " + TABLE_EVENTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_EVENT + " TEXT);";

		db.execSQL(CREATE_TABLE_EVENTS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL("DROP TABLE IF EXISTS " + TABLE_YEAR);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
		onCreate(db);
	}
}
