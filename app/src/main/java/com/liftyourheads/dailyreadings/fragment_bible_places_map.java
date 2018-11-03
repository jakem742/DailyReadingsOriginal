package com.liftyourheads.dailyreadings;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import static com.liftyourheads.dailyreadings.activity_date.readings;

public class fragment_bible_places_map extends Fragment implements OnMapReadyCallback {


    MapView mMapView;
    GoogleMap map;

    static final LatLng PERTH = new LatLng(-31.952854, 115.857342);
    int readingNum = activity_bible_places.readingNum;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bible_places_map, parent, false);
        mMapView = view.findViewById(R.id.bibleMapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.getMapAsync(this);

        return view;
    }

    @Override
    public void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        if (mMapView != null) {
            mMapView.onResume();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null) {
            try {
                mMapView.onDestroy();
            } catch (NullPointerException e) {
                Log.e("Error", "Error while attempting MapView.onDestroy(), ignoring exception", e);
            }
        }    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i("On Map Ready", "Map ready. Loading marker");

        map = googleMap;


        List<String[]> places = readings[readingNum].getPlaces();
        int tagNumber = 0;
        Marker[] markers = new Marker[places.size()];

        // Add some markers to the map, and add a data object to each marker.

        for( String[] place : places) {

            Double latitude = Double.parseDouble(place[1].replaceAll("[^\\d.]", ""));
            Double longitude = Double.parseDouble(place[2].replaceAll("[^\\d.]", ""));
            LatLng mLatLng = new LatLng(latitude, longitude);

            markers[tagNumber] = map.addMarker(new MarkerOptions()
                    .position(mLatLng)
                    .title(place[0]));

            markers[tagNumber].setTag(tagNumber++);
        }

        //Zoom to fit markers

        CameraUpdate cu;

        if (markers.length < 2) {

            cu = CameraUpdateFactory.newLatLngZoom(markers[0].getPosition(), 6F);

        } else {

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();

            int padding = 200; // offset from edges of the map in pixels
            cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        }

        googleMap.animateCamera(cu);
    }


}