package com.example.highlightlistitempressed;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class PressedListView extends ListView {

	public interface OnItemPressListener {

		void onItemPress(int position);
	}

	private final ForwardingOnItemSelectedListener mForwardingOnItemSelectedListener = new ForwardingOnItemSelectedListener();
	
	private OnItemPressListener mOnItemPressListener;
	private int mFocusedItemPosition = -1;

	public PressedListView(Context context) {
		super(context);
		init();
	}

	public PressedListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PressedListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		super.setOnItemSelectedListener(mForwardingOnItemSelectedListener);
		mForwardingOnItemSelectedListener.selfListener = mOnItemSelectedListener;
	}

	
	@Override
	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		mForwardingOnItemSelectedListener.clientListener = listener;
	}

	public void setOnItemPressListener(OnItemPressListener listener) {
		mOnItemPressListener = listener;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			mOnItemPressListener.onItemPress(mFocusedItemPosition);
		}

		return super.onKeyDown(keyCode, event);
	}

	OnItemSelectedListener mOnItemSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			mFocusedItemPosition = position;
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			mFocusedItemPosition = -1;
		}
	};
	
	private static class ForwardingOnItemSelectedListener implements OnItemSelectedListener {

		private OnItemSelectedListener selfListener;
		private OnItemSelectedListener clientListener;

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			if (selfListener != null) {
				selfListener.onItemSelected(parent, view, position, id);
			}
			if (clientListener != null) {
				clientListener.onItemSelected(parent, view, position, id);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			if (selfListener != null) {
				selfListener.onNothingSelected(parent);
			}
			if (clientListener != null) {
				clientListener.onNothingSelected(parent);
			}
		}

	}
}
