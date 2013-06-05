package com.th.nuernberg.quakedetec.acceleration;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public final class AccelSample implements Parcelable, Serializable {

	private static final long serialVersionUID = 1;

	public final float x;
	public final float y;
	public final float z;
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
		this.t = t;
	}
	
	public AccelSample(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.t = System.currentTimeMillis();
	}


	public AccelSample(Parcel source) {
		x = source.readFloat();
		y = source.readFloat();
		z = source.readFloat();
		t = source.readLong();
	}


	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeFloat(x);
		dest.writeFloat(y);
		dest.writeFloat(z);
		dest.writeLong(t);
	}


	@Override
	public int describeContents() {
		return 0;
	}
}
