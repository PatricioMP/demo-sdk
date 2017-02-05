package pt.patricio.demosdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.util.List;

@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private final static String LOG_TAG = CameraPreview.class.getName();
    private static final String LOG_CAMERA_EXCEPTION = "Camera Exception: ";

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private boolean cameraConfigured = false;

    public CameraPreview(Activity activity, Camera camera, FrameLayout preview) {
        super(activity);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        initPreview(w, h);
        startPreview();
    }

    private void startPreview() {
        if (cameraConfigured && mCamera != null) {
            mCamera.startPreview();
        }
    }

    @SuppressLint("LongLogTag")
    private void initPreview(int width, int height) {
        if (mCamera != null && mHolder.getSurface()!= null) {
            try {
                mCamera.setPreviewDisplay(mHolder);
            }
            catch (Exception e) {
                Log.e(LOG_TAG, LOG_CAMERA_EXCEPTION + e.getMessage());
            }

            if (!cameraConfigured) {
                Camera.Parameters parameters = mCamera.getParameters();
                Camera.Size size = getBestPreviewSize(width, height,
                        parameters);

                if (size != null) {
                    List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
                    List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
                    Camera.Size bestFitPreview = null;
                    Camera.Size bestFitPicture = null;
                    for(Camera.Size cameraSize : supportedPreviewSizes) {
                        if(mHolder.getSurfaceFrame().width() >= cameraSize.width &&
                                mHolder.getSurfaceFrame().height() >= cameraSize.height) {
                            if(bestFitPreview == null)
                                bestFitPreview = cameraSize;
                            else if(bestFitPreview.width < cameraSize.width)
                                bestFitPreview = cameraSize;
                            //break;
                        }
                    }

                    if(bestFitPreview != null) {
                        float previewRatio = (float)bestFitPreview.width / (float)bestFitPreview.height;
                        float pictureRatio;

                        for(Camera.Size pictureSize : supportedPictureSizes) {
                            pictureRatio = (float)pictureSize.width / (float)pictureSize.height;
                            if(pictureRatio == previewRatio) {
                                if(bestFitPicture == null)
                                    bestFitPicture = pictureSize;
                                else if(bestFitPicture.width < pictureSize.width && pictureSize.width <= 1920)
                                    bestFitPicture = pictureSize;
                            }
                        }

                        parameters.setPreviewSize(bestFitPreview.width, bestFitPreview.height);
                        if(bestFitPicture != null)
                            parameters.setPictureSize(bestFitPicture.width, bestFitPicture.height);
                        else
                            parameters.setPictureSize(bestFitPreview.width, bestFitPreview.height);

                        mCamera.setParameters(parameters);
                        cameraConfigured = true;
                    }
                }
            }
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            Log.v(LOG_TAG, "SupportPreview: width" + size.width + " height: " + size.height);
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                }
                else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        Log.v(LOG_TAG, "result: width" + result.width + " height: " + result.height);
        return result;
    }

}
