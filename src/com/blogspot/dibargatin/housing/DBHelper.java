package com.blogspot.dibargatin.housing;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	// ===========================================================
	// Constants
	// ===========================================================
	public static String DB_NAME = "housing_db.sqlite3";
	public static int DB_VERSION = 2;

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================
	public DBHelper(Context pContext) {
		super(pContext, DB_NAME, null, DB_VERSION);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE Counters (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, note TEXT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS Counters;");
		onCreate(db);
	}

	// ===========================================================
	// Methods
	// ===========================================================
	public Cursor fetchAllCounters() {
		return getReadableDatabase().rawQuery("SELECT * FROM Counters", null);
	}

	public long insertCounter(String name, String note) {
		ContentValues cv = new ContentValues();
		
		cv.put("name", name);
		cv.put("note", note);
		
		return getWritableDatabase().insert("Counters", null, cv);
	}

	public void updateCounter(long id, String name, String note) {
		ContentValues cv = new ContentValues();
		
		cv.put("name", name);
		cv.put("note", note);
		
		getWritableDatabase().update("Counters", cv, "_id = ?", new String[] { Long.toString(id) });
	}

	public void deleteCounter(long id) {
		getWritableDatabase().delete("Counters", "_id = ?", new String[] { Long.toString(id) });
	}

	public void deleteAllCounters() {
		getWritableDatabase().delete("Counters", null, null);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
