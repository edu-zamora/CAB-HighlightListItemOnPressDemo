package com.example.highlightlistitemclick;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class CABSelection extends SherlockListActivity {

	private ArrayList<String> mItems = new ArrayList<String>();
	private ListView mListView;
	private SelectionAdapter mAdapter;
	private ActionMode mMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		for (int i = 0; i < 24; i++) {
			mItems.add("Name" + i);
		}

		mAdapter = new SelectionAdapter(this,
				R.layout.adapters_cabselection_row, R.id.the_text, mItems);
		setListAdapter(mAdapter);
		mListView = getListView();
		mListView.setSelector(R.drawable.list_selector);
		// TODO: Is this needed?
		// mListView.setItemsCanFocus(false);
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.i("TAG", "onItemLongClick");

				if (mMode == null) {
					startActionMode(new ActionMode.Callback() {

						@Override
						public boolean onCreateActionMode(ActionMode mode, Menu menu) {
							mMode = mode;
							mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
							MenuInflater inflater = getSupportMenuInflater();
							inflater.inflate(R.menu.cabselection_menu, menu);
							return true;
						}

						@Override
						public boolean onPrepareActionMode(ActionMode mode,
								Menu menu) {

							// TODO: Write a method for this
							SparseBooleanArray checkedPositions = mListView.getCheckedItemPositions();
							int nr = 0;
							for (int i = 0; i < checkedPositions.size(); i++) {
								if (checkedPositions.valueAt(i)) {
									nr++;
								}
							}

							// TODO: Use localization plurals for this
							if (nr == 1) {
								mode.setTitle(nr + " row");
							} else {
								mode.setTitle(nr + " rows");	
							}

							return false;
						}

						@Override
						public boolean onActionItemClicked(ActionMode mode,
								MenuItem item) {
							StringBuilder sb = new StringBuilder();
							SparseBooleanArray positions = mListView.getCheckedItemPositions();
							for (int i = 0; i < positions.size(); i++) {
								if (positions.get(i)) {
									sb.append(" " + i + ",");	
								}
							}               
							switch (item.getItemId()) {
							case R.id.select_none:
								uncheckAllItems();
								mode.invalidate();
								break;
							case R.id.select_all:
								checkAllItems();
								mode.invalidate();
								break;
							case R.id.edit_entry:
								Toast.makeText(CABSelection.this, "Edited entries: " + sb.toString(),
										Toast.LENGTH_SHORT).show();
								break;
							case R.id.delete_entry:
								Toast.makeText(CABSelection.this, "Deleted entries : " + sb.toString(),
										Toast.LENGTH_SHORT).show();
								break;
							case R.id.finish_it:
								Toast.makeText(CABSelection.this, "Finish the CAB!",
										Toast.LENGTH_SHORT).show();
								mode.finish();
							}
							return false;
						}

						@Override
						public void onDestroyActionMode(ActionMode mode) {
							// TODO: Is the null assignment needed?
							mMode = null;
							mListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
							uncheckAllItems();
						}

					});
				}

				SparseBooleanArray checkedPositions = mListView.getCheckedItemPositions();
				mListView.setItemChecked(position, !checkedPositions.get(position));
				onListItemClick(mListView, view, position, id);

				return true;
			}
		});
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (mMode != null) {
			mMode.invalidate();
		}
	}

	private void uncheckAllItems() {
		mListView.clearChoices();
		mAdapter.notifyDataSetChanged();
	}

	private void checkAllItems() {
		int numItems = mListView.getCount();
		for (int position = 0; position < numItems; position++) {
			mListView.setItemChecked(position, true);
		}
	}

	private class SelectionAdapter extends ArrayAdapter<String> {

		public SelectionAdapter(Context context, int resource,
				int textViewResourceId, List<String> objects) {
			super(context, resource, textViewResourceId, objects);
		}

		public boolean isChecked(int position) {
			SparseBooleanArray checked = mListView.getCheckedItemPositions();
			if (checked != null) {
				return checked.get(position);
			} else {
				return false;
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);

			if (mMode != null && isChecked(position)) {
				v.setBackgroundResource(R.drawable.list_item_selector_highlighted);
			} else {
				v.setBackgroundResource(R.drawable.list_item_selector_default);
			}

			return v;
		}

	}

}
