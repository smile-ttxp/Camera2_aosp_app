package com.plus.camera.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.android.camera.async.MainThread;

public class RoundedThumbnailView extends com.android.camera.widget.RoundedThumbnailView {
    public RoundedThumbnailView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void startRevealThumbnailAnimation(String accessibilityString) {
        MainThread.checkMainThread();
        // Create a new request.
        mPendingRequest = new RevealRequest(mViewRect.width(), accessibilityString);
    }
}
