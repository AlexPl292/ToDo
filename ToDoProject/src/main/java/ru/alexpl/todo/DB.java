package ru.alexpl.todo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class DB {

	private final Context mCtx;
	private final String LOG_TAG = "aDBLogs";

	//------------ Database -------------------
	public final String DB_NAME = "myProjectDB";
	public final int DB_VERSION = 1;

	public final List<String> TABLES = new ArrayList<String>() {{
		add("myTable");
		add("myFolder");
	}}; // all existing tables

	//------------ Main Table ----------------
	public final String MAIN_COLUMN_ID = "_id";
	public final String MAIN_COLUMN_TODO = "todo";
	public final String MAIN_COLUMN_IMP = "importance";
	public final String MAIN_COLUMN_FOLDER = "folderIn";
	//public final String MAIN_COLUMN_DATE = "date";

	//------------ query for create main table -----------
	public final String DB_MAIN_CREATE = "CREATE TABLE " +
			TABLES.get(0) + "(" +
			MAIN_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			MAIN_COLUMN_TODO + " TEXT NOT NULL, " +
			MAIN_COLUMN_IMP + " INTEGER(1), " +
			MAIN_COLUMN_FOLDER + " INTEGER(2) " +
			//MAIN_COLUMN_DATE + " DATETIME"+
			");";

	//---------------------- Folders table -------------------------
	public final String FOLDERS_COLUMN_ID = "_id";
	public final String FOLDERS_COLUMN_NAME_OF_FOLDER = "folderOut";

	//------------ query for create folders table -----------
	private final String DB_FOLDERS_CREATE = "CREATE TABLE " +
			TABLES.get(1) + "(" +
			FOLDERS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			FOLDERS_COLUMN_NAME_OF_FOLDER + " TEXT NOT NULL " +
			");";
	private final String DB_FOLDERS_DEFAULT = "INSERT INTO " +  // default folders
			TABLES.get(1) + " (" +
			FOLDERS_COLUMN_NAME_OF_FOLDER + ")" +
			" VALUES ('inbox'),('Home'),('Work');";

	private DBHelper mDBHelper;
	private SQLiteDatabase mDB;

	public DB(Context ctx) {
		mCtx = ctx;
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

	private boolean isDBOpen() {
		if (mDB == null) {
			Log.e(LOG_TAG, "Database is not open");
			return false;
		}
		return true;
	}

	public void close() {
		if (mDB != null) mDB = null;
		if (mDBHelper != null) mDBHelper.close();
		Log.d(LOG_TAG, "DB close");
	}

	public void createTestTable() {
		open();
		String query = "CREATE TABLE test (row1 int, row2 int);";
		mDB.execSQL(query);
		TABLES.add("test");
		Log.d("aDebug", "Test table created");
		close();
	}

	public void dropTestTable() {
		open();
		String query = "DROP TABLE test";
		mDB.execSQL(query);
		TABLES.remove("test");
		Log.d("aDebug", "Test table dropped");
		close();
	}

	/**
	 * @return cursor with all data from name
	 */
	public Cursor getAllDataFrom(String name) {

		if (!isTableExist(name) || !isDBOpen() || name == null) return null;

		Cursor c = mDB.query(name, null, null, null, null, null, null);
		Log.d(LOG_TAG, "getAllDataFrom: " + name);
		return c;
	}


	/**
	 * @return cursor with all data folder_table inner MAIN_TABLE
	 */
	public Cursor getAllDataAboutTodo() {

		if (!isDBOpen()) return null;

		String SQLQuery = "select TODO." + MAIN_COLUMN_ID + ", TODO." + MAIN_COLUMN_IMP + "," +
				" TODO." + MAIN_COLUMN_TODO + "," +
				" FOLDER." + FOLDERS_COLUMN_NAME_OF_FOLDER +
				" from " + TABLES.get(0) + " as TODO" +
				" inner join " + TABLES.get(1) + " as FOLDER" +
				" on TODO." + MAIN_COLUMN_FOLDER + " = FOLDER." + FOLDERS_COLUMN_ID;

		return mDB.rawQuery(SQLQuery, null);
	}

	public boolean delDataFrom(int id, String name) {   //TODO set del id if it is the last item of list

		if (!isTableExist(name) || !isDBOpen()) return false;

		mDB.delete(name, "_id =" + Integer.toString(id), null);
		Log.d(LOG_TAG, "Deleted from " + name + " id= " + id);

		return true;
	}

	public boolean addDataIn(Bundle dataForDB, String name) { //TODO next step

		if (!isTableExist(name) || !isDBOpen() || dataForDB == null || name == null) return false;

		if (name.equals(TABLES.get(0))) {
			String txt = dataForDB.getString(MAIN_COLUMN_TODO);
			int imp = dataForDB.getInt(MAIN_COLUMN_IMP);
			int folder = dataForDB.getInt(MAIN_COLUMN_FOLDER, 1);

			ContentValues cv = new ContentValues();
			cv.put(MAIN_COLUMN_TODO, txt);
			cv.put(MAIN_COLUMN_IMP, imp);
			cv.put(MAIN_COLUMN_FOLDER, folder);
			mDB.insert(TABLES.get(0), null, cv);
			return true;
		} else if (name.equals(TABLES.get(1))) {
			String text = dataForDB.getString(FOLDERS_COLUMN_NAME_OF_FOLDER);

			ContentValues cv = new ContentValues();
			cv.put(FOLDERS_COLUMN_NAME_OF_FOLDER, text);
			mDB.insert(TABLES.get(1), null, cv);
			return true;
		}

		return false;
	}

	public int getCountOfEntries(String name) {  //TODO it's error, if count == 0!!!

		if (!isDBOpen() || name == null) return -1;

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

		if (!isDBOpen() || name == null) return -1;

		int del_count = mDB.delete(name, null, null);  //clear table
		mDB.delete("SQLite_sequence", "name = '" + name + "'", null);  //delete ids
		Log.d(LOG_TAG, "DB clear: " + name);

		return del_count;
	}

	public boolean isEmpty(String nameOfTable) {
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