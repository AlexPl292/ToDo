package ru.alexpl.todo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class DB {

	public static DB instanse;

	private final Context mCtx;
	private final String LOG_TAG = "aDBLogs";

	//------------ Database -------------------
	public final String DB_NAME = "myProjectDB";
	public final int DB_VERSION = 1;

	public final List<String> TABLES = new ArrayList<String>() {{
		add(MAIN_TABLE);
		add(FOLDER_TABLE);
	}}; // all existing tables

	//------------ Main Table ----------------
	public final String MAIN_TABLE = "myTable";
	public final String MAIN_COLUMN_ID = "_id";
	public final String MAIN_COLUMN_TODO = "todo";
	public final String MAIN_COLUMN_IMP = "importance";
	public final String MAIN_COLUMN_FOLDER = "folderIn";
	//public final String MAIN_COLUMN_DATE = "date";

	//------------ query for create main table -----------
	public final String DB_MAIN_CREATE = "CREATE TABLE " +
			MAIN_TABLE + "(" +
			MAIN_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			MAIN_COLUMN_TODO + " TEXT NOT NULL, " +
			MAIN_COLUMN_IMP + " INTEGER(1), " +
			MAIN_COLUMN_FOLDER + " INTEGER(2) " +
			//MAIN_COLUMN_DATE + " DATETIME"+
			");";

	//---------------------- Folders table -------------------------
	public final String FOLDER_TABLE = "myFolder";
	public final String FOLDERS_COLUMN_ID = "_id";
	public final String FOLDERS_COLUMN_NAME_OF_FOLDER = "folderOut";

	//------------ query for create folders table -----------
	private final String DB_FOLDERS_CREATE = "CREATE TABLE " +
			FOLDER_TABLE + "(" +
			FOLDERS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			FOLDERS_COLUMN_NAME_OF_FOLDER + " TEXT NOT NULL " +
			");";
	private final String DB_FOLDERS_DEFAULT = "INSERT INTO " +  // default folders
			FOLDER_TABLE + " (" +
			FOLDERS_COLUMN_NAME_OF_FOLDER + ")" +
			" VALUES ('inbox'),('Home'),('Work');";

	private DBHelper mDBHelper;
	private SQLiteDatabase mDB;

	private DB(Context ctx) {
		mCtx = ctx;
		open();
	}

	public static synchronized DB getInstanse(Context ctx) {
		if (instanse == null) {
			instanse = new DB(ctx);
		}
		return instanse;
	}

	public void open() {
		if (mDB != null) {
			Log.d(LOG_TAG, "DB is already open");
			return;
		}

		mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
		mDB = mDBHelper.getWritableDatabase();
		Log.d(LOG_TAG, "DB open");
	}

	public void close() {
		if (mDB != null) mDB = null;
		if (mDBHelper != null) mDBHelper.close();
		Log.d(LOG_TAG, "DB close");
	}

	public void createTestTable() {
		String query = "CREATE TABLE test (_id INTEGER PRIMARY KEY AUTOINCREMENT, row1 int, row2 int);";
		mDB.execSQL(query);
		TABLES.add("test");
	}

	public void dropTestTable() {
		String query = "DROP TABLE test";
		mDB.execSQL(query);
		TABLES.remove("test");
	}

	/**
	 * @return cursor with all data from name
	 */
	public Cursor getAllDataFrom(String name) {

		if (!isTableExist(name) || name == null) return null;

		Cursor c = mDB.query(name, null, null, null, null, null, null);
		Log.d(LOG_TAG, "getAllDataFrom: " + name);

		return c;
	}

	/**
	 * @return cursor with all data folder_table inner MAIN_TABLE
	 */
	public Cursor getAllDataAboutTodo() {
		String SQLQuery = "select TODO." + MAIN_COLUMN_ID + ", TODO." + MAIN_COLUMN_IMP + "," +
				" TODO." + MAIN_COLUMN_TODO + "," +
				" FOLDER." + FOLDERS_COLUMN_NAME_OF_FOLDER +
				" from " + MAIN_TABLE + " as TODO" +
				" inner join " + FOLDER_TABLE + " as FOLDER" +
				" on TODO." + MAIN_COLUMN_FOLDER + " = FOLDER." + FOLDERS_COLUMN_ID;
		return mDB.rawQuery(SQLQuery, null);
	}

	public boolean delDataFrom(int id, String name) {

		if (!isTableExist(name) || id < 0) return false;

		Integer del = mDB.delete(name, "_id =" + Integer.toString(id), null);
		if (del > 0) {
			Log.d(LOG_TAG, "Deleted from " + name + " id= " + id);
			return true;
		} else {
			Log.d(LOG_TAG, "error in deleting");
			return false;
		}
	}

	public long addDataIn(ContentValues dataForDB, String name) {
		if (!isTableExist(name) || dataForDB == null || name == null) return -1;
		long insert = mDB.insert(name, null, dataForDB);
		return insert;
	}

	public int getCountOfEntries(String name) {
		if (name == null) return -1;

		String name_columns = "count";
		String columns[] = new String[]{"count(*) as " + name_columns};
		Cursor c = mDB.query(name, columns, null, null, null, null, null);
		int count = 0;
		if (c != null) {
			if (c.moveToFirst()) {
				count = c.getInt(c.getColumnIndex(name_columns));
			}
		}
		return count;
	}

	public int clearTable(String name) {
		if (name == null || !isTableExist(name)) return -1;

		int del_count = mDB.delete(name, null, null);  //clear table
		mDB.delete("SQLite_sequence", "name = '" + name + "'", null);  //delete ids
		Log.d(LOG_TAG, "DB clear: " + name);

		return del_count;
	}

	public boolean isEmpty(String nameOfTable) {
		if (nameOfTable == null || !isTableExist(nameOfTable)) {
			Log.e(LOG_TAG, "Problem in isEmpty(..)");
			return false;
		}
		int count = getCountOfEntries(nameOfTable);
		if (count == -1) Log.e(LOG_TAG, "Problem with '" + nameOfTable + "' in 'isEmpty(..)");

		return count == 0;
	}

	private boolean isTableExist(String name) {
		for (String tb : TABLES) {
			if (name == tb) return true;
		}
		Log.e(LOG_TAG, "Database with name '" + name + "' is not exist");
		return false;
	}

	public void finalize() throws Throwable {
		if (mDBHelper != null)
			mDBHelper.close();
		if (mDB != null)
			mDB.close();
		if (instanse != null)
			instanse = null;
		super.finalize();
	}

	/**
	 * class of DB helper
	 */
	private class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DB_MAIN_CREATE);
			db.execSQL(DB_FOLDERS_CREATE);
			db.execSQL(DB_FOLDERS_DEFAULT);
			Log.d(LOG_TAG, "DB created");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}
}