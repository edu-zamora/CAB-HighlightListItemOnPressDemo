/*
 * Copyright (C) 2013 Edu Zamora
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eduzamora.android.highlightlistitemonpress;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
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
import com.eduzamora.android.highlightlistitemonpress.R;
import com.eduzamora.android.highlightlistitemonpress.widget.PressListView;
import com.eduzamora.android.highlightlistitemonpress.widget.PressListView.OnItemPressListener;

public class MainActivity extends SherlockActivity {

	private PressListView mListView;
	private SelectionAdapter mAdapter;
	private ActionMode mMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cab_selection_layout);

		mAdapter = new SelectionAdapter(this, R.layout.cab_selection_item, R.id.sushi_name, SUSHIS);
		mListView = (PressListView) findViewById(R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setSelector(R.drawable.list_selector);
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
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

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
			inflater.inflate(R.menu.cab_selection_menu, menu);

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
				Toast.makeText(MainActivity.this, "Modified sushi: " + highlightedItemsString(),
						Toast.LENGTH_SHORT).show();
				break;
			case R.id.delete_entry:
				Toast.makeText(MainActivity.this, "Rotten sushi :( : " + highlightedItemsString(),
						Toast.LENGTH_SHORT).show();
				break;
			}
			return false;
		}

		private String highlightedItemsString() {
			StringBuilder stringBuilder = new StringBuilder();

			List<Integer> positions = mAdapter.getHighlightedPositions();
			if (positions.isEmpty()) {
				stringBuilder.append("None.");
			} else {
				for (int i = 0; i < positions.size(); i++) {
					int numberToShow = positions.get(i) + 1;
					if (i != (positions.size() - 1)) {
						stringBuilder.append(" " + numberToShow + ",");
					} else {
						stringBuilder.append(" " + numberToShow + ".");
					}
				}
			}

			return stringBuilder.toString();
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
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
				int textViewResourceId, String[] items) {
			super(context, resource, textViewResourceId, items);
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

	// NB: Taken from the next awesomely looking post:
	//     http://www.japan-talk.com/jt/new/101-kinds-of-sushi-in-Japan
	public static final String SUSHIS[] = {
		"1. Maguro Nigiri",
		"2. Kappa Maki",
		"3. Sake Nigiri",
		"4. Ikura Gukan",
		"5. Toro",
		"6. Uni",
		"7. Amaebi",
		"8. Ebi Nigiri",
		"9. Hamachi",
		"10. Anago",
		"11. Ika Nigiri",
		"12. Hotate Nigiri",
		"13. Tamagoyaki",
		"14. Tako Nigiri",
		"15. Tai",
		"16. Aji",
		"17. Tekkamaki",
		"18. Saba",
		"19. California Roll",
		"20. Futomaki",
		"21. Unagi",
		"22. Ayu",
		"23. Natto Maki",
		"24. Sanma",
		"25. Negitoro",
		"26. Kani Nigiri",
		"27. Kamaboko Kani",
		"28. Umeboshi",
		"29. Shako Nigiri",
		"30. Daikon Oshinko Maki",
		"31. Ankimo",
		"32. Mentaiko",
		"33. Temaki"
	};

}
