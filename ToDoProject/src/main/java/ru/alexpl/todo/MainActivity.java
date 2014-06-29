package ru.alexpl.todo;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity {

	DB db;
	MainList fragmentList;
	Editor editor;


	private final String LOG_TAG = "aMyLogs";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		db = DB.getInstanse(this);
		ConnectToDB connectToDB = new ConnectToDB();
		connectToDB.execute(this);
		editor = Editor.getInstanse(this);
		/*try {
			connectToDB.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}*/


	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	class ConnectToDB extends AsyncTask<Context, Void, Void> {

		@Override
		protected Void doInBackground(Context... params) {

			fragmentList = (MainList) getFragmentManager()
					.findFragmentById(R.id.fragmentList);

			return null;
		}
	}
}