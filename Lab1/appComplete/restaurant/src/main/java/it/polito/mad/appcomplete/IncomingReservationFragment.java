package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class IncomingReservationFragment extends Fragment
        implements RecyclerItemTouchHelperReservation.RecyclerItemTouchHelperListener,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "IncomingReservation";

    private ArrayList<ReservationInfo> reservationInfoList;
    private RecyclerViewAdapterReservation myAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mySwipeRefreshLayout;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private DatabaseReference database;

    public IncomingReservationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");

        View view = inflater.inflate(R.layout.fragment_incoming_reservation, container, false);

        mySwipeRefreshLayout = view.findViewById(R.id.swiperefresh);

        mySwipeRefreshLayout.setOnRefreshListener(this);

        recyclerView = view.findViewById(R.id.recyclerViewIncomingReservation);

        preferences = getActivity().getSharedPreferences("loginState", Context.MODE_PRIVATE);

        initializeReservation();

        return view;
    }

    private void initializeReservation() {

        database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference branchOrdersIncoming = database.child("restaurants/" +
                preferences.getString("Uid", "") + "/Orders/Incoming");

        branchOrdersIncoming.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reservationInfoList = new ArrayList<>();

                for (DataSnapshot data :  dataSnapshot.getChildren()){
                    ReservationInfo value = data.getValue(ReservationInfo.class);
                    value.setOrderID(data.getKey());

                    reservationInfoList.add(restoreItem(value));
                }

                DatabaseReference branchOrders = database.child("restaurants/" +
                                preferences.getString("Uid", "") + "/Orders/");

                if (reservationInfoList.size() == 0){
                    branchOrders.child("IncomingReservationFlag").setValue(false);

                    editor = preferences.edit();
                    editor.putBoolean("IncomingReservation", false);
                    editor.apply();
                } else {
                    branchOrders.child("IncomingReservationFlag").setValue(true);

                    editor = preferences.edit();
                    editor.putBoolean("IncomingReservation", true);
                    editor.apply();
                }

                try {
                    getActivity().invalidateOptionsMenu();
                } catch (NullPointerException e){
                    Log.w(TAG, "onDataChange: ", e);
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
        myAdapter = new RecyclerViewAdapterReservation(getActivity(), reservationInfoList);
        Log.d(TAG, "initializeRecyclerViewReservation: called");

        Collections.sort(reservationInfoList, ReservationInfo.BY_TIME_ASCENDING);


        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(myAdapter);

        // adding item touch helper
        try {
            ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelperReservation(0,
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, this, getActivity(), true);

            new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
        } catch (NullPointerException e){
            Log.w(TAG, "initializeRecyclerViewReservation: ", e);
        }
    }

    /*
     * callback when recycler view is swiped
     * item will be removed on swiped
     * undo option will be provided in snackbar to restore the item
     */
    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof RecyclerViewAdapterReservation.ViewHolder) {
            // get the removed item name to display it in snack bar
            String name = reservationInfoList.get(viewHolder.getAdapterPosition()).getNamePerson();

            // backup of removed item for undo purpose
            final ReservationInfo deletedItem = reservationInfoList.get(viewHolder.getAdapterPosition());
            Log.d(TAG, "onSwiped: deletedItem " + deletedItem);
            //Log.d(TAG, "onSwiped: deletedIndex " + deletedIndex);
            final String deletedReservationId = deletedItem.getOrderID();
            Log.d(TAG, "onSwiped: deletedOrderId " + deletedReservationId);

            preferences = getActivity().getSharedPreferences("loginState", Context.MODE_PRIVATE);

            database = FirebaseDatabase.getInstance().getReference();

            final DatabaseReference branchOrdersIncoming = database.child("restaurants/" +
                    preferences.getString("Uid", " ") + "/Orders/Incoming");

            branchOrdersIncoming.child(deletedReservationId).removeValue();

            final DatabaseReference statusOrder = database.child("customers").child(deletedItem.getIdPerson())
                    .child("previous_order").child(deletedReservationId).child("order_status");

            if (direction == ItemTouchHelper.RIGHT) {
                final DatabaseReference branchOrdersInPreparation = database.child("restaurants")
                        .child(preferences.getString("Uid", " ")).child("Orders")
                        .child("In_Preparation");

                branchOrdersInPreparation.child(deletedReservationId).setValue(restoreItem(deletedItem));

                statusOrder.setValue("In_Preparation");

                Snackbar snackbar = Snackbar
                        .make(recyclerView, name + "\'s reservation in preparation", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // undo is selected, restore the deleted item
                        branchOrdersIncoming.child(deletedReservationId).setValue(restoreItem(deletedItem));
                        branchOrdersInPreparation.child(deletedReservationId).removeValue();

                        statusOrder.setValue("pending");
                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();

            } else if (direction == ItemTouchHelper.LEFT) {
                // showing snack bar with Undo option

                statusOrder.setValue("canceled");
                Snackbar snackbar = Snackbar
                        .make(recyclerView, name + "\'s reservation removed", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // undo is selected, restore the deleted item
                        branchOrdersIncoming.child(deletedReservationId).setValue(restoreItem(deletedItem));

                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }
        }
    }

    public ReservationInfo restoreItem(ReservationInfo reservationInfo){
        ReservationInfo res = new ReservationInfo();

        res.setOrderID(reservationInfo.getOrderID());
        res.setIdPerson(reservationInfo.getIdPerson());
        res.setPersonOrder(reservationInfo.getPersonOrder());
        res.setNamePerson(reservationInfo.getNamePerson());
        res.setcLatitude(reservationInfo.getcLatitude());
        res.setcLongitude(reservationInfo.getcLongitude());
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
