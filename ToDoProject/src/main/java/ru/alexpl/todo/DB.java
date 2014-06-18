package ru.alexpl.todo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;


public class DB {

	private final Context mCtx;
	private final String LOG_TAG = "aDBLogs";

	//------------ Database -------------------
	public final String DB_NAME = "myProjectDB";
	public final int DB_VERSION = 1;

	//------------ Main Table ----------------
	public final String MAIN_TABLE = "myTable";
	public final String MAIN_COLUMN_ID = "_id";
	public final String MAIN_COLUMN_TODO = "todo";
	public final String MAIN_COLUMN_IMP = "importance";
	public final String MAIN_COLUMN_FOLDER = "folderIn"; // TODO remake!
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
	public final String FOLDERS_TABLE = "myFolder"; // it's for FOLDERS_TABLE
	public final String FOLDERS_COLUMN_ID = "id";
	public final String FOLDERS_COLUMN_NAME_OF_FOLDER = "folderOut";

	//------------ query for create folders table -----------
	private final String DB_FOLDERS_CREATE = "CREATE TABLE " +
													FOLDERS_TABLE + "(" +
													FOLDERS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
													FOLDERS_COLUMN_NAME_OF_FOLDER + " TEXT NOT NULL " +
													");";
	private final String DB_FOLDERS_DEFAULT = "INSERT INTO " +
													FOLDERS_TABLE + " (" +
													FOLDERS_COLUMN_NAME_OF_FOLDER + ")" +
													" VALUES ('inbox'),('Home'),('Work');";

	private DBHelper mDBHelper;
	private SQLiteDatabase mDB;

	public DB(Context ctx) {
		mCtx = ctx;
	}


	public void open() {
		if (mDB != null){
			Log.d(LOG_TAG, "DB is already open");
			return;
		}

		mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
		mDB = mDBHelper.getWritableDatabase();
		Log.d(LOG_TAG, "DB open");
	}

	public void close() {
		if (mDB!= null) mDB = null;
		if (mDBHelper != null) mDBHelper.close();
		Log.d(LOG_TAG, "DB close");
	}

	/**
	 * @return cursor with all data from name
	 */
	public Cursor getAllDataFrom(String name) {

		if (mDB == null) {
			Log.e(LOG_TAG, "Database is not open");
			return null;
		}

		if (name == null){
			return null;
		}

		if (!name.equals(MAIN_TABLE) && !name.equals(FOLDERS_TABLE)) {
			Log.e(LOG_TAG, "Database with name '"+name+"' is not exist");
			return null;
		}
		Cursor c = mDB.query(name, null, null, null, null, null, null);
		Log.d(LOG_TAG, "getAllDataFrom: " + name);
		return c;
	}


	/**
	 * @return cursor with all data folder_table inner MAIN_TABLE
	 */
	public Cursor getAllData() {

		if (mDB == null) {
			Log.e(LOG_TAG, "Database is not open");
			return null;
		}

		String SQLQuery = "select TODO." + MAIN_COLUMN_ID + ", TODO." + MAIN_COLUMN_IMP + "," +
				             " TODO." + MAIN_COLUMN_TODO + "," +
				             " FOLDER." + FOLDERS_COLUMN_NAME_OF_FOLDER +
				             " from " + MAIN_TABLE + " as TODO" +
				             " inner join " + FOLDERS_TABLE + " as FOLDER" +
				             " on TODO." + MAIN_COLUMN_FOLDER + " = FOLDER.id";

		return mDB.rawQuery(SQLQuery, null);
	}

	public boolean delDataFrom(int id, String name) {   //TODO set del id if it is the last item of list

		if (mDB == null) {
			Log.e(LOG_TAG, "Database is not open");
			return false;
		}

		if (name.equals(MAIN_TABLE)) {
			mDB.delete(MAIN_TABLE, MAIN_COLUMN_ID + "=" + Integer.toString(id), null);
			Log.e(LOG_TAG, "Deleted from " + MAIN_TABLE + " id= " + id);
			return true;
		} else if (name.equals(FOLDERS_TABLE)) {
			mDB.delete(FOLDERS_TABLE, FOLDERS_COLUMN_ID + "=" + Integer.toString(id), null);
			Log.e(LOG_TAG, "Deleted from " + FOLDERS_TABLE + " id= " + id);
			return true;
		}
		return false;
	}

	public boolean addDataIn(Bundle dataForDB, String name) {

		if (mDB == null) {
			Log.e(LOG_TAG, "Database is not open");
			return false;
		}

		if (!name.equals(MAIN_TABLE) && !name.equals(FOLDERS_TABLE)) {
			Log.e(LOG_TAG, "Database is not exist");
			return false;
		}

		if (name.equals(MAIN_TABLE)) {
			String txt = dataForDB.getString(MAIN_COLUMN_TODO);
			int imp = dataForDB.getInt(MAIN_COLUMN_IMP);
			int folder = dataForDB.getInt(MAIN_COLUMN_FOLDER, 1);

			ContentValues cv = new ContentValues();
			cv.put(MAIN_COLUMN_TODO, txt);
			cv.put(MAIN_COLUMN_IMP, imp);
			cv.put(MAIN_COLUMN_FOLDER, folder);
			mDB.insert(MAIN_TABLE, null, cv);
			return true;
		} else if (name.equals(FOLDERS_TABLE)) {

			String text = dataForDB.getString(FOLDERS_COLUMN_NAME_OF_FOLDER);
			ContentValues cv = new ContentValues();
			cv.put(FOLDERS_COLUMN_NAME_OF_FOLDER, text);
			mDB.insert(FOLDERS_TABLE, null, cv);
			return true;
		}


		return false;
	}

	public int getCountOfEntries(String name) {  //TODO it's error, if count == 0!!!

		if (mDB == null) {
			Log.e(LOG_TAG, "Database is not open");
			return -1;
		}

		String name_columns = "count";
		String columns[] = new String[]{"count(*) as "+name_columns};
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

		if (mDB == null) {
			Log.e(LOG_TAG, "Database is not open");
			return -1;
		}

		int del_count = mDB.delete(name, null, null);
		mDB.delete("SQLite_sequence", "name = '" + name + "'", null);  //delete id
		Log.d(LOG_TAG, "DB clear: " + name);

		return del_count;
	}

	public void addDefaultFolders(){

		if (mDB == null) {
			Log.e(LOG_TAG, "Database is not open");
			return;
		}

		String folders[] = {"inbox", "Home", "Work"};
		Bundle data = new Bundle();

		for (String folder : folders) {
			data.putString(FOLDERS_COLUMN_NAME_OF_FOLDER, folder);
			addDataIn(data, FOLDERS_TABLE);
		}
	}

	public boolean isEmpty(String nameOfTable){
		return getCountOfEntries(nameOfTable) == 0;
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
			//db.insert(FOLDERS_TABLE, )
			Log.d(LOG_TAG, "DB created");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}
}