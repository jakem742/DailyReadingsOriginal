package com.liftyourheads.dailyreadings;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.liftyourheads.dailyreadings.activity_date.readings;

public class fragment_bible_places_map extends Fragment implements OnMapReadyCallback {


    MapView mMapView;
    GoogleMap map;
    Marker mPerth;
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

        readings[readingNum].getPlaces();

        // Add some markers to the map, and add a data object to each marker.
        mPerth = map.addMarker(new MarkerOptions()
                .position(PERTH)
                .title("Perth"));
        mPerth.setTag(0);

    }


}