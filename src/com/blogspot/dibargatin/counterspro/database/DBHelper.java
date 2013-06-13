
package com.blogspot.dibargatin.counterspro.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.blogspot.dibargatin.counterspro.CountersListActivity;

public class DBHelper extends SQLiteOpenHelper {

    // ===========================================================
    // Constants
    // ===========================================================
    public static String DB_NAME = "counterspro.sqlite3";
    
    public static String DB_NAME_FULL = "data/com.blogspot.dibargatin.counterspro/databases/counterspro.sqlite3";

    public static int DB_VERSION = 5;

    // Таблица счетчиков
    public final static String TABLE_COUNTER = "Counters";

    public final static String COUNTER_ID = "_id";

    public final static String COUNTER_NAME = "name";

    public final static String COUNTER_NOTE = "note";

    public final static String COUNTER_MEASURE = "measure";

    public final static String COUNTER_COLOR = "color";

    public final static String COUNTER_CURRENCY = "currency";

    public final static String COUNTER_RATE_TYPE = "rate_type";

    public final static String COUNTER_PERIOD_TYPE = "period_type";

    public final static String COUNTER_FORMULA = "formula";

    public final static String COUNTER_VIEW_VALUE_TYPE = "view_value_type";

    public final static String COUNTER_INPUT_VALUE_TYPE = "input_value_type";
    
    public final static String COUNTER_IND_GROUP_TYPE = "indications_group_type";

    // Таблица показаний
    public final static String TABLE_INDICATION = "Indications";

    public final static String INDICATION_ID = "_id";

    public final static String INDICATION_COUNTER_ID = "counter_id";

    public final static String INDICATION_DATE = "entry_date";

    public final static String INDICATION_VALUE = "value";

    public final static String INDICATION_RATE = "rate";

    public final static String INDICATION_NOTE = "note";

    // Миграции БД
    private static final Patch[] PATCHES = new Patch[] {
            // №1 Таблицы для храненения счетчиков и показаний
            new Patch() {
                public void apply(SQLiteDatabase db) {
                    db.beginTransaction();

                    try {
                        db.execSQL("PRAGMA foreign_keys = ON;");

                        final StringBuilder queryCounters = new StringBuilder();

                        queryCounters.append("CREATE TABLE " + TABLE_COUNTER + " (");
                        queryCounters.append(COUNTER_ID + " INTEGER PRIMARY KEY,");
                        queryCounters.append(COUNTER_NAME + " TEXT,");
                        queryCounters.append(COUNTER_NOTE + " TEXT,");
                        queryCounters.append(COUNTER_MEASURE + " TEXT,");
                        queryCounters.append(COUNTER_COLOR + " INTEGER,");
                        queryCounters.append(COUNTER_CURRENCY + " TEXT,");
                        queryCounters.append(COUNTER_RATE_TYPE + " INTEGER,");
                        queryCounters.append(COUNTER_PERIOD_TYPE + " INTEGER,");
                        queryCounters.append(COUNTER_FORMULA + " TEXT");
                        queryCounters.append(");");

                        db.execSQL(queryCounters.toString());

                        final StringBuilder queryIndications = new StringBuilder();

                        queryIndications.append("CREATE TABLE " + TABLE_INDICATION + " (");
                        queryIndications.append(INDICATION_ID + " INTEGER PRIMARY KEY,");
                        queryIndications.append(INDICATION_COUNTER_ID + " INTEGER REFERENCES "
                                + TABLE_COUNTER + "(" + COUNTER_ID + ") ON DELETE CASCADE,");
                        queryIndications.append(INDICATION_DATE + " DATE,");
                        queryIndications.append(INDICATION_VALUE + " REAL,");
                        queryIndications.append(INDICATION_RATE + " REAL");
                        queryIndications.append(");");

                        db.execSQL(queryIndications.toString());

                        db.setTransactionSuccessful();

                    } catch (Exception e) {
                        Log.d(CountersListActivity.LOG_TAG, e.getStackTrace().toString());
                    } finally {
                        db.endTransaction();
                    }
                }

                public void revert(SQLiteDatabase db) {
                    db.beginTransaction();

                    try {
                        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COUNTER + ";");
                        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INDICATION + ";");

                        db.setTransactionSuccessful();

                    } catch (Exception e) {
                        Log.d(CountersListActivity.LOG_TAG, e.getStackTrace().toString());
                    } finally {
                        db.endTransaction();
                    }
                }
            }

            // №2 Примечание в показание
            , new Patch() {
                public void apply(SQLiteDatabase db) {
                    db.beginTransaction();

                    try {
                        // Сохраним исходные данные
                        db.execSQL("ALTER TABLE " + TABLE_INDICATION
                                + " RENAME TO Indications_old;");

                        // Создадим новую структуру с полем NOTE
                        final StringBuilder queryIndications = new StringBuilder();

                        queryIndications.append("CREATE TABLE " + TABLE_INDICATION + " (");
                        queryIndications.append(INDICATION_ID + " INTEGER PRIMARY KEY,");
                        queryIndications.append(INDICATION_COUNTER_ID + " INTEGER REFERENCES "
                                + TABLE_COUNTER + "(" + COUNTER_ID + ") ON DELETE CASCADE,");
                        queryIndications.append(INDICATION_DATE + " DATE,");
                        queryIndications.append(INDICATION_VALUE + " REAL,");
                        queryIndications.append(INDICATION_RATE + " REAL,");
                        queryIndications.append(INDICATION_NOTE + " TEXT");
                        queryIndications.append(");");

                        db.execSQL(queryIndications.toString());

                        // Скопируем исходные данные в новую структуру
                        queryIndications.setLength(0);
                        queryIndications.append("INSERT INTO " + TABLE_INDICATION + " (");
                        queryIndications.append(INDICATION_ID + ", ");
                        queryIndications.append(INDICATION_COUNTER_ID + ", ");
                        queryIndications.append(INDICATION_DATE + ", ");
                        queryIndications.append(INDICATION_VALUE + ", ");
                        queryIndications.append(INDICATION_RATE + ", ");
                        queryIndications.append(INDICATION_NOTE + " ");
                        queryIndications.append(") SELECT ");
                        queryIndications.append(INDICATION_ID + ", ");
                        queryIndications.append(INDICATION_COUNTER_ID + ", ");
                        queryIndications.append(INDICATION_DATE + ", ");
                        queryIndications.append(INDICATION_VALUE + ", ");
                        queryIndications.append(INDICATION_RATE + ", ");
                        queryIndications.append("'' AS " + INDICATION_NOTE + " ");
                        queryIndications.append("FROM Indications_old");

                        db.execSQL(queryIndications.toString());

                        // Удалим старую структуру
                        db.execSQL("DROP TABLE IF EXISTS Indications_old;");

                        db.setTransactionSuccessful();

                    } catch (Exception e) {
                        Log.d(CountersListActivity.LOG_TAG, e.getStackTrace().toString());
                    } finally {
                        db.endTransaction();
                    }
                }

                public void revert(SQLiteDatabase db) {
                    db.beginTransaction();

                    try {
                        // Сохраним исходные данные
                        db.execSQL("ALTER TABLE " + TABLE_INDICATION
                                + " RENAME TO Indications_old;");

                        // Откатим структуру к предыдущему состоянию
                        final StringBuilder queryIndications = new StringBuilder();

                        queryIndications.append("CREATE TABLE " + TABLE_INDICATION + " (");
                        queryIndications.append(INDICATION_ID + " INTEGER PRIMARY KEY,");
                        queryIndications.append(INDICATION_COUNTER_ID + " INTEGER REFERENCES "
                                + TABLE_COUNTER + "(" + COUNTER_ID + ") ON DELETE CASCADE,");
                        queryIndications.append(INDICATION_DATE + " DATE,");
                        queryIndications.append(INDICATION_VALUE + " REAL,");
                        queryIndications.append(INDICATION_RATE + " REAL");
                        queryIndications.append(");");

                        db.execSQL(queryIndications.toString());

                        // Скопируем исходные данные в старую структуру
                        queryIndications.setLength(0);
                        queryIndications.append("INSERT INTO " + TABLE_INDICATION + " (");
                        queryIndications.append(INDICATION_ID + ", ");
                        queryIndications.append(INDICATION_COUNTER_ID + ", ");
                        queryIndications.append(INDICATION_DATE + ", ");
                        queryIndications.append(INDICATION_VALUE + ", ");
                        queryIndications.append(INDICATION_RATE + " ");
                        queryIndications.append(") SELECT ");
                        queryIndications.append(INDICATION_ID + ", ");
                        queryIndications.append(INDICATION_COUNTER_ID + ", ");
                        queryIndications.append(INDICATION_DATE + ", ");
                        queryIndications.append(INDICATION_VALUE + ", ");
                        queryIndications.append(INDICATION_RATE + " ");
                        queryIndications.append("FROM Indications_old");

                        db.execSQL(queryIndications.toString());

                        // Удалим старую структуру
                        db.execSQL("DROP TABLE IF EXISTS Indications_old;");

                        db.setTransactionSuccessful();

                    } catch (Exception e) {
                        Log.d(CountersListActivity.LOG_TAG, e.getStackTrace().toString());
                    } finally {
                        db.endTransaction();
                    }
                }
            }

            // №3 Вид отображаемого значения в счетчик
            , new Patch() {
                public void apply(SQLiteDatabase db) {
                    db.beginTransaction();

                    try {
                        // Сохраним исходные данные
                        db.execSQL("ALTER TABLE " + TABLE_COUNTER + " RENAME TO Counters_old;");

                        // Создадим новую структуру с полем VIEW_VALUE_TYPE
                        db.execSQL("PRAGMA foreign_keys = ON;");

                        final StringBuilder queryCounters = new StringBuilder();

                        queryCounters.append("CREATE TABLE " + TABLE_COUNTER + " (");
                        queryCounters.append(COUNTER_ID + " INTEGER PRIMARY KEY,");
                        queryCounters.append(COUNTER_NAME + " TEXT,");
                        queryCounters.append(COUNTER_NOTE + " TEXT,");
                        queryCounters.append(COUNTER_MEASURE + " TEXT,");
                        queryCounters.append(COUNTER_COLOR + " INTEGER,");
                        queryCounters.append(COUNTER_CURRENCY + " TEXT,");
                        queryCounters.append(COUNTER_RATE_TYPE + " INTEGER,");
                        queryCounters.append(COUNTER_PERIOD_TYPE + " INTEGER,");
                        queryCounters.append(COUNTER_FORMULA + " TEXT,");
                        queryCounters.append(COUNTER_VIEW_VALUE_TYPE + " INTEGER");
                        queryCounters.append(");");

                        db.execSQL(queryCounters.toString());

                        // Скопируем исходные данные в новую структуру
                        queryCounters.setLength(0);
                        queryCounters.append("INSERT INTO " + TABLE_COUNTER + " (");
                        queryCounters.append(COUNTER_ID + ", ");
                        queryCounters.append(COUNTER_NAME + ", ");
                        queryCounters.append(COUNTER_NOTE + ", ");
                        queryCounters.append(COUNTER_MEASURE + ", ");
                        queryCounters.append(COUNTER_COLOR + ", ");
                        queryCounters.append(COUNTER_CURRENCY + ", ");
                        queryCounters.append(COUNTER_RATE_TYPE + ", ");
                        queryCounters.append(COUNTER_PERIOD_TYPE + ", ");
                        queryCounters.append(COUNTER_FORMULA + ", ");
                        queryCounters.append(COUNTER_VIEW_VALUE_TYPE + " ");
                        queryCounters.append(") SELECT ");
                        queryCounters.append(COUNTER_ID + ", ");
                        queryCounters.append(COUNTER_NAME + ", ");
                        queryCounters.append(COUNTER_NOTE + ", ");
                        queryCounters.append(COUNTER_MEASURE + ", ");
                        queryCounters.append(COUNTER_COLOR + ", ");
                        queryCounters.append(COUNTER_CURRENCY + ", ");
                        queryCounters.append(COUNTER_RATE_TYPE + ", ");
                        queryCounters.append(COUNTER_PERIOD_TYPE + ", ");
                        queryCounters.append(COUNTER_FORMULA + ", ");
                        queryCounters.append("0 AS " + COUNTER_VIEW_VALUE_TYPE + " ");
                        queryCounters.append("FROM Counters_old");

                        db.execSQL(queryCounters.toString());

                        // Удалим старую структуру
                        db.execSQL("DROP TABLE IF EXISTS Counters_old;");

                        db.setTransactionSuccessful();

                    } catch (Exception e) {
                        Log.d(CountersListActivity.LOG_TAG, e.getStackTrace().toString());
                    } finally {
                        db.endTransaction();
                    }
                }

                public void revert(SQLiteDatabase db) {
                    db.beginTransaction();

                    try {
                        // Сохраним исходные данные
                        db.execSQL("ALTER TABLE " + TABLE_COUNTER + " RENAME TO Counters_old;");

                        // Вернем исходную структуру
                        db.execSQL("PRAGMA foreign_keys = ON;");

                        final StringBuilder queryCounters = new StringBuilder();

                        queryCounters.append("CREATE TABLE " + TABLE_COUNTER + " (");
                        queryCounters.append(COUNTER_ID + " INTEGER PRIMARY KEY,");
                        queryCounters.append(COUNTER_NAME + " TEXT,");
                        queryCounters.append(COUNTER_NOTE + " TEXT,");
                        queryCounters.append(COUNTER_MEASURE + " TEXT,");
                        queryCounters.append(COUNTER_COLOR + " INTEGER,");
                        queryCounters.append(COUNTER_CURRENCY + " TEXT,");
                        queryCounters.append(COUNTER_RATE_TYPE + " INTEGER,");
                        queryCounters.append(COUNTER_PERIOD_TYPE + " INTEGER,");
                        queryCounters.append(COUNTER_FORMULA + " TEXT");
                        queryCounters.append(");");

                        db.execSQL(queryCounters.toString());

                        // Скопируем исходные данные в новую структуру
                        queryCounters.setLength(0);
                        queryCounters.append("INSERT INTO " + TABLE_COUNTER + " (");
                        queryCounters.append(COUNTER_ID + ", ");
                        queryCounters.append(COUNTER_NAME + ", ");
                        queryCounters.append(COUNTER_NOTE + ", ");
                        queryCounters.append(COUNTER_MEASURE + ", ");
                        queryCounters.append(COUNTER_COLOR + ", ");
                        queryCounters.append(COUNTER_CURRENCY + ", ");
                        queryCounters.append(COUNTER_RATE_TYPE + ", ");
                        queryCounters.append(COUNTER_PERIOD_TYPE + ", ");
                        queryCounters.append(COUNTER_FORMULA + " ");
                        queryCounters.append(") SELECT ");
                        queryCounters.append(COUNTER_ID + ", ");
                        queryCounters.append(COUNTER_NAME + ", ");
                        queryCounters.append(COUNTER_NOTE + ", ");
                        queryCounters.append(COUNTER_MEASURE + ", ");
                        queryCounters.append(COUNTER_COLOR + ", ");
                        queryCounters.append(COUNTER_CURRENCY + ", ");
                        queryCounters.append(COUNTER_RATE_TYPE + ", ");
                        queryCounters.append(COUNTER_PERIOD_TYPE + ", ");
                        queryCounters.append(COUNTER_FORMULA + " ");
                        queryCounters.append("FROM Counters_old");

                        db.execSQL(queryCounters.toString());

                        // Удалим старую структуру
                        db.execSQL("DROP TABLE IF EXISTS Counters_old;");

                        db.setTransactionSuccessful();

                    } catch (Exception e) {
                        Log.d(CountersListActivity.LOG_TAG, e.getStackTrace().toString());
                    } finally {
                        db.endTransaction();
                    }
                }
            }

            // №4 Вид вводимого значения в счетчик
            , new Patch() {
                public void apply(SQLiteDatabase db) {
                    db.beginTransaction();

                    try {
                        // Сохраним исходные данные
                        db.execSQL("ALTER TABLE " + TABLE_COUNTER + " RENAME TO Counters_old;");

                        // Создадим новую структуру с полем VIEW_VALUE_TYPE
                        db.execSQL("PRAGMA foreign_keys = ON;");

                        final StringBuilder queryCounters = new StringBuilder();

                        queryCounters.append("CREATE TABLE " + TABLE_COUNTER + " (");
                        queryCounters.append(COUNTER_ID + " INTEGER PRIMARY KEY,");
                        queryCounters.append(COUNTER_NAME + " TEXT,");
                        queryCounters.append(COUNTER_NOTE + " TEXT,");
                        queryCounters.append(COUNTER_MEASURE + " TEXT,");
                        queryCounters.append(COUNTER_COLOR + " INTEGER,");
                        queryCounters.append(COUNTER_CURRENCY + " TEXT,");
                        queryCounters.append(COUNTER_RATE_TYPE + " INTEGER,");
                        queryCounters.append(COUNTER_PERIOD_TYPE + " INTEGER,");
                        queryCounters.append(COUNTER_FORMULA + " TEXT,");
                        queryCounters.append(COUNTER_VIEW_VALUE_TYPE + " INTEGER,");
                        queryCounters.append(COUNTER_INPUT_VALUE_TYPE + " INTEGER");
                        queryCounters.append(");");

                        db.execSQL(queryCounters.toString());

                        // Скопируем исходные данные в новую структуру
                        queryCounters.setLength(0);
                        queryCounters.append("INSERT INTO " + TABLE_COUNTER + " (");
                        queryCounters.append(COUNTER_ID + ", ");
                        queryCounters.append(COUNTER_NAME + ", ");
                        queryCounters.append(COUNTER_NOTE + ", ");
                        queryCounters.append(COUNTER_MEASURE + ", ");
                        queryCounters.append(COUNTER_COLOR + ", ");
                        queryCounters.append(COUNTER_CURRENCY + ", ");
                        queryCounters.append(COUNTER_RATE_TYPE + ", ");
                        queryCounters.append(COUNTER_PERIOD_TYPE + ", ");
                        queryCounters.append(COUNTER_FORMULA + ", ");
                        queryCounters.append(COUNTER_VIEW_VALUE_TYPE + ", ");
                        queryCounters.append(COUNTER_INPUT_VALUE_TYPE + " ");
                        queryCounters.append(") SELECT ");
                        queryCounters.append(COUNTER_ID + ", ");
                        queryCounters.append(COUNTER_NAME + ", ");
                        queryCounters.append(COUNTER_NOTE + ", ");
                        queryCounters.append(COUNTER_MEASURE + ", ");
                        queryCounters.append(COUNTER_COLOR + ", ");
                        queryCounters.append(COUNTER_CURRENCY + ", ");
                        queryCounters.append(COUNTER_RATE_TYPE + ", ");
                        queryCounters.append(COUNTER_PERIOD_TYPE + ", ");
                        queryCounters.append(COUNTER_FORMULA + ", ");
                        queryCounters.append(COUNTER_VIEW_VALUE_TYPE + ", ");
                        queryCounters.append("0 AS " + COUNTER_INPUT_VALUE_TYPE + " ");
                        queryCounters.append("FROM Counters_old");

                        db.execSQL(queryCounters.toString());

                        // Удалим старую структуру
                        db.execSQL("DROP TABLE IF EXISTS Counters_old;");

                        db.setTransactionSuccessful();

                    } catch (Exception e) {
                        Log.d(CountersListActivity.LOG_TAG, e.getStackTrace().toString());
                    } finally {
                        db.endTransaction();
                    }
                }

                public void revert(SQLiteDatabase db) {
                    db.beginTransaction();

                    try {
                        // Сохраним исходные данные
                        db.execSQL("ALTER TABLE " + TABLE_COUNTER + " RENAME TO Counters_old;");

                        // Вернем исходную структуру
                        db.execSQL("PRAGMA foreign_keys = ON;");

                        final StringBuilder queryCounters = new StringBuilder();

                        queryCounters.append("CREATE TABLE " + TABLE_COUNTER + " (");
                        queryCounters.append(COUNTER_ID + " INTEGER PRIMARY KEY,");
                        queryCounters.append(COUNTER_NAME + " TEXT,");
                        queryCounters.append(COUNTER_NOTE + " TEXT,");
                        queryCounters.append(COUNTER_MEASURE + " TEXT,");
                        queryCounters.append(COUNTER_COLOR + " INTEGER,");
                        queryCounters.append(COUNTER_CURRENCY + " TEXT,");
                        queryCounters.append(COUNTER_RATE_TYPE + " INTEGER,");
                        queryCounters.append(COUNTER_PERIOD_TYPE + " INTEGER,");
                        queryCounters.append(COUNTER_FORMULA + " TEXT,");
                        queryCounters.append(COUNTER_VIEW_VALUE_TYPE + " INTEGER");
                        queryCounters.append(");");

                        db.execSQL(queryCounters.toString());

                        // Скопируем исходные данные в новую структуру
                        queryCounters.setLength(0);
                        queryCounters.append("INSERT INTO " + TABLE_COUNTER + " (");
                        queryCounters.append(COUNTER_ID + ", ");
                        queryCounters.append(COUNTER_NAME + ", ");
                        queryCounters.append(COUNTER_NOTE + ", ");
                        queryCounters.append(COUNTER_MEASURE + ", ");
                        queryCounters.append(COUNTER_COLOR + ", ");
                        queryCounters.append(COUNTER_CURRENCY + ", ");
                        queryCounters.append(COUNTER_RATE_TYPE + ", ");
                        queryCounters.append(COUNTER_PERIOD_TYPE + ", ");
                        queryCounters.append(COUNTER_FORMULA + ", ");
                        queryCounters.append(COUNTER_VIEW_VALUE_TYPE + " ");
                        queryCounters.append(") SELECT ");
                        queryCounters.append(COUNTER_ID + ", ");
                        queryCounters.append(COUNTER_NAME + ", ");
                        queryCounters.append(COUNTER_NOTE + ", ");
                        queryCounters.append(COUNTER_MEASURE + ", ");
                        queryCounters.append(COUNTER_COLOR + ", ");
                        queryCounters.append(COUNTER_CURRENCY + ", ");
                        queryCounters.append(COUNTER_RATE_TYPE + ", ");
                        queryCounters.append(COUNTER_PERIOD_TYPE + ", ");
                        queryCounters.append(COUNTER_FORMULA + ", ");
                        queryCounters.append(COUNTER_VIEW_VALUE_TYPE + " ");
                        queryCounters.append("FROM Counters_old");

                        db.execSQL(queryCounters.toString());

                        // Удалим старую структуру
                        db.execSQL("DROP TABLE IF EXISTS Counters_old;");

                        db.setTransactionSuccessful();

                    } catch (Exception e) {
                        Log.d(CountersListActivity.LOG_TAG, e.getStackTrace().toString());
                    } finally {
                        db.endTransaction();
                    }
                }
            }
            
            // №5 Вид группировки показаний в счетчик
            , new Patch() {
                public void apply(SQLiteDatabase db) {
                    db.beginTransaction();

                    try {
                        // Сохраним исходные данные
                        db.execSQL("ALTER TABLE " + TABLE_COUNTER + " RENAME TO Counters_old;");

                        // Создадим новую структуру с полем VIEW_VALUE_TYPE
                        db.execSQL("PRAGMA foreign_keys = ON;");

                        final StringBuilder queryCounters = new StringBuilder();

                        queryCounters.append("CREATE TABLE " + TABLE_COUNTER + " (");
                        queryCounters.append(COUNTER_ID + " INTEGER PRIMARY KEY,");
                        queryCounters.append(COUNTER_NAME + " TEXT,");
                        queryCounters.append(COUNTER_NOTE + " TEXT,");
                        queryCounters.append(COUNTER_MEASURE + " TEXT,");
                        queryCounters.append(COUNTER_COLOR + " INTEGER,");
                        queryCounters.append(COUNTER_CURRENCY + " TEXT,");
                        queryCounters.append(COUNTER_RATE_TYPE + " INTEGER,");
                        queryCounters.append(COUNTER_PERIOD_TYPE + " INTEGER,");
                        queryCounters.append(COUNTER_FORMULA + " TEXT,");
                        queryCounters.append(COUNTER_VIEW_VALUE_TYPE + " INTEGER,");
                        queryCounters.append(COUNTER_INPUT_VALUE_TYPE + " INTEGER,");
                        queryCounters.append(COUNTER_IND_GROUP_TYPE + " INTEGER");                        
                        queryCounters.append(");");

                        db.execSQL(queryCounters.toString());

                        // Скопируем исходные данные в новую структуру
                        queryCounters.setLength(0);
                        queryCounters.append("INSERT INTO " + TABLE_COUNTER + " (");
                        queryCounters.append(COUNTER_ID + ", ");
                        queryCounters.append(COUNTER_NAME + ", ");
                        queryCounters.append(COUNTER_NOTE + ", ");
                        queryCounters.append(COUNTER_MEASURE + ", ");
                        queryCounters.append(COUNTER_COLOR + ", ");
                        queryCounters.append(COUNTER_CURRENCY + ", ");
                        queryCounters.append(COUNTER_RATE_TYPE + ", ");
                        queryCounters.append(COUNTER_PERIOD_TYPE + ", ");
                        queryCounters.append(COUNTER_FORMULA + ", ");
                        queryCounters.append(COUNTER_VIEW_VALUE_TYPE + ", ");
                        queryCounters.append(COUNTER_INPUT_VALUE_TYPE + ", ");
                        queryCounters.append(COUNTER_IND_GROUP_TYPE + " ");
                        queryCounters.append(") SELECT ");
                        queryCounters.append(COUNTER_ID + ", ");
                        queryCounters.append(COUNTER_NAME + ", ");
                        queryCounters.append(COUNTER_NOTE + ", ");
                        queryCounters.append(COUNTER_MEASURE + ", ");
                        queryCounters.append(COUNTER_COLOR + ", ");
                        queryCounters.append(COUNTER_CURRENCY + ", ");
                        queryCounters.append(COUNTER_RATE_TYPE + ", ");
                        queryCounters.append(COUNTER_PERIOD_TYPE + ", ");
                        queryCounters.append(COUNTER_FORMULA + ", ");
                        queryCounters.append(COUNTER_VIEW_VALUE_TYPE + ", ");
                        queryCounters.append(COUNTER_INPUT_VALUE_TYPE + ", ");
                        queryCounters.append("0 AS " + COUNTER_IND_GROUP_TYPE + " ");
                        queryCounters.append("FROM Counters_old");

                        db.execSQL(queryCounters.toString());

                        // Удалим старую структуру
                        db.execSQL("DROP TABLE IF EXISTS Counters_old;");

                        db.setTransactionSuccessful();

                    } catch (Exception e) {
                        Log.d(CountersListActivity.LOG_TAG, e.getStackTrace().toString());
                    } finally {
                        db.endTransaction();
                    }
                }

                public void revert(SQLiteDatabase db) {
                    db.beginTransaction();

                    try {
                        // Сохраним исходные данные
                        db.execSQL("ALTER TABLE " + TABLE_COUNTER + " RENAME TO Counters_old;");

                        // Вернем исходную структуру
                        db.execSQL("PRAGMA foreign_keys = ON;");

                        final StringBuilder queryCounters = new StringBuilder();

                        queryCounters.append("CREATE TABLE " + TABLE_COUNTER + " (");
                        queryCounters.append(COUNTER_ID + " INTEGER PRIMARY KEY,");
                        queryCounters.append(COUNTER_NAME + " TEXT,");
                        queryCounters.append(COUNTER_NOTE + " TEXT,");
                        queryCounters.append(COUNTER_MEASURE + " TEXT,");
                        queryCounters.append(COUNTER_COLOR + " INTEGER,");
                        queryCounters.append(COUNTER_CURRENCY + " TEXT,");
                        queryCounters.append(COUNTER_RATE_TYPE + " INTEGER,");
                        queryCounters.append(COUNTER_PERIOD_TYPE + " INTEGER,");
                        queryCounters.append(COUNTER_FORMULA + " TEXT,");
                        queryCounters.append(COUNTER_VIEW_VALUE_TYPE + " INTEGER,");
                        queryCounters.append(COUNTER_INPUT_VALUE_TYPE + " INTEGER");                        
                        queryCounters.append(");");

                        db.execSQL(queryCounters.toString());

                        // Скопируем исходные данные в новую структуру
                        queryCounters.setLength(0);
                        queryCounters.append("INSERT INTO " + TABLE_COUNTER + " (");
                        queryCounters.append(COUNTER_ID + ", ");
                        queryCounters.append(COUNTER_NAME + ", ");
                        queryCounters.append(COUNTER_NOTE + ", ");
                        queryCounters.append(COUNTER_MEASURE + ", ");
                        queryCounters.append(COUNTER_COLOR + ", ");
                        queryCounters.append(COUNTER_CURRENCY + ", ");
                        queryCounters.append(COUNTER_RATE_TYPE + ", ");
                        queryCounters.append(COUNTER_PERIOD_TYPE + ", ");
                        queryCounters.append(COUNTER_FORMULA + ", ");
                        queryCounters.append(COUNTER_VIEW_VALUE_TYPE + ", ");
                        queryCounters.append(COUNTER_INPUT_VALUE_TYPE + " ");
                        queryCounters.append(") SELECT ");
                        queryCounters.append(COUNTER_ID + ", ");
                        queryCounters.append(COUNTER_NAME + ", ");
                        queryCounters.append(COUNTER_NOTE + ", ");
                        queryCounters.append(COUNTER_MEASURE + ", ");
                        queryCounters.append(COUNTER_COLOR + ", ");
                        queryCounters.append(COUNTER_CURRENCY + ", ");
                        queryCounters.append(COUNTER_RATE_TYPE + ", ");
                        queryCounters.append(COUNTER_PERIOD_TYPE + ", ");
                        queryCounters.append(COUNTER_FORMULA + ", ");
                        queryCounters.append(COUNTER_VIEW_VALUE_TYPE + ", ");
                        queryCounters.append(COUNTER_INPUT_VALUE_TYPE + " ");
                        queryCounters.append("FROM Counters_old");

                        db.execSQL(queryCounters.toString());

                        // Удалим старую структуру
                        db.execSQL("DROP TABLE IF EXISTS Counters_old;");

                        db.setTransactionSuccessful();

                    } catch (Exception e) {
                        Log.d(CountersListActivity.LOG_TAG, e.getStackTrace().toString());
                    } finally {
                        db.endTransaction();
                    }
                }
            }
    /*
     * , new Patch() { public void apply(SQLiteDatabase db) { ... } public void
     * revert(SQLiteDatabase db) { ... } }
     */
    };

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
        for (int i = 0; i < PATCHES.length; i++) {
            PATCHES[i].apply(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = oldVersion; i < newVersion; i++) {
            PATCHES[i].apply(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = oldVersion; i > newVersion; i++) {
            PATCHES[i - 1].revert(db);
        }
    }

    // ===========================================================
    // Methods
    // ===========================================================
    
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
    private static class Patch {
        public void apply(SQLiteDatabase db) {
        }

        public void revert(SQLiteDatabase db) {
        }
    }
}
