package com.liftyourheads.dailyreadings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/* GOOGLE BASED MAP
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;*/

//// MAPBOX BASED MAP ////
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

//Style related imports
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import android.view.View;
import android.graphics.Color;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import static com.liftyourheads.dailyreadings.activity_date.readings;

public class fragment_bible_places_map extends Fragment  {

    MapboxMap map;
    MapView mMapView;
    FrameLayout greyOverlay;
    private Integer readingNum;
    String TAG = "Mapbox Instance";
    String JSON_CHARSET = "UTF-8";

    public static fragment_bible_places_map newInstance(int reading) {
        fragment_bible_places_map fragment = new fragment_bible_places_map();
        Bundle args = new Bundle();
        args.putInt("readingNum", reading);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readingNum = getArguments().getInt("readingNum");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bible_places_map, container, false);
        Mapbox.getInstance(requireActivity(), getString(R.string.mapbox_access_token));
        mMapView = view.findViewById(R.id.bibleMapView);
        greyOverlay = view.findViewById(R.id.greyOverlay);
        mMapView.onCreate(savedInstanceState);

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {

                map = mapboxMap;

                map.setStyleUrl("mapbox://styles/jakemadden/cjo3z4v16dju92so6jswmuffb");


                Log.i(TAG, "Map ready. Loading markers");


                List<String[]> places = readings[readingNum].getPlaces();
                int tagNumber = 0;
                Marker[] markers = new Marker[places.size()];

                // Add some markers to the map, and add a data object to each marker.

                for( String[] place : places) {

                    Double latitude = Double.parseDouble(place[1].replaceAll("[^\\d.]", ""));
                    Double longitude = Double.parseDouble(place[2].replaceAll("[^\\d.]", ""));
                    LatLng mLatLng = new LatLng(latitude, longitude);

                    markers[tagNumber] = map.addMarker(new MarkerOptions()
                            .setPosition(mLatLng)
                            .setTitle(place[0]));

                    markers[tagNumber].setId(tagNumber++);
                }

                //Zoom to fit markers
                CameraUpdate cu;

                if (markers.length > 0 ) { //Check if markers do exist for this reading

                    if (markers.length < 2) { //If there's only one point, zoom in to a reasonable distance
                        cu = CameraUpdateFactory.newLatLngZoom(markers[0].getPosition(), 6F);

                    } else { //Automatically zoom out to fit all points

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();

                            for (Marker marker : markers) {
                                try {
                                    builder.include(marker.getPosition());
                                } catch (Exception e) {
                                    //Log.i("Marker Info", "Failed to process marker " + marker.getTitle());
                                    e.printStackTrace();

                                }
                            }

                        LatLngBounds bounds = builder.build();

                        // Calculate distance between northeast and southwest
                        float[] results = new float[1];
                        android.location.Location.distanceBetween(bounds.getNorthEast().getLatitude(), bounds.getNorthEast().getLongitude(),
                                bounds.getSouthWest().getLatitude(), bounds.getSouthWest().getLongitude(), results);

                        if (results[0] < 3000) { // distance is less than 1 km -> set to zoom level 8
                            cu = CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 8F);
                        } else {
                            int padding = 200; // offset from edges of the map in pixels
                            cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                        }
                    }

                    mapboxMap.animateCamera(cu);


                    ////////// OFFLINE MAP DATA STORAGE /////////


                } else {
                    //There aren't any points to see! Grey out the map area
                    greyOverlay.setVisibility(View.VISIBLE);
                    mapboxMap.getUiSettings().setAllGesturesEnabled(false);
                }
            }
        });

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

    public void getOfflineMaps() {


        // Set up the OfflineManager
        OfflineManager offlineManager = OfflineManager.getInstance(getContext());

        // Create a bounding box for the offline region
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(new LatLng(47.415, 53.514966)) // Northeast
                .include(new LatLng(9.550219, -11.37934023)) // Southwest
                .build();

        // Define the offline region
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                map.getStyleUrl(),
                latLngBounds,
                10,
                20,
                getActivity().getResources().getDisplayMetrics().density);

        // Implementation that uses JSON to store Biblical World as the offline region name.
        byte[] metadata;

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Offline Map Region 1", "Biblical World");
            String json = jsonObject.toString();
            metadata = json.getBytes(JSON_CHARSET);
        } catch (Exception exception) {
            Log.e(TAG, "Failed to encode metadata: " + exception.getMessage());
            metadata = null;
        }

        // Create the region asynchronously
        offlineManager.createOfflineRegion(definition, metadata,
                new OfflineManager.CreateOfflineRegionCallback() {
                    @Override
                    public void onCreate(OfflineRegion offlineRegion) {
                        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);

                        // Monitor the download progress using setObserver
                        offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
                            @Override
                            public void onStatusChanged(OfflineRegionStatus status) {

                                // Calculate the download percentage
                                double percentage = status.getRequiredResourceCount() >= 0
                                        ? (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) :
                                        0.0;

                                if (status.isComplete()) {
                                // Download complete
                                    Log.d(TAG, "Region downloaded successfully.");
                                } else if (status.isRequiredResourceCountPrecise()) {
                                    Log.d(TAG, Double.toString(percentage));
                                }
                            }

                            @Override
                            public void onError(OfflineRegionError error) {
                                // If an error occurs, print to logcat
                                Log.e(TAG, "onError reason: " + error.getReason());
                                Log.e(TAG, "onError message: " + error.getMessage());
                            }

                            @Override
                            public void mapboxTileCountLimitExceeded(long limit) {
                                // Notify if offline region exceeds maximum tile count
                                Log.e(TAG, "Mapbox tile count limit exceeded: " + limit);
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error: " + error);
                    }
                });

    }
    /*

    GOOGLE MAPS BASED MAP

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i("On Map Ready", "Map ready. Loading markers");

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

        if (markers.length > 0 ) {

            if (markers.length < 2) { //If there's only one point, zoom in to a reasonable distance
                cu = CameraUpdateFactory.newLatLngZoom(markers[0].getPosition(), 6F);

            } else { //Automatically zoom out to fit all points

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker : markers) {
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();

                // Calculate distance between northeast and southwest
                float[] results = new float[1];
                android.location.Location.distanceBetween(bounds.northeast.latitude, bounds.northeast.longitude,
                        bounds.southwest.latitude, bounds.southwest.longitude, results);

                if (results[0] < 1000) { // distance is less than 1 km -> set to zoom level 15
                    cu = CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 8F);
                } else {
                    int padding = 200; // offset from edges of the map in pixels
                    cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                }
            }

            googleMap.animateCamera(cu);

        } else {
            //There aren't any points to see! Grey out the map area
            greyOverlay.setVisibility(View.VISIBLE);
            googleMap.getUiSettings().setAllGesturesEnabled(false);
        }

    }
    */

}