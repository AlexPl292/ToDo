package ru.alexpl.todo;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;


public class DBTest extends AndroidTestCase {

	private static final String TEST_FILE_PREFIX = "test_";
	private DB mdb;
	private String testFake = "testFake";
	private String testTable = "test";

	public void setUp() throws Exception {
		super.setUp();
		RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), TEST_FILE_PREFIX);
		mdb = new DB(context);
		mdb.createTestTable();
	}

	public void tearDown() throws Exception {
		super.tearDown();
		mdb.dropTestTable();
		mdb = null;
	}


	public void testGetAllDataFrom() {
		assertNull(mdb.getAllDataFrom(testFake));
		Cursor res = mdb.getAllDataFrom(testTable);
		assertNotNull(res);
		assertEquals("Empty tab returns not empty Cursor", res.getCount(), 0);

		addData();

		res = mdb.getAllDataFrom(testTable);

		assertNotNull(res);
		assertEquals(res.getCount(), 2);
		assertEquals(res.getColumnCount(), 3);

		if (res.moveToFirst()) {
			int i = 0;
			do {
				assertEquals("Return fake cursor", res.getInt(res.getColumnIndex("row1")), ++i);
				assertEquals("Return fake cursor", res.getInt(res.getColumnIndex("row2")), ++i);
			} while (res.moveToNext());
		}
		mdb.close();
	}

	public void testGetAllDataAboutTodo() {
		int i = mdb.getAllDataFrom(mdb.MAIN_TABLE).getCount();
		int j = mdb.getAllDataAboutTodo().getCount();
		assertTrue("Count of tasks not equals count of all",
				i == j);
		mdb.close();
	}

	public void testDelDataFrom() {
		assertFalse(mdb.delDataFrom(0, testTable));
		assertFalse(mdb.delDataFrom(0, testTable));
		assertFalse(mdb.delDataFrom(1, testTable));

		addData();

		assertFalse(mdb.delDataFrom(0, testFake));
		assertFalse(mdb.delDataFrom(-1, testTable));
		assertTrue(mdb.delDataFrom(1, testTable));

		Cursor res = mdb.getAllDataFrom(testTable);
		if (res.moveToFirst()) {
			do {
				assertEquals("Return fake", res.getInt(res.getColumnIndex("row1")), 3);
				assertEquals("Return fake", res.getInt(res.getColumnIndex("row2")), 4);
			} while (res.moveToNext());
		}

		assertTrue(mdb.delDataFrom(2, testTable));
		res = mdb.getAllDataFrom(testTable);
		assertEquals("Empty tab returns not empty Cursor", res.getCount(), 0);
	}

	public void testAddDataIn() {
		ContentValues contentValues = new ContentValues(); //add data
		contentValues.put("row1", 1);
		contentValues.put("row2", 2);
		assertTrue(mdb.addDataIn(contentValues, testTable) > 0);
		contentValues.clear();
		contentValues.put("row1", 3);
		contentValues.put("row2", 4);
		assertTrue(mdb.addDataIn(contentValues, testTable) > 0);
		assertFalse(mdb.addDataIn(contentValues, testFake) > 0);
		contentValues.clear();
		contentValues.put("rowFake", 3);
		contentValues.put("rowFake", 4);
		assertFalse(mdb.addDataIn(contentValues, testTable) > 0);
	}

	public void testGetCountOfEntries() {
		assertTrue(mdb.getCountOfEntries(testTable) == 0);
		addData();
		int testCountOfEntries = 2;
		assertTrue(mdb.getCountOfEntries(testTable) == testCountOfEntries);
		addData();
		assertTrue(mdb.getCountOfEntries(testTable) == testCountOfEntries * 2);
	}

	public void testClearTable() {
		addData();
		assertTrue(mdb.clearTable(testTable) == 2);
		assertTrue(mdb.getAllDataFrom(testTable).getCount() == 0);
		assertTrue(mdb.clearTable(testTable) == 0);
		assertTrue(mdb.clearTable(testFake) == -1);
	}

	public void testIsEmpty() {
		assertTrue(mdb.isEmpty(testTable));
		addData();
		assertFalse(mdb.isEmpty(testTable));
		assertFalse(mdb.isEmpty(testFake));
	}

	private void addData() {
		ContentValues contentValues = new ContentValues(); //add data
		contentValues.put("row1", 1);
		contentValues.put("row2", 2);
		mdb.addDataIn(contentValues, testTable);
		contentValues.clear();
		contentValues.put("row1", 3);
		contentValues.put("row2", 4);
		mdb.addDataIn(contentValues, testTable);
	}
}
