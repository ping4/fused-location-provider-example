package com.kpbird.fusedlocation;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rindress on 9/16/13.
 */
public class locationInfo implements Parcelable {
  private String coordinates;
  private String date;

  public String getCoordinates() {
    return coordinates;
  }

  public void setCoordinates(String coordinates) {
    this.coordinates = coordinates;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public int describeContents() {
    return 0;
  }

  public void writeToParcel(Parcel out, int flags) {
    out.writeString(coordinates);
    out.writeString(date);
  }

  public static final Parcelable.Creator<locationInfo> CREATOR
      = new Parcelable.Creator<locationInfo>() {
    public locationInfo createFromParcel(Parcel in) {
      return new locationInfo(in);
    }

    public locationInfo[] newArray(int size) {
      return new locationInfo[size];
    }
  };

  private locationInfo(Parcel in) {
    coordinates = in.readString();
    date = in.readString();
  }

  public locationInfo(String c, String d){
    this.coordinates = c;
    this.date = d;
  }

}
