package pt.patricio.demosdk;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.ImageView;

/**
 * Service that will handle the overlay
 * Created by patricio on 04-02-2017.
 */

public class OverlayService extends Service {

    private OverlayObject overlayObject;
    private ImageView view;

    public int onStartCommand(Intent intent, int flags, int startId) {
        int resource = intent.getIntExtra(CameraManager.OVERLAY, 0);

        overlayObject = new OverlayObject(this);

        view = new ImageView(this);
        view.setBackgroundResource(resource);
        overlayObject.addOverlayView(view);

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ISDKInterface.Stub() {

            @Override
            public Coordinates getCoordinates() throws RemoteException {
                if(overlayObject != null) {
                    return new Coordinates(overlayObject.getPositionX(), overlayObject.getPositionY());
                }
                return null;
            }
        };
    }

    @Override
    public void onDestroy() {
        overlayObject.removeOverlayView(view);
        stopSelf();
    }
}
