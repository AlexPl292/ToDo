package ru.alexpl.todo;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	DB db;
	MainList fragmentList;
	Editor editor;

	private final String LOG_TAG = "aMyLogs";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(LOG_TAG, "start");
		setContentView(R.layout.main);
		db = new DB(this);


		fragmentList = (MainList) getFragmentManager()
				.findFragmentById(R.id.fragmentList);
		if (fragmentList != null) {
			fragmentList.setDB(db); // TODO remake setDB with setBundle and getBundle
		}
		editor = new Editor(this, db);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void showAllData() {
		db.open();
		logCursor(db.getAllDataFrom(db.TABLES.get(0)));  //TODO remake for all tables
		Log.d(LOG_TAG, "-------------------------------------");
		logCursor(db.getAllDataFrom(db.TABLES.get(1)));
		db.close();
	}

	public void logCursor(Cursor c) {
		if (c != null) {
			if (c.moveToFirst()) {
				String str;
				do {
					str = "";
					for (String cn : c.getColumnNames()) {
						str = str.concat(cn + " = " + c.getString(c.getColumnIndex(cn)) + "; ");
					}
					Log.d(LOG_TAG, str);
				} while (c.moveToNext());
			} else
				Log.d(LOG_TAG, "Cursor is empty");
		} else
			Log.d(LOG_TAG, "Cursor is null");
	}
}