package com.example.SalinasCrimeMap;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class CrimeEvent {

    private String listNumber;
    private String date;
    private String topic;
    private String location;
    private Context context;
    private String htmlLink;


    CrimeEvent(Context context){
        this.listNumber = "";
        this.date = "";
        this.topic = "";
        this.location = "";
        this.context = context;
        this.htmlLink = "";
    }



    CrimeEvent(String listNumber, String date, String topic, String location, String htmlLink){

        this.listNumber = listNumber;
        this.date = date;
        this.topic = topic;
        this.location = location;
        this.htmlLink = htmlLink;
    }



    public LatLng getLocationFromAddress(final String strAddress) {


        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 1);
            if (address == null) {
                return null;
            }

            if(address.size() > 0){
                Address location = address.get(0);
                p1 = new LatLng(location.getLatitude(), location.getLongitude() );
            }

        } catch (IOException ex) {

            Log.d("error:", "Error plotting address: " + strAddress);
        }

        return p1;
    }

    public void setHtmlLink(String htmlLink){

        this.htmlLink = htmlLink;
    }
    public void setListNumber(String listNumber){
        this.listNumber = listNumber;
    }

    public void setDate(String date){
        this.date = date;
    }

    public void setTopic(String topic){
        this.topic = topic;
    }

    public void setLocation(String location){
        this.location = location;
    }

    public String getListNumber(){
        return listNumber;
    }

    public String getDate(){
        return date;
    }

    public String getTopic(){
        return topic;
    }

    public String getLocation(){
        return location;
    }

    public String getHtmlLink(){

        return htmlLink;
    }


}
