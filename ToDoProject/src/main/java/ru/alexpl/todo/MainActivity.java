package ru.alexpl.todo;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Resources;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {

	private final String LOG_TAG = "aMyLogs";
	private SimpleCursorAdapter scAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		new Connector().execute(this);
		new Editor(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onDestroy() {
		DB.instance = null;
		super.onDestroy();
	}

	/**
	 * Class extends AsyncTask for connect to DataBase and create an Editor
	 */
	private class Connector extends AsyncTask<Context, Void, Void> {

		@Override
		protected Void doInBackground(Context... params) {
			DB.getInstanse(params[0]);
			//new Editor(params[0]);
			return null;
		}
	}

	/**
	 * CursorLoader for SimpleCursorLoader
	 */
	static class MyCursorLoader extends CursorLoader {

		DB db;

		public MyCursorLoader(Context context, DB db) {
			super(context);
			this.db = db;
		}

		@Override
		public Cursor loadInBackground() {
			return db.getAllDataAboutTodo();
		}
	}

	/**
	 * Class for making spinners, buttons etc.
	 */
	private class Editor implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
		private static final String LOG_TAG = "aEditorLogs";

		private EditText editText;
		private Activity act;
		private DB db;
		private int folderForDB;
		private int importanceForDB;
		private Spinner folderSpinner;
		private Spinner impSpinner;
		private ImageView rotator;
		private ImageView adder;
		private ListView list;
		private final int importanceDefault = 1;
		private final int folderDefault = 0;

		private Editor(Context ctx) {
			act = (Activity) ctx;
			db = DB.getInstanse(act);

			folderSpinner = (Spinner) act.findViewById(R.id.folderSpinner);
			impSpinner = (Spinner) act.findViewById(R.id.impSpinner);
			folderSpinner.setVisibility(View.GONE);
			impSpinner.setVisibility(View.GONE);

			editText = (EditText) act.findViewById(R.id.ETTextInput);

			rotator = (ImageView) act.findViewById(R.id.IVRotator);
			adder = (ImageView) act.findViewById(R.id.IVAdd);

			//--------------------- making list --------------------
			list = (ListView) findViewById(R.id.todoList);

			String[] from = new String[]{db.MAIN_COLUMN_TODO, db.FOLDERS_COLUMN_NAME_OF_FOLDER, db.MAIN_COLUMN_IMP};
			int[] to = new int[]{R.id.TVListText, R.id.TVListFolder, R.id.IVEditor};

			scAdapter = new SimpleCursorAdapter(act, R.layout.list, null, from, to, 0) {
				@Override
				public void setViewImage(ImageView v, String value) {
					int imageId;
					switch (Integer.parseInt(value)) {
						case 0:
							imageId = R.drawable.item_edit_null;
							break;
						case 1:
							imageId = R.drawable.item_edit;
							break;
						case 2:
							imageId = R.drawable.item_edit_red;
							break;
						default:
							imageId = R.drawable.item_edit;
							break;
					}
					v.setImageResource(imageId);
					super.setViewImage(v, Integer.toString(imageId));
				}

				@Override
				public void bindView(View view, Context context, Cursor cursor) {
					super.bindView(view, context, cursor);
					view.setTag(cursor.getString(cursor.getColumnIndex(db.MAIN_COLUMN_ID)));
				}
			};
			list.setAdapter(scAdapter);

//			Looper.prepare();
			getLoaderManager().initLoader(0, null, this);
			setListner();

			new MakingEditor().execute(this);
		}

		public ContentValues getData() {
			//noinspection ConstantConditions
			String s = editText.getText().toString();
			if (s.isEmpty() || s == null) return null;

			if (!folderSpinner.isShown() && !impSpinner.isShown()) {
				importanceForDB = importanceDefault;
				folderForDB = folderDefault + 1;
			} else {
				folderSpinner.setSelection(folderDefault);
				impSpinner.setSelection(importanceDefault);
			}

			ContentValues dataSet = new ContentValues();
			dataSet.put(db.MAIN_COLUMN_TODO, s);
			dataSet.put(db.MAIN_COLUMN_IMP, importanceForDB);
			dataSet.put(db.MAIN_COLUMN_FOLDER, folderForDB);

			editText.setText("");
			return dataSet;
		}

		private void setListner() {
			OnSwipeTouchListener touchListener =
					new OnSwipeTouchListener(
							list,
							new OnSwipeTouchListener.DismissCallbacks() {
								@Override
								public boolean canDismiss(int position) {
									return true;
								}

								@Override
								synchronized public void onDismiss(ListView listView, int[] reverseSortedPositions) {
									Cursor cursor = (Cursor) listView.getItemAtPosition(reverseSortedPositions[0]);
									int id =
											Integer.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(db.MAIN_COLUMN_ID)));

									Cursor c = scAdapter.getCursor();
									CursorWithDelete cwd = new CursorWithDelete(c, reverseSortedPositions[0]);
									scAdapter.swapCursor(cwd);

									db.delDataFrom(id, db.MAIN_TABLE);
								}
							}
					);
			assert list != null;
			list.setOnTouchListener(touchListener);
			list.setOnScrollListener(touchListener.makeScrollListener());
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.IVRotator:
					if (!folderSpinner.isShown()) {
						impSpinner.setVisibility(View.VISIBLE);
						folderSpinner.setVisibility(View.VISIBLE);
						//rotator.clearAnimation();
						//rotator.animate().setDuration(200).rotation(180);
					} else {
						impSpinner.setVisibility(View.GONE);
						folderSpinner.setVisibility(View.GONE);
						//rotator.clearAnimation();
						//	rotator.animate().setDuration(200).rotation(0);
					}
					break;
				case R.id.IVAdd:
					ContentValues data = getData();
					if (data != null) {
						db.addDataIn(data, db.MAIN_TABLE);
						//noinspection ConstantConditions
						getLoaderManager().getLoader(0).forceLoad();
						break;
					}
			}
		}

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			return new MyCursorLoader(act, db);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			//CursorWithDelete cwd = new CursorWithDelete(data, )
			scAdapter.swapCursor(data);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
		}

		private class MakingEditor extends AsyncTask<View.OnClickListener, Void, Void> {

			@Override
			protected Void doInBackground(View.OnClickListener... params) {
				folderForDB = folderDefault + 1;
				importanceForDB = importanceDefault;  // Настройки по умолчанию

				//--------------------------------- rotate and adder buttons ------------------
				rotator.setOnClickListener(params[0]);
				adder.setOnClickListener(params[0]);

				//--------------- making folder spinner---------------------
				int countOfMyFolder = db.getCountOfEntries(db.FOLDER_TABLE);
				Cursor c = db.getAllDataFrom(db.FOLDER_TABLE);
				String[] dataForSpinner = new String[countOfMyFolder];

				if (c != null) {
					if (c.moveToFirst()) {
						int i = 0;
						do {
							dataForSpinner[i] =
									c.getString(c.getColumnIndex(db.FOLDERS_COLUMN_NAME_OF_FOLDER));
							i++;
						} while (c.moveToNext());
					}
				} else Log.e("aDBLogs", "DB of myFolder is empty");

				ArrayAdapter<String> adapterForFolderSpinner =
						new ArrayAdapter<String>(act, android.R.layout.simple_spinner_item, dataForSpinner);

				adapterForFolderSpinner.setDropDownViewResource(
						android.R.layout.simple_spinner_dropdown_item);

				folderSpinner.setAdapter(adapterForFolderSpinner);
				folderSpinner.setSelection(0);

				folderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
						folderForDB = i + 1;
					}

					@Override
					public void onNothingSelected(AdapterView<?> adapterView) {
						folderForDB = folderDefault + 1;
					}
				});

				//---------------------importance spinner------------------
				Resources res = act.getResources();
				Integer[] impInSpinner = new Integer[res.getInteger(R.integer.count_of_imp)];
				impInSpinner[0] = res.getInteger(R.integer.importance_0);
				impInSpinner[1] = res.getInteger(R.integer.importance_1);
				impInSpinner[2] = res.getInteger(R.integer.importance_2);
				ArrayAdapter<Integer> adapterForImpSpinner =
						new ArrayAdapter<Integer>(act, android.R.layout.simple_spinner_item, impInSpinner);
				adapterForImpSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

				impSpinner.setAdapter(adapterForImpSpinner);
				impSpinner.setSelection(res.getInteger(R.integer.importance_1));
				impSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
						importanceForDB = i;
					}

					@Override
					public void onNothingSelected(AdapterView<?> adapterView) {
						importanceForDB = importanceDefault;
					}
				});
				return null;
			}
		}

		private class CursorWithDelete extends AbstractCursor {

			private Cursor cursor;
			private int posToIgnore;

			public CursorWithDelete(Cursor cursor, int posToRemove) {
				this.cursor = cursor;
				this.posToIgnore = posToRemove;
			}

			@Override
			public boolean onMove(int oldPosition, int newPosition) {
				if (newPosition < posToIgnore) {
					cursor.moveToPosition(newPosition);
				} else {
					cursor.moveToPosition(newPosition + 1);
				}
				return true;
			}

			@Override
			public int getCount() {
				return cursor.getCount() - 1;
			}

			@Override
			public String[] getColumnNames() {
				return cursor.getColumnNames();
			}

			@Override
			public String getString(int column) {
				return cursor.getString(column);
			}

			@Override
			public short getShort(int column) {
				return cursor.getShort(column);
			}

			@Override
			public int getInt(int column) {
				return cursor.getInt(column);
			}

			@Override
			public long getLong(int column) {
				return cursor.getLong(column);
			}

			@Override
			public float getFloat(int column) {
				return cursor.getFloat(column);
			}

			@Override
			public double getDouble(int column) {
				return cursor.getDouble(column);
			}

			@Override
			public boolean isNull(int column) {
				return cursor.isNull(column);
			}
		}
	}
}