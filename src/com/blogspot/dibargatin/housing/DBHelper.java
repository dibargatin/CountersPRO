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
	public static int DB_VERSION = 3;

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
		db.execSQL("PRAGMA foreign_keys = ON;");
		db.execSQL("CREATE TABLE Counters (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, note TEXT);");
		db.execSQL("CREATE TABLE Entries (_id INTEGER PRIMARY KEY AUTOINCREMENT, counter_id INTEGER REFERENCES Counters(_id), entry_date DATE, value REAL);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS Counters;");
		db.execSQL("DROP TABLE IF EXISTS Entries;");
		onCreate(db);
	}

	// ===========================================================
	// Counter Methods
	// ===========================================================
	public Cursor fetchAllCounters() {
		return getReadableDatabase().rawQuery("SELECT * FROM Counters", null);
	}
	
	public Cursor fetchCounterById(long id) {
		return getReadableDatabase().rawQuery("SELECT * FROM Counters WHERE _id = ?", new String[]{Long.toString(id)});
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
	// Entries Methods
	// ===========================================================
	public Cursor fetchEntriesByCounterId(long counterId) {
		return getReadableDatabase().rawQuery("SELECT _id, counter_id, strftime('%d.%m.%Y', entry_date) AS entry_date, " +
				"value FROM Entries AS en WHERE counter_id = ? ORDER BY entry_date DESC", new String[] { Long.toString(counterId) });
		
		//(SELECT value FROM Entries WHERE counter_id = ? AND entry_date < en.entry_date ORDER BY entry_date DESC LIMIT 1) AS value
	}

	public long insertEntry(long counterId, String entryDate, double value) {
		ContentValues cv = new ContentValues();

		cv.put("counter_id", counterId);
		cv.put("entry_date", entryDate);
		cv.put("value", value);

		return getWritableDatabase().insert("Entries", null, cv);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
