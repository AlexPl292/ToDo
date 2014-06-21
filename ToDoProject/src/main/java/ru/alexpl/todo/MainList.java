package ru.alexpl.todo;

import android.app.ListFragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * class for control List Fragment
 *
 * @author Alex Plate
 */
public class MainList extends ListFragment {

	DB mDB;
	AdapterForList adapterForList;
	final String LOG_TAG = "aListLogs";

	final String ATTRIBUTE_DATA = "data"; // TODO can i make this a local var?
	final String ATTRIBUTE_FOLDER = "folder";
	final String ATTRIBUTE_IMP = "importance";
	final String ATTRIBUTE_ID = "_id";


	public void onActivityCreated (Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		Log.d(LOG_TAG, "List created");

		updateList();
	}

	public void setDB(DB db){
		mDB = db;
		Log.d(LOG_TAG,"DB set to MainList");
	}

	public void updateList(){
		Cursor cursor;
		cursor = mDB.getAllDataAboutTodo();

		adapterForList = new AdapterForList(getActivity(), cursor);

		setListAdapter(adapterForList);
		Log.d(LOG_TAG,"update and DB close");


		//---------------------- making of listener-----------------

		ListView listView = getListView();

		OnSwipeTouchListener touchListener =
				new OnSwipeTouchListener(
	                listView,
                    new OnSwipeTouchListener.DismissCallbacks() {
                        @Override
                        public boolean canDismiss(int position) {
	                        return true;
                        }

                        @Override
                        public void onDismiss(ListView listView, int[] reverseSortedPositions) {
	                        for (int position : reverseSortedPositions) {
		                        mDB.delDataFrom(position, mDB.TABLES.get(0));
	                        }
	                        updateList();
                        }
                    });
		assert listView != null;

		listView.setOnTouchListener(touchListener);

		listView.setOnScrollListener(touchListener.makeScrollListener());
	}


	protected class AdapterForList extends BaseAdapter {

		Context ctx;
		Cursor data;
		LayoutInflater lInflater;

		int length = mDB.getCountOfEntries(mDB.TABLES.get(0));
		String []dataText = new String[length];
		String []dataFolder = new String[length];
		int []dataImp = new int[length];
		int []dataId = new int[length];

		public AdapterForList (Context context, Cursor dataForAdding){
			ctx = context;
			data = dataForAdding;
			lInflater = (LayoutInflater) ctx
					   .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			if(data!=null){
				int k=-1;
				if(data.moveToFirst()) do {
					k++;
					dataText[k] = data.getString(data.getColumnIndex(mDB.MAIN_COLUMN_TODO));
					dataFolder[k] =data.getString(data.getColumnIndex(mDB.FOLDERS_COLUMN_NAME_OF_FOLDER));
					dataImp[k] =data.getInt(data.getColumnIndex(mDB.MAIN_COLUMN_IMP));
					dataId[k] =data.getInt(data.getColumnIndex(mDB.MAIN_COLUMN_ID));
				} while (data.moveToNext());
			}
		}

		@Override
		public int getCount() {
			return data.getCount();
		}

		@Override
		public Object getItem(int i) {
			ArrayList<Map<String, Object>> arrayListItem = new ArrayList<Map<String, Object>>
					                                               (data.getCount());
			Map<String, Object> m;
			m = new HashMap<String, Object>();
			m.put(ATTRIBUTE_DATA, dataText[i]);
			m.put(ATTRIBUTE_FOLDER, dataFolder[i]);
			m.put(ATTRIBUTE_IMP, dataImp[i]);
			m.put(ATTRIBUTE_ID, dataId[i]);
			arrayListItem.add(m);

			return arrayListItem;
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View convertView, ViewGroup viewGroup) {

			View view = convertView;

			if (view == null) {
				view = lInflater.inflate(R.layout.list, viewGroup, false);
			}

			 int imageId;

			switch (dataImp[i]){
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

			assert view != null;
			((TextView) view.findViewById(R.id.TVListText)).setText(dataText[i]);
			((TextView) view.findViewById(R.id.TVListFolder)).setText(dataFolder[i]);
//          ((TextView) view.findViewById(R.id.listImp)).setText(Integer.toString(dataImp[i]));


			ImageView editorImage = (ImageView) view.findViewById(R.id.IVEditor);
			editorImage.setScaleY((float) 0.5);
			editorImage.setScaleX((float) 0.5);
			editorImage.setImageResource(imageId);

			view.setTag(dataId[i]);
			return view;
		}
	}
}