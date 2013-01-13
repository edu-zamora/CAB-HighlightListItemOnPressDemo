package com.example.highlightlistitempressed;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.example.highlightlistitempressed.PressListView.OnItemPressListener;

public class CABSelection extends SherlockActivity {

	private ArrayList<String> mItems = new ArrayList<String>();
	private PressListView mListView;
	private SelectionAdapter mAdapter;
	private ActionMode mMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cab_selection);

		for (int i = 0; i < 24; i++) {
			mItems.add("Name" + i);
		}

		mAdapter = new SelectionAdapter(this, R.layout.adapters_cabselection_row, R.id.the_text, mItems);
		mListView = (PressListView) findViewById(R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setSelector(R.drawable.list_selector);
		// TODO: Is this needed?
		mListView.setItemsCanFocus(false);
		mListView.setFocusableInTouchMode(false);

		mListView.setOnItemPressListener(new OnItemPressListener() {

			@Override
			public void onItemPress(int position) {
				onListItemPress(position);
			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onListItemClick(position);
			}

		});

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.i("TAG", "onItemLongClick");

				if (mMode == null) {
					startActionMode(new ActionModeCallback());
				}

				onListItemClick(position);

				return true;
			}
		});
	}

	public void onListItemPress(int position) {
		if (mMode != null) {
			mAdapter.pressItem(position);
		}
	}

	public void onListItemClick(int position) {
		if (mMode != null) {
			mAdapter.toogleHighlightItem(position);
			mMode.invalidate();
		}
	}

	private class ActionModeCallback implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = getSupportMenuInflater();
			inflater.inflate(R.menu.cabselection_menu, menu);

			mMode = mode;
			mListView.setSelector(R.drawable.list_selector_cab);
			mAdapter.notifyDataSetChanged();
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			updateTitle(mode);

			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			StringBuilder sb = new StringBuilder();
			List<Integer> positions = mAdapter.getHighlightedPositions();
			for (int i = 0; i < positions.size(); i++) {
				sb.append(" " + i + ",");	
			}               
			switch (item.getItemId()) {
			case R.id.select_none:
				mAdapter.unhighlightAllItems();
				mode.invalidate();
				break;
			case R.id.select_all:
				mAdapter.highlightAllItems();
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
			}
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// TODO: Is the null assignment needed?
			mMode = null;
			mListView.setSelector(R.drawable.list_selector);
			mAdapter.unhighlightAllItems();	
		}

		private void updateTitle(ActionMode mode) {
			int highlightedItemsCount = mAdapter.getHighlightedPositionsCount();
			String actionModeTitle = getResources().getQuantityString(R.plurals.number_of_selected_rows, highlightedItemsCount, highlightedItemsCount);
			mode.setTitle(actionModeTitle);
		}

	};

	private class SelectionAdapter extends ArrayAdapter<String> {

		SparseBooleanArray mWasHighlightedBeforePress = new SparseBooleanArray();
		SparseBooleanArray mHighlightedItems = new SparseBooleanArray();

		public SelectionAdapter(Context context, int resource,
				int textViewResourceId, List<String> objects) {
			super(context, resource, textViewResourceId, objects);
		}

		public void pressItem(int position) {
			mHighlightedItems.put(position, true);
			notifyDataSetChanged();
		}

		public void toogleHighlightItem(int position) {
			boolean shouldHighlight = !wasHighlightedBeforePress(position);
			mHighlightedItems.put(position, shouldHighlight);
			mWasHighlightedBeforePress.put(position, shouldHighlight);
			notifyDataSetChanged();
		}

		public boolean wasHighlightedBeforePress(int position) {
			return mWasHighlightedBeforePress.get(position);
		}

		public boolean isHighlighted(int position) {
			return mHighlightedItems.get(position);
		}

		public List<Integer> getHighlightedPositions() {
			List<Integer> highlightedPositions = new ArrayList<Integer>();

			int numPositions = mHighlightedItems.size();
			for(int i = 0; i < numPositions; i++) {
				if (mHighlightedItems.valueAt(i)) {
					highlightedPositions.add(mHighlightedItems.keyAt(i));
				}
			}

			return highlightedPositions;
		}

		public int getHighlightedPositionsCount() {
			return getHighlightedPositions().size();
		}

		private void unhighlightAllItems() {
			mListView.clearChoices();
			int numItems = getCount();
			for (int position = 0; position < numItems; position++) {
				mHighlightedItems.put(position, false);
				mWasHighlightedBeforePress.put(position, false);
			}
			mAdapter.notifyDataSetChanged();
		}

		private void highlightAllItems() {
			int numItems = getCount();
			for (int position = 0; position < numItems; position++) {
				mHighlightedItems.put(position, true);
				mWasHighlightedBeforePress.put(position, true);
			}
			mAdapter.notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);

			if (isHighlighted(position)) {
				v.setBackgroundResource(R.drawable.list_item_selector_highlighted);
			} else {
				v.setBackgroundResource(R.drawable.list_item_selector_default);
			}

			return v;
		}

	}

}
