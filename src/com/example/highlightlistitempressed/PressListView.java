package com.example.highlightlistitempressed;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class PressListView extends ListView {

	public interface OnItemPressListener {

		void onItemPress(int position);
	}

	private static final int INVALID_POSITION = -1;
	
	private final ForwardingOnTouchListener mForwardingOnTouchListener = new ForwardingOnTouchListener();
	private final ForwardingOnItemSelectedListener mForwardingOnItemSelectedListener = new ForwardingOnItemSelectedListener();
	
	private OnItemPressListener mOnItemPressListener;
	
	private GestureDetector mGestureDetector;
	
	private int mFocusedItemPosition = INVALID_POSITION;

	public PressListView(Context context) {
		super(context);
		init(context);
	}

	public PressListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PressListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mGestureDetector = new GestureDetector(context, new ListGestureDetectorListener());
		super.setOnTouchListener(mForwardingOnTouchListener);
		mForwardingOnTouchListener.selfListener = mOnTouchListener;
		super.setOnItemSelectedListener(mForwardingOnItemSelectedListener);
		mForwardingOnItemSelectedListener.selfListener = mOnItemSelectedListener;
	}

	public void setOnItemPressListener(OnItemPressListener listener) {
		mOnItemPressListener = listener;
	}
	
	@Override
	public void setOnTouchListener(OnTouchListener listener) {
		mForwardingOnTouchListener.clientListener = listener;
	}
	
	@Override
	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		mForwardingOnItemSelectedListener.clientListener = listener;
	}

	//-- Touch Presses --------------------------------------------------------

	private static class ForwardingOnTouchListener implements OnTouchListener {

		private OnTouchListener selfListener;
		private OnTouchListener clientListener;
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (selfListener != null) {
				selfListener.onTouch(v, event);
			}
			if (clientListener != null) {
				clientListener.onTouch(v, event);
			}
			return false;
		}
		
	}
	
	OnTouchListener mOnTouchListener = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return mGestureDetector.onTouchEvent(event);
		}
	};
	
	class ListGestureDetectorListener extends SimpleOnGestureListener {

		@Override
		public void onShowPress(MotionEvent e) {
			super.onShowPress(e);
			int position = positionFromMotionEvent(e);
			notifyItemPress(position);
		}
		
	}
		
	//-- D-pad/Trackball Presses ----------------------------------------------
	
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
	
	OnItemSelectedListener mOnItemSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			mFocusedItemPosition = position;
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			mFocusedItemPosition = INVALID_POSITION;
		}
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			notifyItemPress(mFocusedItemPosition);
		}

		return super.onKeyDown(keyCode, event);
	}
	
	//-- Helper methods--------------------------------------------------------

	private void notifyItemPress(int position) {
		if (mOnItemPressListener != null && validPosition(position)) {
			mOnItemPressListener.onItemPress(position);
		}
	}
	
	private boolean validPosition(int position) {
		return 0 <= position && position <= (getCount() - 1);
	}
	
	private int positionFromMotionEvent(MotionEvent event) {
		return pointToPosition((int) event.getX(), (int) event.getY());
	}
	
}
