package ru.alexpl.todo;

import android.os.Bundle;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;


public class DBTest extends AndroidTestCase {

	private static final String TEST_FILE_PREFIX = "test_";
	private DB mdb;
	private RenamingDelegatingContext context;

	public void setUp() throws Exception {
		super.setUp();

		context = new RenamingDelegatingContext(getContext(), TEST_FILE_PREFIX);
		mdb = new DB(context);
		mdb.createTestTable();
	}

	public void tearDown() throws Exception {
		super.tearDown();
		mdb.dropTestTable();
		mdb.close();
		mdb = null;
	}


	public void testGetAllDataFrom() {
		Bundle bundle = new Bundle();
		bundle.putInt("row1", 1);
		bundle.putInt("row2", 2);

		mdb.addDataIn(bundle, mdb.TABLES.get(2));

	}

	/*public void testGetAllData() {

	}

	public void testDelDataFrom() {

	}

	public void testAddDataIn() {

	}

	public void testGetCountOfEntries() {

	}

	public void testClearTable() {

	}

	public void testAddDefaultFolders() {

	}

	public void testIsEmpty() {

	}*/

	/*private void backUp() throws Exception {  // TODO make back up
		setUp();

		Cursor[] backUp = new Cursor[mdb.TABLES.length];
		int i = 0;
		for (String tables : mdb.TABLES) {
			backUp[i] = mdb.getAllDataFrom(tables);
			i++;
		}

		Log.d("aDebug", context.getFilesDir().toString());

		tearDown();
	}*/
}
