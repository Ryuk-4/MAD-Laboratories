package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class ReadyToGoReservationFragment extends Fragment
        implements RecyclerViewAdapterReservation.OnReservationClickListener {

    private static final String TAG = "ReservationReadyToGo";

    private ArrayList<ReservationInfo> reservationReadyToGoList;
    private RecyclerViewAdapterReservation myAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mySwipeRefreshLayout;

    private SharedPreferences preferences;
    private DatabaseReference database;
    private DatabaseReference branchOrdersReady;

    private FloatingActionButton fab1;

    private FirebaseAuth auth;

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

        auth = FirebaseAuth.getInstance();

        initializeReservation();

        return view;
    }

    private void initializeReservation() {
        preferences = getActivity().getSharedPreferences("loginState", Context.MODE_PRIVATE);

        database = FirebaseDatabase.getInstance().getReference();
        branchOrdersReady = database.child("restaurants").child(preferences.getString("Uid", " "))
                .child("Orders").child("Ready_To_Go");

        branchOrdersReady.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reservationReadyToGoList = new ArrayList<>();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ReservationInfo value = data.getValue(ReservationInfo.class);
                    value.setOrderID(data.getKey());

                    Date date = Calendar.getInstance().getTime();

                    if (value.getStatus_order() != null && value.getStatus_order().equals("in_delivery")) {
                        database.child("restaurants").child(auth.getCurrentUser().getUid())
                                .child("sold_orders").child(value.getOrderID()).setValue(value);

                        database.child("restaurants").child(auth.getCurrentUser().getUid())
                                .child("sold_orders").child(value.getOrderID()).child("date")
                                .setValue(String.valueOf(date));
                    } else {
                        reservationReadyToGoList.add(restoreItem(value));
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
                this, true);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(myAdapter);

    }

    public ReservationInfo restoreItem(ReservationInfo reservationInfo) {
        ReservationInfo res = new ReservationInfo();

        res.setOrderID(reservationInfo.getOrderID());
        res.setIdPerson(reservationInfo.getIdPerson());
        res.setRestaurantId(auth.getCurrentUser().getUid());
        res.setNamePerson(reservationInfo.getNamePerson());
        res.setOrderList(reservationInfo.getOrderList());
        res.setcLatitude(reservationInfo.getcLatitude());
        res.setcLongitude(reservationInfo.getcLongitude());
        res.setTimeReservation(reservationInfo.getTimeReservation());

        if (reservationInfo.getNote() != null) {
            res.setNote(reservationInfo.getNote());
        }
        return res;
    }


    @Override
    public void reservationClickListener(int position) {
        Log.d(TAG, "OnReservationClickListener: called");

        Intent intent = new Intent(getActivity(), FindNearestRiderActivity.class);
        intent.putExtra("reservationId", reservationReadyToGoList.get(position).getOrderID());
        startActivity(intent);
    }
}
