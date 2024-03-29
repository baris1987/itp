package com.th.nuernberg.quakedetec.acceleration;

import android.os.Parcel;
import android.os.Parcelable;

public final class AccelSample implements Parcelable {

	public final float x;
	public final float y;
	public final float z;
	public final float abs;
	public final long t;

	public static final Parcelable.Creator<AccelSample> CREATOR = new Parcelable.Creator<AccelSample>() {
		public AccelSample createFromParcel(Parcel in) {
			return new AccelSample(in);
		}

		public AccelSample[] newArray(int size) {
			return new AccelSample[size];
		}
	};

	/**
	 * An accelerometer sample.
	 * 
	 * @param x
	 *            X channel acceleration, meters per second squared.
	 * @param y
	 *            Y channel acceleration, meters per second squared.
	 * @param z
	 *            Z channel acceleration, meters per second squared.
	 * @param t
	 *            sample time in milliseconds, e.g. System.currentTimeMillis
	 */
	public AccelSample(float x, float y, float z, long t) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.abs = (float) ((float)Math.sqrt(x * x + y * y + z * z) - 9.81);
		this.t = t;
	}
	
	public AccelSample(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.abs = (float) ((float)Math.sqrt(x * x + y * y + z * z) - 9.81);
		this.t = System.currentTimeMillis();
	}


	public AccelSample(Parcel source) {
		x = source.readFloat();
		y = source.readFloat();
		z = source.readFloat();
		abs = source.readFloat();
		t = source.readLong();
	}


	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeFloat(x);
		dest.writeFloat(y);
		dest.writeFloat(z);
		dest.writeFloat(abs);
		dest.writeLong(t);
	}


	@Override
	public int describeContents() {
		return 0;
	}
}
