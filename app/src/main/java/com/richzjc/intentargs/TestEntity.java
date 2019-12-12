package com.richzjc.intentargs;

import android.os.Parcel;
import android.os.Parcelable;

public class TestEntity implements Parcelable {
    public String key;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.key);
    }

    public TestEntity() {
    }

    protected TestEntity(Parcel in) {
        this.key = in.readString();
    }

    public static final Parcelable.Creator<TestEntity> CREATOR = new Parcelable.Creator<TestEntity>() {
        @Override
        public TestEntity createFromParcel(Parcel source) {
            return new TestEntity(source);
        }

        @Override
        public TestEntity[] newArray(int size) {
            return new TestEntity[size];
        }
    };
}
