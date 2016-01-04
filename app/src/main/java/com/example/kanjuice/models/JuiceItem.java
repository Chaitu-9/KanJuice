package com.example.kanjuice.models;

import android.os.Parcel;
import android.os.Parcelable;

public class JuiceItem implements Parcelable {
    public String juiceName;
    public boolean isMultiSelected;
    public int selectedQuantity;
    public boolean isSugarless;
    public boolean animate;
    public int imageResId;
    public int kanResId;

    public JuiceItem(String juiceName, int imageId, int kanResId, boolean isSugarless) {
        this.juiceName = juiceName;
        this.imageResId = imageId;
        this.kanResId = kanResId;
        this.isSugarless = isSugarless;
        this.isMultiSelected = false;
        this.selectedQuantity = 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(juiceName);
        dest.writeByte((byte) (isSugarless ? 1:0));
        dest.writeInt(selectedQuantity);
    }

    public static final Creator<JuiceItem> CREATOR = new Creator<JuiceItem>() {
        public JuiceItem createFromParcel(Parcel in) {
            return new JuiceItem(in);
        }

        public JuiceItem[] newArray(int size) {
            return new JuiceItem[size];
        }
    };

    private JuiceItem(Parcel in) {
        juiceName = in.readString();
        isSugarless = in.readByte() != 0;
        selectedQuantity = in.readInt();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JuiceItem juiceItem = (JuiceItem) o;

        if (selectedQuantity != juiceItem.selectedQuantity) return false;
        if (isSugarless != juiceItem.isSugarless) return false;
        if (imageResId != juiceItem.imageResId) return false;
        if (kanResId != juiceItem.kanResId) return false;
        return !(juiceName != null ? !juiceName.equals(juiceItem.juiceName) : juiceItem.juiceName != null);

    }

    @Override
    public int hashCode() {
        int result = juiceName != null ? juiceName.hashCode() : 0;
        result = 31 * result + selectedQuantity;
        result = 31 * result + (isSugarless ? 1 : 0);
        result = 31 * result + imageResId;
        result = 31 * result + kanResId;
        return result;
    }
}
