package pt.patricio.demosdk;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * The coordinates of the overlay
 * Created by patricio on 04-02-2017.
 */

public class Coordinates implements Parcelable {
    
    private int x;
    
    private int y;

    public static final Parcelable.Creator<Coordinates> CREATOR = new Parcelable.Creator<Coordinates>() {
        @Override public Coordinates createFromParcel(Parcel source) {
            return new Coordinates(source);
        }

        @Override public Coordinates[] newArray(int size) {
            return new Coordinates[size];
        }
    };
    
    private Coordinates(Parcel parcel) {
        readFromParcel(parcel);
    }

    public Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(x);
        dest.writeInt(y);
    }

    private void readFromParcel(Parcel parcel) {
        x = parcel.readInt();
        y = parcel.readInt();
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

}
