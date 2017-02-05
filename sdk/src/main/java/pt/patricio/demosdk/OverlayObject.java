package pt.patricio.demosdk;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * View that will contain the overlay
 * Created by patricio on 04-02-2017.
 */
public class OverlayObject {
    private static final String LOG_TAG = "OverlayObject";

    private WindowManager windowManager;
    private View overlayView;

    private WindowManager.LayoutParams layoutParams;

    private int locationX, locationY;

    OverlayObject(Context context) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }


    final void removeOverlayView(View view) {
        if (view != null) {
            windowManager.removeView(view);
        }
    }

    int getPositionX() {
        return locationX;
    }

    int getPositionY() {
        return locationY;
    }

    /** Add a global floating view.
     *
     * @param view the view to overlay across all apps and activities
     */
    final void addOverlayView(View view) {
        overlayView = view;
        layoutParams = newWindowManagerLayoutParams();

        View.OnTouchListener mOnTouchListener = newSimpleOnTouchListener();
        overlayView.setOnTouchListener(mOnTouchListener);

        windowManager.addView(overlayView, newWindowManagerLayoutParams());
    }

    /** Provides the drag ability for the overlay view. This touch listener
     * allows user to drag the view anywhere on screen. */
    private View.OnTouchListener newSimpleOnTouchListener() {
        return new View.OnTouchListener() {
            //            private long timeStart; // Maybe use in the future, with ViewConfiguration's getLongClickTime or whatever it is called.
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private int[] overlayViewLocation = {0,0};

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        timeStart = System.currentTimeMillis();
                        initialX = layoutParams.x;
                        initialY = layoutParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        return true;
                    case MotionEvent.ACTION_MOVE:
                        layoutParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                        layoutParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(overlayView, layoutParams);

                        overlayView.getLocationOnScreen(overlayViewLocation);

                        //Log.v(LOG_TAG, "Overlay X: " + layoutParams.x);
                        //Log.v(LOG_TAG, "Overlay Y: " + layoutParams.y);

                        Log.v(LOG_TAG, "location X: " + overlayViewLocation[0]);
                        Log.v(LOG_TAG, "location Y: " + overlayViewLocation[1]);

                        locationX = overlayViewLocation[0];
                        locationY = overlayViewLocation[1];

                        return true;
                    case MotionEvent.ACTION_UP:

                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        return true;
                }
                return false;
            }
        };
    }

    /** Returns the default layout params for the overlay views. */
    private static WindowManager.LayoutParams newWindowManagerLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;

        return params;
    }
}