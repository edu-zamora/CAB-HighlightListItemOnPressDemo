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

package com.eduzamora.android.highlightlistitemonpress.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.HapticFeedbackConstants;
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
	
	private GestureDetector mGestureDetector;
	
	private final ForwardingOnTouchListener mForwardingOnTouchListener = new ForwardingOnTouchListener();
	private final ForwardingOnItemSelectedListener mForwardingOnItemSelectedListener = new ForwardingOnItemSelectedListener();
	private final ForwardingOnItemClickListener mForwardingOnItemClickListener = new ForwardingOnItemClickListener();
	
	private OnItemPressListener mOnItemPressListener;
	private OnItemLongClickListener mOnItemLongClickListener;
	
	private boolean mWasConsumedByLongClick = false;
	
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
		super.setOnItemClickListener(mForwardingOnItemClickListener);
	}

	public void setOnItemPressListener(OnItemPressListener listener) {
		mOnItemPressListener = listener;
	}

	//-- Touch listener: used for detecting touch presses ---------------------

	@Override
	public void setOnTouchListener(OnTouchListener listener) {
		mForwardingOnTouchListener.clientListener = listener;
	}
	
	//-- Item selected listener: used for d-pad/trackpad presses --------------

	@Override
	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		mForwardingOnItemSelectedListener.clientListener = listener;
	}

	//-- Item long click & item click -----------------------------------------
	
	// NB:
	// We handle onItemLongClick manually here because the system onItemLongClick
	// callback is not always correctly called (from time to time, one callback is not made).
	// Doing that, makes us also handle onItemClick manually, so we only call it
	// when our onItemLongClick callback did not consume the event
	
	@Override
	public void setOnItemLongClickListener(OnItemLongClickListener listener) {
		mOnItemLongClickListener = listener;
		super.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (isInTouchMode()) {
					// Touch long clicks are handled using the GestureDetector
					return false;
				} else {
					// d-pad/trackpad long clicks are handled normally
					if (mOnItemLongClickListener != null) {
						return mOnItemLongClickListener.onItemLongClick(parent, view, position, id);
					}
				}
				
				return false;
			}
		});
	}
	
	@Override
	public void setOnItemClickListener(OnItemClickListener listener) {
		mForwardingOnItemClickListener.clientListener = listener;
	}
	
	private class ForwardingOnItemClickListener implements OnItemClickListener {

		private OnItemClickListener clientListener;
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (clientListener != null && !mWasConsumedByLongClick) {
				clientListener.onItemClick(parent, view, position, id);
			}
			mWasConsumedByLongClick = false;
		}
		
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

		@Override
		public void onLongPress(MotionEvent e) {
			super.onLongPress(e);
			int position = positionFromMotionEvent(e);
			notifyItemLongClick(position);
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
	
	private void notifyItemLongClick(int position) {
		if (mOnItemLongClickListener != null && validPosition(position)) {
			mWasConsumedByLongClick = mOnItemLongClickListener.onItemLongClick(this, getChildAt(position), position, getAdapter().getItemId(position));
			if (mWasConsumedByLongClick) {
				performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
			}
		}
	}
	
	private boolean validPosition(int position) {
		return 0 <= position && position <= (getCount() - 1);
	}
	
	private int positionFromMotionEvent(MotionEvent event) {
		return pointToPosition((int) event.getX(), (int) event.getY());
	}
	
}
