package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

import static it.polito.mad.data_layer_access.FirebaseUtils.*;


public class ReadyToGoReservationFragment extends Fragment
        implements RecyclerViewAdapterReservation.OnReservationClickListener {

    private static final String TAG = "ReservationReadyToGo";

    private ArrayList<ReservationInfo> reservationReadyToGoList;
    private RecyclerViewAdapterReservation myAdapter;
    private RecyclerView recyclerView;

    public ReadyToGoReservationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_readytogo_reservation, container, false);
        Log.d(TAG, "onCreateView: called");

        recyclerView = view.findViewById(R.id.recyclerViewReadyToGoReservation);

        setupFirebase();

        initializeReservation();

        return view;
    }

    private void initializeReservation() {

        branchOrdersReady.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reservationReadyToGoList = new ArrayList<>();

                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    try {
                       ReservationInfo value = data.getValue(ReservationInfo.class);
                       value.setOrderID(data.getKey());

                       Calendar date = Calendar.getInstance();

                       if (value.getStatus_order() != null && value.getStatus_order().equals("delivered")){

                           if (value.getDate() == null){
                               branchSoldOrders.child(value.getOrderID()).setValue(value);

                               branchSoldOrders.child(value.getOrderID()).child("date")
                                       .setValue(date.get(Calendar.DAY_OF_MONTH) + "-" +
                                               (date.get(Calendar.MONTH)+1) + "-" + date.get(Calendar.YEAR));

                               branchOrdersReady.child(value.getOrderID()).removeValue();
                           }


                       }

                       if (value.getStatus_order() != null && value.getStatus_order().equals("in_preparation")){
                           value.setStatus_order("waiting");
                       }
                       reservationReadyToGoList.add(restoreItem(value));
                   } catch (NullPointerException nEx){
                        Log.w(TAG, "onDataChange: ", nEx);
                    }

                }

                initializeRecyclerViewReservation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        });

    }


    private void initializeRecyclerViewReservation() {
        myAdapter = new RecyclerViewAdapterReservation(getActivity(), reservationReadyToGoList,
                this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(myAdapter);

    }

    public ReservationInfo restoreItem(ReservationInfo reservationInfo) {
        ReservationInfo res = new ReservationInfo();

        res.setOrderID(reservationInfo.getOrderID());
        res.setIdPerson(reservationInfo.getIdPerson());
        res.setRestaurantId(Uid);
        res.setNamePerson(reservationInfo.getNamePerson());
        res.setOrderList(reservationInfo.getOrderList());
        res.setcLatitude(reservationInfo.getcLatitude());
        res.setcLongitude(reservationInfo.getcLongitude());
        res.setTimeReservation(reservationInfo.getTimeReservation());

        if (reservationInfo.getNote() != null) {
            res.setNote(reservationInfo.getNote());
        }

        if (reservationInfo.getStatus_order() != null){
            res.setStatus_order(reservationInfo.getStatus_order());
        }
        return res;
    }


    @Override
    public void reservationClickListener(int position) {
        Log.d(TAG, "OnReservationClickListener: called");

        String status = reservationReadyToGoList.get(position).getStatus_order();

        if (status != null) {
            if (status.equals("ready") || status.equals("in_delivery")){
                return;
            }
        }

        Intent intent = new Intent(getActivity(), FindNearestRiderActivity.class);
        intent.putExtra("reservationId", reservationReadyToGoList.get(position).getOrderID());
        startActivity(intent);
    }
}
