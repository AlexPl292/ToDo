package ru.alexpl.todo;

import android.app.Activity;
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
		db = DB.getInstanse(this);

		fragmentList = (MainList) getFragmentManager()
				.findFragmentById(R.id.fragmentList);

		editor = Editor.getInstanse(this);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}