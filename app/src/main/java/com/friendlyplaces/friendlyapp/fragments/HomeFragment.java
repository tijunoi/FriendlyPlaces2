package com.friendlyplaces.friendlyapp.fragments;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.friendlyplaces.friendlyapp.R;
import com.friendlyplaces.friendlyapp.activities.DetailedPlaceActivity;
import com.friendlyplaces.friendlyapp.model.FriendlyPlace;
import com.friendlyplaces.friendlyapp.utilities.MarkerColorUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnPoiClickListener,
        GoogleMap.OnInfoWindowClickListener, ClusterManager.OnClusterItemInfoWindowClickListener<FriendlyPlace> {

    GoogleMap mMap;
    Fragment mapFragment;
    FragmentManager fmanager;
    //
    //
    // FloatingActionButton floatingActionButton;
    public static final int PLACE_PICKER_REQUEST = 1;
    OnPlacePickedListener mOnPlacePickedListener;

    // Declare a variable for the cluster manager.
    private ClusterManager<FriendlyPlace> mClusterManager;


    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(layoutInflater, viewGroup, bundle);
        View v = layoutInflater.inflate(R.layout.fragment_home, viewGroup, false);
        //floatingActionButton = v.findViewById(R.id.quick_button);
        //floatingActionButton.setTransitionName(getString(R.string.fabTransition));
        //floatingActionButton.setOnClickListener(this);
        mapFragment = getChildFragmentManager().findFragmentById(R.id.map);
        SupportMapFragment supportmapfragment = (SupportMapFragment) mapFragment;
        supportmapfragment.getMapAsync(this);
        return v;

    }

    /**
     * Referencia del marker visualizandose actualmente. Se guarda para poder borrarlo luego, ya que GoogleMap no tiene el método
     */
    Marker currentMarker;
    PointOfInterest currentPointOfInterest; //para comparar el place id

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPlacePickedListener) {
            mOnPlacePickedListener = (OnPlacePickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        // mMap.setOnPoiClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();
        if (mMap.isMyLocationEnabled()) {
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();

            if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            //todo: crear nosaltres un botó que sigui com el de location i copipastear aixo a el onclick
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                        .zoom(17)                   // Sets the zoom
                        .bearing(90)                // Sets the orientation of the camera to east
                        .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }

            // Initialize the manager with the context and the map.
            // (Activity extends context, so we can pass 'this' in the constructor.)
            mClusterManager = new ClusterManager<>(getActivity(), mMap);
            mMap.setOnInfoWindowClickListener(mClusterManager);
            mMap.setOnCameraIdleListener(mClusterManager);
            mMap.setOnMarkerClickListener(mClusterManager);
            mClusterManager.setOnClusterItemInfoWindowClickListener(this);
            mClusterManager.setRenderer(new FriendlyPlaceClusterItemRenderer(getContext(), mMap, mClusterManager));

            //Agafo la instancia de la Database Firestore. Normalment no l'agafes per el mig del codi.
            // L'agafes al principi del onCreate, o fas servir FirebaseFirestore.getInstance() directament
            FirebaseFirestore db = FirebaseFirestore.getInstance();


            /*
             * En aquesta demo agafo tots els places que hi ha a la database i faig un marker per cada un
             * OBVIAMENT no sera aixi en real, es només per que et facis idea de Firebase
             */

            db.collection("FriendlyPlaces")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                //La queri retorna un array de DocumentSnapshots (task.getResult() es la array) per aixo es fa un foreach
                                for (DocumentSnapshot document : task.getResult()) {
                                    Log.d("Firestore", document.getId() + " => ");

                                    //Firebase AUTOPARSEJA, aixi que ja tens el objecte creat i parsejat
                                    FriendlyPlace lloc = document.toObject(FriendlyPlace.class); //Obviament si lo que et retorna la query no te els atribut que te el Objecte peta

                                    mClusterManager.addItem(lloc);

                                    //Marker marker = mMap.addMarker(new MarkerOptions().position(lloc.getLatLng()).title("Nom del lloc").snippet("Rating: " + lloc.avgRating));
                                    //marker.showInfoWindow();

                                }
                            } else {
                                Log.d("Firestore", "Error getting documents: ", task.getException());
                            }
                        }
                    });


        }

        // Add a marker in Sydney and move the camera
       /* LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            getActivity().finish();
        }
    }

    @Override
    public void onPoiClick(PointOfInterest pointOfInterest) {
        if (currentMarker != null) {
            if (pointOfInterest.placeId.equals(currentPointOfInterest.placeId)) {
                //do nothing
            } else {
                currentMarker.remove();
                currentMarker = mMap.addMarker(new MarkerOptions()
                        .position(pointOfInterest.latLng)
                        .title(pointOfInterest.name)
                        .snippet("Pulsa para ver los detalles"));
                currentMarker.setTag(pointOfInterest);
                currentPointOfInterest = pointOfInterest;
            }
        } else {
            currentMarker = mMap.addMarker(new MarkerOptions()
                    .position(pointOfInterest.latLng)
                    .title(pointOfInterest.name)
                    .snippet("Pulsa para ver los detalles"));
            currentMarker.setTag(pointOfInterest);
            currentPointOfInterest = pointOfInterest;
        }
    }



    @Override
    public void onInfoWindowClick(Marker marker) {
        PointOfInterest poi = (PointOfInterest) marker.getTag();

        Intent intent = new Intent(getContext(), DetailedPlaceActivity.class);
        //ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), floatingActionButton, getString(R.string.fabTransition));
        if (poi != null) {
            intent.putExtra("placeId", poi.placeId);
            intent.putExtra("placeName", poi.name);
            startActivity(intent);
        }


    }

    @Override
    public void onClusterItemInfoWindowClick(FriendlyPlace friendlyPlace) {
        Intent intent = new Intent(getContext(), DetailedPlaceActivity.class);
        //ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), floatingActionButton, getString(R.string.fabTransition));
        intent.putExtra("placeId", friendlyPlace.pid);
        intent.putExtra("placeName", friendlyPlace.name);

        startActivity(intent);
    }


    public interface OnPlacePickedListener {
        void OnTryingPickingAPlace();
    }

    public class FriendlyPlaceClusterItemRenderer extends DefaultClusterRenderer<FriendlyPlace> {
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getActivity().getApplicationContext());

        @Override
        protected void onBeforeClusterItemRendered(FriendlyPlace item, MarkerOptions markerOptions) {
            BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.defaultMarker(MarkerColorUtil.getColor(item));
            markerOptions.icon(markerDescriptor);
        }

        public FriendlyPlaceClusterItemRenderer(Context context, GoogleMap map, ClusterManager<FriendlyPlace> clusterManager) {
            super(context, map, clusterManager);
        }
    }
}



