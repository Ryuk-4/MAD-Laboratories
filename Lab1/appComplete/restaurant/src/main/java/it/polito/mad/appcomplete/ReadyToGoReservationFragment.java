package it.polito.mad.appcomplete;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


public class ReadyToGoReservationFragment extends Fragment {

    private static final String TAG = "ReservationReadyToGo";

    private List<ReservationInfo> reservationReadyToGoList =  new ArrayList<>();
    private RecyclerViewAdapterReservation myAdapter;
    private RecyclerView recyclerView;

    private ReservationActivityInterface resActivityInterface;

    public ReadyToGoReservationFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: " + TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_incoming_reservation, container, false);
        Log.d(TAG, "onCreateView: " + TAG);

        recyclerView = view.findViewById(R.id.recyclerViewIncomingReservation);
        initializeRecyclerViewReservation();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        resActivityInterface = (ReservationActivityInterface)getActivity();
        Log.d(TAG, "onAttach: " + TAG);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        resActivityInterface = null;
    }

    public void newReservationHasSent(ReservationInfo reservation) {
        //if(reservationPreparingList.size())
        reservationReadyToGoList.add(reservation);
        Log.d(TAG, "newReservationHasSent: ");
        initializeRecyclerViewReservation();
    }

    private void initializeRecyclerViewReservation() {
        myAdapter = new RecyclerViewAdapterReservation(getActivity(),
                reservationReadyToGoList);

        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    }

    public void removeItem(){
        reservationReadyToGoList.remove(reservationReadyToGoList.size() - 1);
        initializeRecyclerViewReservation();
    }
}
