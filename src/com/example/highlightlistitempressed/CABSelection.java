package com.example.highlightlistitempressed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.example.highlightlistitempressed.PressedListView.OnTrackballEventListener;

public class CABSelection extends SherlockActivity {

	private ArrayList<String> mItems = new ArrayList<String>();
	private PressedListView mListView;
	private SelectionAdapter mAdapter;
	private ActionMode mMode;
	private int mFocusedItemPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cab_selection);
		
		for (int i = 0; i < 24; i++) {
			mItems.add("Name" + i);
		}

		mAdapter = new SelectionAdapter(this,
				R.layout.adapters_cabselection_row, R.id.the_text, mItems);
		mListView = (PressedListView) findViewById(R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setSelector(R.drawable.list_selector);

		final GestureDetector gestureDetector = new GestureDetector(this, new ListGestureDetectorListener());
		mListView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});
		mListView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Log.i("TAG", "onItemSelected");
				mFocusedItemPosition = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				Log.i("TAG", "onNothingSelected");
			}
		});
		
		mListView.setOnTrackballEventListener(new OnTrackballEventListener() {
			
			@Override
			public void onTrackballEvent(MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					onListItemPress(mFocusedItemPosition);	
				}
			}
		});
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onListItemClick(position);
			}

		});
		
		// TODO: Is this needed?
		mListView.setItemsCanFocus(false);
		mListView.setFocusableInTouchMode(false);
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
							mListView.setSelector(R.drawable.list_selector_cab);
							mAdapter.notifyDataSetChanged();
							MenuInflater inflater = getSupportMenuInflater();
							inflater.inflate(R.menu.cabselection_menu, menu);
							return true;
						}

						@Override
						public boolean onPrepareActionMode(ActionMode mode,
								Menu menu) {

							int nr = mAdapter.getHighlightedPositionsCount();
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
							mListView.setSelector(R.drawable.list_selector);
							mAdapter.unhighlightAllItems();
						}

					});
				}

				onListItemClick(position);

				return true;
			}
		});
	}

	class ListGestureDetectorListener extends SimpleOnGestureListener {

		@Override
		public void onShowPress(MotionEvent e) {
			Log.i("TAG", "onShowPress");
			int position = mListView.pointToPosition((int) e.getX(), (int) e.getY());
			onListItemPress(position);
		}

	}

	public void onListItemPress(int position) {
		if (mMode != null) {
			mAdapter.pressItem(position);
			mMode.invalidate();
		}
	}
	
	public void onListItemClick(int position) {
		if (mMode != null) {
			mAdapter.toogleHighlightItem(position);
			mMode.invalidate();
		}
	}
	
	private class SelectionAdapter extends ArrayAdapter<String> {

		HashMap<Integer, Boolean> mWasHighlightedBeforePress = new HashMap<Integer, Boolean>();
		HashMap<Integer, Boolean> mHighlightedItems = new HashMap<Integer, Boolean>();

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
			Boolean wasHighlighted = mWasHighlightedBeforePress.get(position);
			if (wasHighlighted != null) {
				return wasHighlighted;
			} else {
				return false;
			}
		}

		public boolean isHighlighted(int position) {
			Boolean isHighlighted = mHighlightedItems.get(position);
			if (isHighlighted != null) {
				return isHighlighted;
			} else {
				return false;
			}
		}

		public List<Integer> getHighlightedPositions() {
			List<Integer> highlightedPositions = new ArrayList<Integer>();

			for (int position = 0; position < mHighlightedItems.size(); position++) {
				if (isHighlighted(position)) {
					highlightedPositions.add(position);
				}
			}

			return highlightedPositions;
		}

		public int getHighlightedPositionsCount() {			
			return getHighlightedPositions().size();
		}

		private void unhighlightAllItems() {
			mListView.clearChoices();
			int numItems = mListView.getCount();
			for (int position = 0; position < numItems; position++) {
				mHighlightedItems.put(position, false);
				mWasHighlightedBeforePress.put(position, false);
			}
			mAdapter.notifyDataSetChanged();
		}

		private void highlightAllItems() {
			int numItems = mListView.getCount();
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
