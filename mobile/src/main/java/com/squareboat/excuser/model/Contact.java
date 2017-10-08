package com.squareboat.excuser.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class Contact implements Parcelable {

    @SerializedName("id")
    private int mId;
    @SerializedName("mobile")
    private String mMobile;
    @SerializedName("name")
    private String mName;

    public Contact(int mId, String mMobile, String mName) {
        this.mId = mId;
        this.mMobile = mMobile;
        this.mName = mName;
    }

    public Contact() {
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getMobile() {
        return mMobile;
    }

    public void setMobile(String mobile) {
        mMobile = mobile;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mId);
        dest.writeString(this.mMobile);
        dest.writeString(this.mName);
    }

    protected Contact(Parcel in) {
        this.mId = in.readInt();
        this.mMobile = in.readString();
        this.mName = in.readString();
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel source) {
            return new Contact(source);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
}
