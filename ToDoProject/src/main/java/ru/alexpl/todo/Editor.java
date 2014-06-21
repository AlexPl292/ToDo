package ru.alexpl.todo;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

public class Editor implements View.OnClickListener {

	Context context;
	EditText editText;
	Activity act;
	DB db;
	int folderForDB;
	int importanceForDB;
	Spinner folderSpinner;
	Spinner impSpinner;
	ImageView rotator;
	ImageView adder;
	MainList fragmentList;

	public Editor(Context ctx, DB database) {
		db = database;
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

		makingEditor();
	}

	public void makingEditor() {

		folderForDB = 1;
		importanceForDB = 1;  // Настройки по умолчанию

		//--------------------------------- rotate and adder buttons ------------------

		rotator.setOnClickListener(this);
		adder.setOnClickListener(this);


		//--------------- making folder spinner---------------------
		db.open();
		int countOfMyFolder = db.getCountOfEntries(db.TABLES.get(1));
		Cursor c = db.getAllDataFrom(db.TABLES.get(1));
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
				folderForDB = 1;
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
				importanceForDB = 1;
			}
		});
	}

	public Bundle getData() {
		String s = editText.getText().toString();

		if (!folderSpinner.isShown() && !impSpinner.isShown()) {
			importanceForDB = 1;
			folderForDB = 1;
		} else {
			folderSpinner.setSelection(0);
			impSpinner.setSelection(1);
		}

		Bundle dataSet = new Bundle();
		dataSet.putString(db.MAIN_COLUMN_TODO, s);
		dataSet.putInt(db.MAIN_COLUMN_IMP, importanceForDB);
		dataSet.putInt(db.MAIN_COLUMN_FOLDER, folderForDB);

		if (!s.isEmpty()) {
			editText.setText("");
			return dataSet;
		} else return null;
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
				Bundle data = getData();
				if (data != null) {
					db.open();
					db.addDataIn(data, db.TABLES.get(0));
					assert fragmentList != null;
						fragmentList.updateList();
					db.close();
					break;
				}
		}
	}
}

