package com.plus.camera.app;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;

import com.android.camera.CameraModule;
import com.android.camera.app.AppController;
import com.android.camera.config.AppConfig;
import com.android.camera.module.ModuleController;
import com.android.camera.ui.MainActivityLayout;
import com.android.camera.widget.RoundedThumbnailView;
import com.plus.camera.CameraActivity;
import com.plus.camera.Thumbnail;

import java.lang.ref.WeakReference;

public class CameraAppUI extends com.android.camera.app.CameraAppUI {
    private boolean mIsSecureCamera = false;
    public CameraAppUI(AppController controller, MainActivityLayout appRootView, boolean isCaptureIntent) {
        super(controller, appRootView, isCaptureIntent);

        if (controller instanceof com.plus.camera.app.AppController) {
            mIsSecureCamera = ((com.plus.camera.app.AppController) controller).isSecureCamera();
        }
    }

    @Override
    protected void initFilmstrip(ViewGroup appRootView) {
        if (mController instanceof com.plus.camera.app.AppController) {
            if (((com.plus.camera.app.AppController) mController).isFilmstripSupported()) {
                super.initFilmstrip(appRootView);
                mRoundedThumbnailView.setCallback(new RoundedThumbnailView.Callback() {
                    @Override
                    public void onHitStateFinished() {
                        showFilmstrip();
                    }
                });
            } else {
                mRoundedThumbnailView.setCallback(new RoundedThumbnailView.Callback() {
                    @Override
                    public void onHitStateFinished() {
                        if (mController instanceof CameraActivity) {
                            ((CameraActivity) mController).gotoGallery();
                        }
                    }
                });
            }
        }
    }

    @Override
    public void setupClingForViewer(int viewerType) {
    }

    @Override
    public void clearClingForViewer(int viewerType) {
    }

    @Override
    public void resume() {
        super.resume();
        initThumbnail();
    }

    @Override
    public void onModeButtonPressed(int modeIndex) {
    }

    @Override
    public void showBottomControls() {
    }

    @Override
    public void hideBottomControls() {
    }

    @Override
    public void setFilmstripBottomControlsListener(BottomPanel.Listener listener) {
    }

    @Override
    public void hideFilmstrip() {
    }

    @Override
    public int getFilmstripVisibility() {
        if (mFilmstripLayout == null) return View.GONE;
        return super.getFilmstripVisibility();
    }

    @Override
    public void updateCaptureIndicatorThumbnail(Bitmap thumbnailBitmap, int rotation) {
        this.updateCaptureIndicatorThumbnail(thumbnailBitmap, rotation, true);
    }

    private boolean needCaptureIndicator() {
        if (mIsCaptureIntent) return false;
        if (mSuppressCaptureIndicator || getFilmstripVisibility() == View.VISIBLE) {
            return false;
        }
        return true;
    }

    private CameraModule getCurrentModule() {
        ModuleController moduleController = mController.getCurrentModuleController();
        if (moduleController instanceof CameraModule) {
            return (CameraModule) moduleController;
        }
        return null;
    }

    public void initThumbnail() {
        updateThumbnailView(mIsSecureCamera);
    }

    private void updateThumbnailView(boolean isSecureCamera) {
        if (mRoundedThumbnailView == null) return;
        if (isSecureCamera) {
            clearThumbnailView();
        } else {
            ContentResolver contentResolver = mController.getAndroidContext().getContentResolver();
            final WeakReference<ContentResolver> resolver = new WeakReference<ContentResolver>(contentResolver);
            updateThumbnail(resolver);
        }
    }

    /** update thumbnail bitmap by latest. */
    private void updateThumbnail(final WeakReference<ContentResolver> resolver) {
        (new AsyncTask<Void, Void, Thumbnail>() {
            @Override
            protected Thumbnail doInBackground(Void... params) {
                ContentResolver cr = resolver.get();
                if (cr == null) return null;
                return Thumbnail.getLastThumbnail(cr);
            }

            @Override
            protected void onPostExecute(Thumbnail thumbnail) {
                if (thumbnail != null) {
                    if (mController instanceof CameraActivity) {
                        ((CameraActivity) mController).setThumbnailUri(thumbnail.getUri());
                    }
                    startThumbnailAnimation(thumbnail.getBitmap(), false);
                } else {
                    clearThumbnailView();
                }
            }
        }).execute();
    }

    /** update thumbnail bitmap by uri. */
    private void updateThumbnail(final WeakReference<ContentResolver> resolver, Uri targetUri) {
        (new AsyncTask<Uri, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Uri... params) {
                Uri uri = params[0];
                ContentResolver cr = resolver.get();
                if (cr == null) return null;
                Thumbnail t = Thumbnail.getThumbnailByUri(cr, uri);
                if (t != null) {
                    return t.getBitmap();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    startThumbnailAnimation(bitmap, false);
                }
            }
        }).execute(targetUri);
    }

    public void startThumbnailAnimation(Bitmap bitmap, boolean needAnimation) {
        startCaptureIndicatorRevealAnimation(getCurrentModule().getPeekAccessibilityString());
        updateCaptureIndicatorThumbnail(bitmap, 0, needAnimation);
    }

    private void updateCaptureIndicatorThumbnail(Bitmap thumbnailBitmap, int rotation, boolean needAnimation) {
        if (!needCaptureIndicator()) return;
        mRoundedThumbnailView.setThumbnail(thumbnailBitmap, rotation);
    }

    private void clearThumbnailView() {
        clearThumbnailUri();
    }

    public void clearThumbnailUri() {
        updateThumbnailUri(null);
    }

    public void updateThumbnailUri(Uri uri) {
        if (mController instanceof CameraActivity) {
            ((CameraActivity) mController).setThumbnailUri(uri);
        }
    }
}
