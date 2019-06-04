package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static it.polito.mad.data_layer_access.FirebaseUtils.*;

public class IncomingReservationFragment extends Fragment
        implements RecyclerItemTouchHelperReservation.RecyclerItemTouchHelperListener {

    private static final String TAG = "IncomingReservation";

    private ArrayList<ReservationInfo> reservationInfoList;
    private RecyclerViewAdapterReservation myAdapter;
    private RecyclerView recyclerView;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public IncomingReservationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");

        View view = inflater.inflate(R.layout.fragment_incoming_reservation, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewIncomingReservation);

        preferences = getActivity().getSharedPreferences("loginState", Context.MODE_PRIVATE);

        setupFirebase();

        initializeReservation();

        return view;
    }

    private void initializeReservation() {

        branchOrdersIncoming.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reservationInfoList = new ArrayList<>();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ReservationInfo value = data.getValue(ReservationInfo.class);
                    value.setOrderID(data.getKey());

                    reservationInfoList.add(restoreItem(value));
                }

                triggerNotification();

                initializeRecyclerViewReservation();
            }

            private void triggerNotification() {


                if (reservationInfoList.size() == 0) {
                    branchRestaurantOrders.child("IncomingReservationFlag").setValue(false);

                    editor = preferences.edit();
                    editor.putBoolean("IncomingReservation", false);
                    editor.apply();
                } else {
                    branchRestaurantOrders.child("IncomingReservationFlag").setValue(true);

                    editor = preferences.edit();
                    editor.putBoolean("IncomingReservation", true);
                    editor.apply();
                }

                try {
                    getActivity().invalidateOptionsMenu();
                } catch (NullPointerException e) {
                    Log.w(TAG, "onDataChange: ", e);
                }
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

        try {
            Collections.sort(reservationInfoList, ReservationInfo.BY_TIME_ASCENDING);
        } catch (Exception e) {
            Log.w(TAG, "initializeRecyclerViewReservation: ", e);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(myAdapter);

        // adding item touch helper
        try {
            ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelperReservation(0,
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, this, getActivity(), true);

            new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
        } catch (NullPointerException e) {
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
        if (viewHolder instanceof RecyclerViewAdapterReservation.ReservationViewHolder) {
            // get the removed item name to display it in snack bar
            String name = reservationInfoList.get(viewHolder.getAdapterPosition()).getNamePerson();

            // backup of removed item for undo purpose
            final ReservationInfo deletedItem = reservationInfoList.get(viewHolder.getAdapterPosition());
            Log.d(TAG, "onSwiped: deletedItem " + deletedItem);
            //Log.d(TAG, "onSwiped: deletedIndex " + deletedIndex);
            final String deletedReservationId = deletedItem.getOrderID();
            Log.d(TAG, "onSwiped: deletedOrderId " + deletedReservationId);

            branchOrdersIncoming.child(deletedReservationId).removeValue();

            final DatabaseReference statusOrder = branchCustomer.child(deletedItem.getIdPerson())
                    .child("previous_order").child(deletedReservationId).child("order_status");

            storeTime(deletedItem.getTimeReservation(), false);
            storeFood(deletedItem.getOrderList(), false);

            if (direction == ItemTouchHelper.RIGHT) {

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

                        storeTime(deletedItem.getTimeReservation(), true);
                        storeFood(deletedItem.getOrderList(), true);
                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();

            } else if (direction == ItemTouchHelper.LEFT) {
                // showing snack bar with Undo option

                statusOrder.setValue("canceled");

                restoreQuantity(deletedItem.getOrderList(), true);

                Snackbar snackbar = Snackbar
                        .make(recyclerView, name + "\'s reservation removed", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // undo is selected, restore the deleted item
                        branchOrdersIncoming.child(deletedReservationId).setValue(restoreItem(deletedItem));
                        statusOrder.setValue("pending");

                        storeTime(deletedItem.getTimeReservation(), true);
                        storeFood(deletedItem.getOrderList(), true);

                        restoreQuantity(deletedItem.getOrderList(), false);
                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }
        }
    }

    private void restoreQuantity(Map<String, FoodInfo> foodList, Boolean flag) {
        branchDailyFood.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                    if (foodList.containsKey(dataSnapshot1.getKey())) {

                        branchDailyFood.child(dataSnapshot1.getKey() + "/quantity")
                                .runTransaction(new Transaction.Handler() {

                                    @NonNull
                                    @Override
                                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                        if (mutableData.getValue() == null) {
                                            mutableData.setValue("0");
                                        } else {
                                            Integer quantity;
                                            if (flag) {
                                                quantity = Integer.valueOf(mutableData.getValue().toString()) +
                                                        Integer.valueOf(foodList.get(dataSnapshot1.getKey()).getQuantity());
                                                mutableData.setValue(String.valueOf(quantity));
                                            } else {
                                                quantity = Integer.valueOf(mutableData.getValue().toString()) -
                                                        Integer.valueOf(foodList.get(dataSnapshot1.getKey()).getQuantity());
                                                mutableData.setValue(String.valueOf(quantity));
                                            }
                                        }

                                        return Transaction.success(mutableData);
                                    }

                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, boolean b,
                                                           @Nullable DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.getValue() != null) {

                                            if (dataSnapshot.getValue() instanceof String) {
                                                branchDailyFood.child(dataSnapshot1.getKey() + "/quantity")
                                                        .setValue(dataSnapshot.getValue());
                                            } else {
                                                branchDailyFood.child(dataSnapshot1.getKey() + "/quantity")
                                                        .setValue(String.valueOf(dataSnapshot.getValue()));
                                            }

                                        }
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        });
    }

    private void storeFood(Map<String, FoodInfo> orders, Boolean undo) {

        popularFoodBranch.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, String> foodTimes = new HashMap<>();

                Log.d(TAG, "storeFood: called");
                String dataSnapshotKey = "";

                try{
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        dataSnapshotKey = data.getKey();
                        break;
                    }

                    for (DataSnapshot data : dataSnapshot.child(dataSnapshotKey).getChildren()) {
                        String temp = data.getValue().toString();

                        foodTimes.put(data.getKey(), temp);
                    }

                    for (String key : orders.keySet()) {
                        if (foodTimes.get(key) != null) {
                            Integer t;

                            if (undo) {
                                t = Integer.valueOf(foodTimes.get(key)) -
                                        Integer.valueOf(orders.get(key).getQuantity());
                            } else {
                                t = Integer.valueOf(foodTimes.get(key)) +
                                        Integer.valueOf(orders.get(key).getQuantity());

                                if (t > 100) {
                                    t = 100;
                                }
                            }

                            foodTimes.put(key, String.valueOf(t));
                        } else {
                            foodTimes.put(key, orders.get(key).getQuantity());
                        }
                    }

                    popularFoodBranch.removeValue();
                    popularFoodBranch.push().setValue(foodTimes);
                } catch (NullPointerException nEx){
                    Log.w(TAG, "onDataChange: ", nEx);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        });
    }

    private void storeTime(String time, Boolean undo) {

        timeBranch.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, String> times = new HashMap<>();

                try{
                    for (DataSnapshot res : dataSnapshot.getChildren()) {
                        String temp = res.getValue().toString();

                        times.put(res.getKey(), temp);

                    }

                    if (times.get(time) != null) {
                        Integer t;

                        if (undo) {
                            t = Integer.valueOf(times.get(time)) - 1;

                        } else {
                            t = Integer.valueOf(times.get(time)) + 1;

                            if (t > 90) {
                                t = 90;
                            }
                        }

                        times.put(time, String.valueOf(t));
                    } else {
                        times.put(time, "1");
                    }
                    timeBranch.child(time).setValue(times.get(time));
                } catch (NullPointerException nEx){
                    Log.w(TAG, "onDataChange: ", nEx);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        });

    }

    public ReservationInfo restoreItem(ReservationInfo reservationInfo) {
        ReservationInfo res = new ReservationInfo();

        res.setOrderID(reservationInfo.getOrderID());
        res.setIdPerson(reservationInfo.getIdPerson());
        res.setOrderList(reservationInfo.getOrderList());
        res.setNamePerson(reservationInfo.getNamePerson());
        res.setcLatitude(reservationInfo.getcLatitude());
        res.setcLongitude(reservationInfo.getcLongitude());
        res.setTimeReservation(reservationInfo.getTimeReservation());

        if (reservationInfo.getNote() != null) {
            res.setNote(reservationInfo.getNote());
        }

        return res;
    }

}
