package ru.alexpl.todo;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.*;

public class Editor implements View.OnClickListener {
	public static Editor instanse;

	private Context context;
	private EditText editText;
	private Activity act;
	private DB db;
	private int folderForDB;
	private int importanceForDB;
	private Spinner folderSpinner;
	private Spinner impSpinner;
	private ImageView rotator;
	private ImageView adder;
	private MainList fragmentList;
	private final int importanceDefault = 1;
	private final int folderDefault = 0;


	private Editor(Context ctx) {
		context = ctx;
		act = (Activity) ctx;

		folderSpinner = (Spinner) act.findViewById(R.id.folderSpinner);
		impSpinner = (Spinner) act.findViewById(R.id.impSpinner);
		folderSpinner.setVisibility(View.GONE);
		impSpinner.setVisibility(View.GONE);

		editText = (EditText) act.findViewById(R.id.ETTextInput);

		rotator = (ImageView) act.findViewById(R.id.IVRotator);
		adder = (ImageView) act.findViewById(R.id.IVAdd);

		fragmentList = (MainList) act.getFragmentManager()
				                          .findFragmentById(R.id.fragmentList);

		/*makingEditor();*/
		MakingEditor makingEditor = new MakingEditor();
		makingEditor.execute(this);
	}

	public static Editor getInstanse(Context ctx) {
		if (instanse == null)
			instanse = new Editor(ctx);
		return instanse;
	}

	public void makingEditor() {

	}

	public ContentValues getData() {
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.IVRotator:
				if (!folderSpinner.isShown()) {
					impSpinner.setVisibility(View.VISIBLE);
					folderSpinner.setVisibility(View.VISIBLE);
					rotator.clearAnimation();
					rotator.animate().setDuration(200).rotation(180);
				} else {
					impSpinner.setVisibility(View.GONE);
					folderSpinner.setVisibility(View.GONE);
					rotator.clearAnimation();
					rotator.animate().setDuration(200).rotation(0);
				}
				break;
			case R.id.IVAdd:
				ContentValues data = getData();
				if (data != null) {
					db.addDataIn(data, db.MAIN_TABLE);
					assert fragmentList != null;
						fragmentList.updateList();
					break;
				}
		}
	}

	class MakingEditor extends AsyncTask<View.OnClickListener, Void, Void> {

		@Override
		protected Void doInBackground(View.OnClickListener... params) {
			db = DB.instanse;

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
			} else Log.d("DBLogs", "DB of myFolder is empty");

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
			                                        }
			);


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
}

