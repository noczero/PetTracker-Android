package com.zero.pettracker.ui.outdoor;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

public class Node implements Parcelable {
    private LatLng latLng;
    private String name;
    private ArrayList<String> connectedList;

    public Node(LatLng latLng, String name) {
        this.latLng = latLng;
        this.name = name;
        this.connectedList = null;
    }

    protected Node(Parcel in) {
        latLng = (LatLng) in.readValue(LatLng.class.getClassLoader());
        name = in.readString();
        connectedList = in.readArrayList(String.class.getClassLoader());
    }

    public static final Creator<Node> CREATOR = new Creator<Node>() {
        @Override
        public Node createFromParcel(Parcel in) {
            return new Node(in);
        }

        @Override
        public Node[] newArray(int size) {
            return new Node[size];
        }
    };

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getConnectedList() {
        return connectedList;
    }

    public void setConnectedList(ArrayList<String> connectedList) {
        this.connectedList = connectedList;
    }

    public void addConnectedList(String nodeID){
        this.connectedList.add(nodeID);
    }


    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.latLng);
        dest.writeString(this.name);
        dest.writeList(this.connectedList);
    }
}
