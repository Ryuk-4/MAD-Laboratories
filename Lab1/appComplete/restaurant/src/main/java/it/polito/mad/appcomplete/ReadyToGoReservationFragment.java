package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ReadyToGoReservationFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "ReservationReadyToGo";

    private ArrayList<ReservationInfo> reservationReadyToGoList;
    private RecyclerViewAdapterReservation myAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mySwipeRefreshLayout;

    private SharedPreferences preferences;
    private DatabaseReference database;
    private DatabaseReference branchOrdersReady;

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

        mySwipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        mySwipeRefreshLayout.setOnRefreshListener(this);

        initializeReservation();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        initializeReservation();
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

                    reservationReadyToGoList.add(restoreItem(value));
                }

                initializeRecyclerViewReservation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        if(id == R.id.menu_refresh){

            /*
             * TODO: mySwipeRefreshLayout.setRefreshing(true);
             * TODO: myUpdateOP.
             */
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeRecyclerViewReservation() {
        myAdapter = new RecyclerViewAdapterReservation(getActivity(), reservationReadyToGoList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(myAdapter);

    }

    public ReservationInfo restoreItem(ReservationInfo reservationInfo) {
        ReservationInfo res = new ReservationInfo();

        res.setOrderID(reservationInfo.getOrderID());
        res.setIdPerson(reservationInfo.getIdPerson());
        res.setPersonOrder(reservationInfo.getPersonOrder());
        res.setNamePerson(reservationInfo.getNamePerson());
        res.setTimeReservation(reservationInfo.getTimeReservation());

        if (reservationInfo.getNote() != null) {
            res.setNote(reservationInfo.getNote());
        }

        return res;
    }

    @Override
    public void onRefresh() {
        //TODO: myUpdateOP.
        mySwipeRefreshLayout.setRefreshing(false);
    }
}
