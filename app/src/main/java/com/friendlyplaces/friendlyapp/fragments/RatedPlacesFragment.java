package com.friendlyplaces.friendlyapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.friendlyplaces.friendlyapp.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class RatedPlacesFragment extends Fragment {


    public RatedPlacesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rated_places, container, false);
    }

}