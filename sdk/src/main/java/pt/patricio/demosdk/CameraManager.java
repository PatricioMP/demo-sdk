package pt.patricio.demosdk;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Surface;
import android.widget.FrameLayout;

/**
 * Class responsible to manage all the camera related work
 * Created by patricio on 04-02-2017.
 */

@SuppressWarnings("deprecation")
public class CameraManager {

    private final static String LOG_TAG = CameraManager.class.getName();
    private static final String LOG_CAMERA_EXCEPTION = "Camera Exception: ";

    final static String OVERLAY = "overlay";

    private Activity activity;
    private FrameLayout anchor;
    private int overlay = 0;
    private Camera camera;
    private CameraPreview cameraPreview;
    private Intent intent;

    private CameraListener cameraListener;

    private static ISDKInterface service
            ;
    private static ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;
            Log.d(LOG_TAG,  " Disconnected from service");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ISDKInterface.Stub.asInterface(binder);
            Log.d(LOG_TAG,  " Connected to service");
        }
    };

    public interface CameraListener {
        void onImageCreate(Bitmap result);

        void onError(String error);
    }

    public CameraManager(Activity activity) {
        this.activity = activity;
    }

    public void start(FrameLayout anchor, int overlayResource) {
        this.anchor = anchor;
        this.overlay = overlayResource;
        anchor.removeAllViews();
        initCamera();
        anchor.addView(cameraPreview);
        intent = new Intent(activity, OverlayService.class);
        intent.putExtra(OVERLAY, overlay);

        activity.bindService(intent, connection, Context.BIND_NOT_FOREGROUND);
        activity.startService(intent);
    }

    public void stop() {
        activity.stopService(intent);
    }

    public void takePicture() {
        camera.takePicture(null, null, myPictureCallback_JPG);
    }

    public void setCameraListener(CameraListener listener) {
        cameraListener = listener;
    }

    private Camera.PictureCallback myPictureCallback_JPG = new Camera.PictureCallback(){

        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            Bitmap photo = BitmapFactory.decodeByteArray(arg0, 0, arg0.length);

            Bitmap rotate = rotateBitmap(photo);
            camera.stopPreview();
            Bitmap result = overlay(rotate,
                    BitmapFactory.decodeResource(activity.getResources(), overlay));
            Log.i(LOG_TAG, "width: " + result.getWidth() + " height: " + result.getHeight());
            if(cameraListener != null)
                cameraListener.onImageCreate(result);
        }
    };

    /**
     * Rotate the bitmap image on the desired orientation.
     * @param bmp: The bitmap image to rotate.
     * @return The flipped bitmap image.
     * */
    private Bitmap rotateBitmap(Bitmap bmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        matrix.postRotate(90);

        // recreate the new Bitmap
        return Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
    }

    /**
     * Initializes the camera hardware.
     * */
    private void initCamera() {
        if(checkCameraHardware(activity)) {
            // Create an instance of Camera
            camera = getCameraInstance(camera);
            // Create our Preview and set it as the content of our activity.
            cameraPreview = new CameraPreview(activity, camera, anchor);
        }
        else if(cameraListener != null) {
            cameraListener.onError("Can't initialize camera");
        }
    }

    /**
     * Gets the hardware camera instance.
     * @param camera: The camera to associate with the hardware.
     * @return The camera instance.
     * */
    private Camera getCameraInstance(Camera camera) {
        int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        boolean found = false;

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    camera = Camera.open(camIdx);
                    setCameraDisplayOrientation(activity, camIdx, camera);
                    found = true;
                } catch (Exception e) {
                    Log.e(LOG_TAG, LOG_CAMERA_EXCEPTION + e.getMessage());
                }
            }
        }

        if(!found) {
            for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    try {
                        camera = Camera.open(camIdx);
                        setCameraDisplayOrientation(activity, camIdx, camera);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, LOG_CAMERA_EXCEPTION + e.getMessage());
                    }
                }
            }
        }
        return camera;
    }

    private static void setCameraDisplayOrientation(Activity activity,
                                                    int cameraId, Camera camera) {
        Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        }
        else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    /**
     * Checks if the target phone has camera hardware.
     * @param context: The application activity.
     * @return True if camera hardware is available.
     * */
    private boolean checkCameraHardware(Context context) {
        // this device has a camera
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private Bitmap overlay(Bitmap original, Bitmap overlay) {
        Bitmap bmOverlay = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(original, new Matrix(), null);

        Log.v(LOG_TAG, "Overlay width: " + overlay.getWidth() + " height: " + overlay.getHeight());
        int centerX = overlay.getHeight() / 2;
        int centerY = (original.getWidth() - overlay.getHeight()) / 2;
        overlay = getResizedBitmap(overlay, original.getWidth() - overlay.getHeight());

        try {
            Coordinates coordinates = service.getCoordinates();
            Log.d(LOG_TAG, "X: " + coordinates.getX() + " Y: " + coordinates.getY());
            int left = centerX + coordinates.getX();
            int top = centerY + coordinates.getY();
            Log.d(LOG_TAG, "left: " + left + " top: " + top);
            canvas.drawBitmap(overlay, coordinates.getX(), coordinates.getY(), null);

        } catch (RemoteException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return bmOverlay;
    }

    private Bitmap getResizedBitmap(Bitmap bm, int newWidth)
    {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleWidth);
        // recreate the new Bitmap
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
    }

}
