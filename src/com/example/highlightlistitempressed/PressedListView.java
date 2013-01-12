package com.example.highlightlistitempressed;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class PressedListView extends ListView {

	public interface OnTrackballEventListener {
		
		void onTrackballEvent(MotionEvent event);
	}
	
	private OnTrackballEventListener mOnTrackballEventListener = null;

	public PressedListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setOnTrackballEventListener(OnTrackballEventListener listener) {
		mOnTrackballEventListener = listener;
	}
	
	@Override
	public boolean onTrackballEvent(MotionEvent event) {
        if (mOnTrackballEventListener != null) {
			mOnTrackballEventListener.onTrackballEvent(event);
		}
		
		return true;
	}
	
}
