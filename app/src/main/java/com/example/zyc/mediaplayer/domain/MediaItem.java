package com.example.zyc.mediaplayer.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * @author zyc             //Parcelable 和 Serializable
 * 媒体信息
 */
public class MediaItem implements Serializable {
    private String name;
    private long duration;
    private long size;
    private String data;
    private String artist;

    public MediaItem(){

    }

//    protected MediaItem(Parcel in) {
//        name = in.readString();
//        duration = in.readLong();
//        size = in.readLong();
//        data = in.readString();
//        artist = in.readString();
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    /**
     * 用Code=>genarate=>tostring
     * @return
     */
    @Override
    public String toString() {
        return "MediaItem{" +
                "name='" + name + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                ", data='" + data + '\'' +
                ", artist='" + artist + '\'' +
                '}';
    }

//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(name);
//        dest.writeLong(duration);
//        dest.writeLong(size);
//        dest.writeString(data);
//        dest.writeString(artist);
//    }
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    public static final Creator<MediaItem> CREATOR = new Creator<MediaItem>() {
//        @Override
//        public MediaItem createFromParcel(Parcel in) {
//            return new MediaItem(in);
//        }
//
//        @Override
//        public MediaItem[] newArray(int size) {
//            return new MediaItem[size];
//        }
//    };

}
