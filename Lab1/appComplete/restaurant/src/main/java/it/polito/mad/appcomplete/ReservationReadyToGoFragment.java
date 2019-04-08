package it.polito.mad.appcomplete;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


public class ReservationReadyToGoFragment extends Fragment {

    private static final String TAG = "ReservationReadyToGo";

    private List<ReservationInfo> reservationReadyToGoList;
    private RecyclerViewAdapterReservation myAdapter;
    private RecyclerView recyclerView;

    public ReservationReadyToGoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.incoming_reservation_fragment, container, false);

        initializeReservation(view);

        return view;
    }

    private void initializeReservation(View view) {
        reservationReadyToGoList = new ArrayList<>();

        //TODO: Populate the list by getting the cards from the preparing phase

        initializeRecyclerViewReservation(view);
    }

    private void initializeRecyclerViewReservation(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewIncomingReservation);

        myAdapter = new RecyclerViewAdapterReservation(getActivity(),
                reservationReadyToGoList);

        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    }
}
